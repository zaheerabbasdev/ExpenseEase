package linguistic.tech.expenseease.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "salary_config")
data class Salary(
    @PrimaryKey val id: Int = 1, // Only one salary config
    val amount: Double,
    val billingDay: Int // 1-31
)
