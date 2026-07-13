package com.example.skmaccount.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "budgets",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Budget(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val limitAmount: Double,
    val startDate: Long,
    val endDate: Long,
    @ColumnInfo(index = true)
    val categoryId: Int? = null // Null if it's an overall budget
)
