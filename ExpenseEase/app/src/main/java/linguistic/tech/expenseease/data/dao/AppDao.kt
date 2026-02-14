package linguistic.tech.expenseease.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import linguistic.tech.expenseease.data.entity.Balance
import linguistic.tech.expenseease.data.entity.Salary
import linguistic.tech.expenseease.data.entity.SalaryHistory
import linguistic.tech.expenseease.data.entity.AppSettings

@Dao
interface AppDao {
    @Query("SELECT * FROM balance WHERE id = 1")
    fun getBalance(): Flow<Balance?>

    @Query("SELECT currentBalance FROM balance WHERE id = 1")
    suspend fun getBalanceValue(): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateBalance(balance: Balance)

    @Query("SELECT * FROM salary_config WHERE id = 1")
    fun getSalaryConfig(): Flow<Salary?>

    @Query("SELECT * FROM salary_config WHERE id = 1")
    suspend fun getSalaryConfigSync(): Salary?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateSalaryConfig(salary: Salary)

    @Query("SELECT * FROM salary_history WHERE monthYear = :monthYear LIMIT 1")
    suspend fun getSalaryHistory(monthYear: String): SalaryHistory?

    @Query("SELECT * FROM salary_history ORDER BY dateApplied DESC LIMIT 1")
    suspend fun getLastSalaryHistory(): SalaryHistory?

    @Insert
    suspend fun insertSalaryHistory(history: SalaryHistory)

    @Query("DELETE FROM balance")
    suspend fun deleteBalance()

    @Query("DELETE FROM salary_config")
    suspend fun deleteSalaryConfig()

    @Query("DELETE FROM salary_history")
    suspend fun deleteSalaryHistory()

    @Query("SELECT * FROM app_settings WHERE id = 1")
    fun getAppSettings(): Flow<AppSettings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateAppSettings(settings: AppSettings)
}
