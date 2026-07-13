package com.example.skmaccount.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.skmaccount.data.dao.BudgetDao
import com.example.skmaccount.data.dao.CategoryDao
import com.example.skmaccount.data.dao.ExpenseDao
import com.example.skmaccount.data.model.Budget
import com.example.skmaccount.data.model.Category
import com.example.skmaccount.data.model.Expense
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Category::class, Expense::class, Budget::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Migration from v1 to v2: adds `note` and `isIncome` columns to expenses
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE expenses ADD COLUMN note TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE expenses ADD COLUMN isIncome INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "khatabook_database"
                )
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback())
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    val dao = database.categoryDao()
                    val defaultCategories = listOf(
                        Category(name = "Food", iconName = "ic_food", colorHex = "#FF5722"),
                        Category(name = "Transport", iconName = "ic_transport", colorHex = "#2196F3"),
                        Category(name = "Shopping", iconName = "ic_shopping", colorHex = "#E91E63"),
                        Category(name = "Utilities", iconName = "ic_utilities", colorHex = "#4CAF50"),
                        Category(name = "Entertainment", iconName = "ic_entertainment", colorHex = "#9C27B0"),
                        Category(name = "Health", iconName = "ic_health", colorHex = "#00BCD4"),
                        Category(name = "Salary", iconName = "ic_salary", colorHex = "#00E676"),
                        Category(name = "Other", iconName = "ic_other", colorHex = "#607D8B")
                    )
                    dao.insertAll(defaultCategories)
                }
            }
        }
    }
}
