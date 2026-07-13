package com.example.skmaccount.data.repository

import com.example.skmaccount.data.dao.BudgetDao
import com.example.skmaccount.data.dao.CategoryDao
import com.example.skmaccount.data.dao.ExpenseDao
import com.example.skmaccount.data.model.Budget
import com.example.skmaccount.data.model.Category
import com.example.skmaccount.data.model.Expense
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class FinanceRepository(
    private val categoryDao: CategoryDao,
    private val expenseDao: ExpenseDao,
    private val budgetDao: BudgetDao
) {

    // Categories
    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()

    suspend fun insertCategory(category: Category) {
        withContext(Dispatchers.IO) { categoryDao.insertCategory(category) }
    }

    suspend fun deleteCategory(category: Category) {
        withContext(Dispatchers.IO) { categoryDao.deleteCategory(category) }
    }

    // Expenses
    val allExpenses: Flow<List<Expense>> = expenseDao.getAllExpenses()

    fun getExpensesBetweenDates(startDate: Long, endDate: Long): Flow<List<Expense>> {
        return expenseDao.getExpensesBetweenDates(startDate, endDate)
    }

    fun getTotalSpentBetweenDates(startDate: Long, endDate: Long): Flow<Double?> {
        return expenseDao.getTotalSpentBetweenDates(startDate, endDate)
    }

    suspend fun insertExpense(expense: Expense) {
        withContext(Dispatchers.IO) { expenseDao.insertExpense(expense) }
    }

    suspend fun updateExpense(expense: Expense) {
        withContext(Dispatchers.IO) { expenseDao.updateExpense(expense) }
    }

    suspend fun deleteExpense(expense: Expense) {
        withContext(Dispatchers.IO) { expenseDao.deleteExpense(expense) }
    }

    // Budgets
    val overallBudget: Flow<Budget?> = budgetDao.getOverallBudget()

    fun getCategoryBudget(categoryId: Int): Flow<Budget?> {
        return budgetDao.getCategoryBudget(categoryId)
    }

    val allCategoryBudgets: Flow<List<Budget>> = budgetDao.getAllCategoryBudgets()

    suspend fun insertBudget(budget: Budget) {
        withContext(Dispatchers.IO) { budgetDao.insertBudget(budget) }
    }

    suspend fun updateBudget(budget: Budget) {
        withContext(Dispatchers.IO) { budgetDao.updateBudget(budget) }
    }

    suspend fun deleteBudget(budget: Budget) {
        withContext(Dispatchers.IO) { budgetDao.deleteBudget(budget) }
    }
}
