package linguistic.tech.expenseease.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "salary_history")
data class SalaryHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val monthYear: String, // e.g., "MM-YYYY"
    val dateApplied: Long
)
