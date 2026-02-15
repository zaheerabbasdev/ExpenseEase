package linguistic.tech.expenseease.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "balance")
data class Balance(
    @PrimaryKey val id: Int = 1, // Only one balance record
    val currentBalance: Double
)
