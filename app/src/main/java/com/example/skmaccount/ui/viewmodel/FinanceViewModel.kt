package com.example.skmaccount.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.skmaccount.data.model.Budget
import com.example.skmaccount.data.model.Category
import com.example.skmaccount.data.model.Expense
import com.example.skmaccount.data.repository.FinanceRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class FinanceViewModel(private val repository: FinanceRepository) : ViewModel() {

    val allCategories: StateFlow<List<Category>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val overallBudget: StateFlow<Budget?> = repository.overallBudget
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allCategoryBudgets: StateFlow<List<Budget>> = repository.allCategoryBudgets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentMonthSelected = MutableStateFlow(Calendar.getInstance())
    val currentMonthSelected: StateFlow<Calendar> = _currentMonthSelected

    private fun getStartAndEndOfMonth(calendar: Calendar): Pair<Long, Long> {
        val cal = calendar.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59); cal.set(Calendar.MILLISECOND, 999)
        return Pair(start, cal.timeInMillis)
    }

    val currentMonthExpenses: StateFlow<List<Expense>> = _currentMonthSelected
        .flatMapLatest { cal ->
            val (s, e) = getStartAndEndOfMonth(cal)
            repository.getExpensesBetweenDates(s, e)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Only spending (isIncome == false)
    val currentMonthTotalSpent: StateFlow<Double> = currentMonthExpenses
        .map { list -> list.filter { !it.isIncome }.sumOf { it.amount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Only income (isIncome == true)
    val currentMonthTotalIncome: StateFlow<Double> = currentMonthExpenses
        .map { list -> list.filter { it.isIncome }.sumOf { it.amount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Net balance = income - spending
    val currentMonthNetBalance: StateFlow<Double> = combine(
        currentMonthTotalIncome, currentMonthTotalSpent
    ) { income, spent -> income - spent }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Category breakdowns (expenses only) for current month
    val categoryBreakdowns: StateFlow<Map<Int, Double>> = currentMonthExpenses
        .map { list ->
            list.filter { !it.isIncome }
                .groupBy { it.categoryId }
                .mapValues { e -> e.value.sumOf { it.amount } }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Data class for 6-month bar chart
    data class MonthSummary(val label: String, val income: Float, val spent: Float)

    // 6-month spending + income trend (always based on current real-time data)
    val sixMonthTrend: StateFlow<List<MonthSummary>> = flow {
        // Rebuild whenever allExpenses changes — observe raw all-expenses flow
        repository.allExpenses.collect { allExpenses ->
            val result = mutableListOf<MonthSummary>()
            val now = Calendar.getInstance()
            val monthFmt = java.text.SimpleDateFormat("MMM", java.util.Locale.getDefault())
            for (i in 5 downTo 0) {
                val cal = Calendar.getInstance()
                cal.add(Calendar.MONTH, -i)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59); cal.set(Calendar.MILLISECOND, 999)
                val end = cal.timeInMillis
                val monthExpenses = allExpenses.filter { it.timestamp in start..end }
                val totalIncome = monthExpenses.filter { it.isIncome }.sumOf { it.amount }.toFloat()
                val totalSpent = monthExpenses.filter { !it.isIncome }.sumOf { it.amount }.toFloat()
                val label = monthFmt.format(cal.time)
                result.add(MonthSummary(label, totalIncome, totalSpent))
            }
            emit(result)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun nextMonth() {
        val cal = _currentMonthSelected.value.clone() as Calendar
        cal.add(Calendar.MONTH, 1)
        _currentMonthSelected.value = cal
    }

    fun previousMonth() {
        val cal = _currentMonthSelected.value.clone() as Calendar
        cal.add(Calendar.MONTH, -1)
        _currentMonthSelected.value = cal
    }

    fun addExpense(amount: Double, categoryId: Int, title: String, note: String = "", isIncome: Boolean = false) {
        viewModelScope.launch {
            repository.insertExpense(
                Expense(
                    amount = amount,
                    categoryId = categoryId,
                    title = title,
                    note = note,
                    isIncome = isIncome,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch { repository.deleteExpense(expense) }
    }

    fun setOverallBudget(amount: Double) {
        viewModelScope.launch {
            val existing = overallBudget.value
            if (existing != null) {
                repository.updateBudget(existing.copy(limitAmount = amount))
            } else {
                val (s, e) = getStartAndEndOfMonth(_currentMonthSelected.value)
                repository.insertBudget(Budget(limitAmount = amount, startDate = s, endDate = e, categoryId = null))
            }
        }
    }

    fun setCategoryBudget(categoryId: Int, amount: Double) {
        viewModelScope.launch {
            val existing = allCategoryBudgets.value.find { it.categoryId == categoryId }
            if (existing != null) {
                repository.updateBudget(existing.copy(limitAmount = amount))
            } else {
                val (s, e) = getStartAndEndOfMonth(_currentMonthSelected.value)
                repository.insertBudget(Budget(limitAmount = amount, startDate = s, endDate = e, categoryId = categoryId))
            }
        }
    }
}

class FinanceViewModelFactory(private val repository: FinanceRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FinanceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FinanceViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
