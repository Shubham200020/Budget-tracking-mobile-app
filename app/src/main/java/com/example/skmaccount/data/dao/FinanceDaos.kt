package com.example.skmaccount.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.skmaccount.data.model.Budget
import com.example.skmaccount.data.model.Category
import com.example.skmaccount.data.model.Expense
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCategory(category: Category)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(categories: List<Category>)

    @Delete
    fun deleteCategory(category: Category)
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE timestamp >= :startDate AND timestamp <= :endDate ORDER BY timestamp DESC")
    fun getExpensesBetweenDates(startDate: Long, endDate: Long): Flow<List<Expense>>

    @Query("SELECT SUM(amount) FROM expenses WHERE timestamp >= :startDate AND timestamp <= :endDate")
    fun getTotalSpentBetweenDates(startDate: Long, endDate: Long): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertExpense(expense: Expense)

    @Update
    fun updateExpense(expense: Expense)

    @Delete
    fun deleteExpense(expense: Expense)
}

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets WHERE categoryId IS NULL LIMIT 1")
    fun getOverallBudget(): Flow<Budget?>

    @Query("SELECT * FROM budgets WHERE categoryId = :categoryId LIMIT 1")
    fun getCategoryBudget(categoryId: Int): Flow<Budget?>

    @Query("SELECT * FROM budgets WHERE categoryId IS NOT NULL")
    fun getAllCategoryBudgets(): Flow<List<Budget>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBudget(budget: Budget)

    @Update
    fun updateBudget(budget: Budget)

    @Delete
    fun deleteBudget(budget: Budget)
}
