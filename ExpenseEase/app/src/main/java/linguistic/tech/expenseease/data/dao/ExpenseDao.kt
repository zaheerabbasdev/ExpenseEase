package linguistic.tech.expenseease.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import linguistic.tech.expenseease.data.entity.Expense

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Query("SELECT SUM(amount) FROM expenses WHERE date >= :startTime")
    fun getTotalExpenseSince(startTime: Long): Flow<Double?>

    @Query("SELECT * FROM expenses WHERE id = :id LIMIT 1")
    suspend fun getExpenseById(id: Long): Expense?

    @Insert
    suspend fun insertExpense(expense: Expense)

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("DELETE FROM expenses")
    suspend fun deleteAllExpenses()
}
