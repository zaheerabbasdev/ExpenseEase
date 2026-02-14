package linguistic.tech.expenseease.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey val id: Int = 1, // Singleton
    val currencySymbol: String = "$"
)
