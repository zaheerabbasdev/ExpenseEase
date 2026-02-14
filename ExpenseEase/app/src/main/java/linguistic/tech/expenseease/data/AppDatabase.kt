package linguistic.tech.expenseease.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import linguistic.tech.expenseease.data.dao.AppDao
import linguistic.tech.expenseease.data.dao.CategoryDao
import linguistic.tech.expenseease.data.dao.ExpenseDao
import linguistic.tech.expenseease.data.entity.AppSettings
import linguistic.tech.expenseease.data.entity.Balance
import linguistic.tech.expenseease.data.entity.Category
import linguistic.tech.expenseease.data.entity.Expense
import linguistic.tech.expenseease.data.entity.Salary
import linguistic.tech.expenseease.data.entity.SalaryHistory

@Database(
    entities = [Expense::class, Category::class, Salary::class, Balance::class, SalaryHistory::class, AppSettings::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun categoryDao(): CategoryDao
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "expense_ease_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
