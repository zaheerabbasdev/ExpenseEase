package linguistic.tech.expenseease.data.repository

import linguistic.tech.expenseease.data.dao.AppDao
import linguistic.tech.expenseease.data.dao.CategoryDao
import linguistic.tech.expenseease.data.dao.ExpenseDao
import linguistic.tech.expenseease.data.entity.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.*

class ExpenseRepository(
    private val expenseDao: ExpenseDao,
    private val categoryDao: CategoryDao,
    private val appDao: AppDao
) {
    val allExpenses: Flow<List<Expense>> = expenseDao.getAllExpenses()
    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()
    val balance: Flow<Balance?> = appDao.getBalance()
    val salaryConfig: Flow<Salary?> = appDao.getSalaryConfig()
    
    // Default to "$" if settings not yet created
    val currencySymbol: Flow<String> = appDao.getAppSettings().map { it?.currencySymbol ?: "$" }

    fun getTotalExpenseSince(startTime: Long): Flow<Double?> = expenseDao.getTotalExpenseSince(startTime)

    suspend fun insertExpense(expense: Expense) {
        expenseDao.insertExpense(expense)
        // Update balance
        val currentBalance = appDao.getBalanceValue() ?: 0.0
        appDao.updateBalance(Balance(currentBalance = currentBalance - expense.amount))
    }

    suspend fun updateExpense(expense: Expense) {
        val oldExpense = expenseDao.getExpenseById(expense.id)
        if (oldExpense != null) {
            val diff = oldExpense.amount - expense.amount
            if (diff != 0.0) {
                 val currentBalance = appDao.getBalanceValue() ?: 0.0
                 appDao.updateBalance(Balance(currentBalance = currentBalance + diff))
            }
            expenseDao.updateExpense(expense)
        }
    }

    suspend fun deleteExpense(expense: Expense) {
        val currentBalance = appDao.getBalanceValue() ?: 0.0
        // Refund amount
        appDao.updateBalance(Balance(currentBalance = currentBalance + expense.amount))
        expenseDao.deleteExpense(expense)
    }

    suspend fun insertCategory(category: Category) = categoryDao.insertCategory(category)
    
    suspend fun deleteCategory(category: Category) = categoryDao.deleteCategory(category)

    suspend fun updateSalaryConfig(salary: Salary) = appDao.updateSalaryConfig(salary)
    
    suspend fun updateCurrency(symbol: String) = appDao.updateAppSettings(AppSettings(currencySymbol = symbol))

    suspend fun updateBalance(amount: Double) = appDao.updateBalance(Balance(currentBalance = amount))

    suspend fun checkAndApplySalary() {
        val config = appDao.getSalaryConfigSync() ?: return
        val currentCalendar = Calendar.getInstance()
        
        // Get the last applied salary date
        val lastHistory = appDao.getLastSalaryHistory()
        
        val checkCalendar = Calendar.getInstance()
        if (lastHistory != null) {
            checkCalendar.timeInMillis = lastHistory.dateApplied
            // Move to next month from last applied
            checkCalendar.add(Calendar.MONTH, 1)
        } else {
            // If no history, establish a starting point. 
            // If we just installed, maybe we shouldn't apply past salaries?
            // Let's assume we start checking from current month if no history exists.
            // Or if we want to be safe, just check current month.
            checkCalendar.time = currentCalendar.time
        }

        // Reset to billing day
        // We need to handle months with fewer days (e.g. billing day 31 in Feb)
        // logic: iterate month by month from checkCalendar to currentCalendar
        
        while (checkCalendar.get(Calendar.YEAR) < currentCalendar.get(Calendar.YEAR) || 
              (checkCalendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR) && 
               checkCalendar.get(Calendar.MONTH) <= currentCalendar.get(Calendar.MONTH))) {
               
            val monthYearFormat = SimpleDateFormat("MM-yyyy", Locale.getDefault())
            val monthYear = monthYearFormat.format(checkCalendar.time)
            
            // check if already applied (double check)
            if (appDao.getSalaryHistory(monthYear) == null) {
                // Check if we passed the billing day for this month being checked
                // If the month checked is BEFORE current month, we definitely passed it.
                // If it IS current month, we need to check dayOfMonth >= billingDay
                
                val isPastMonth = (checkCalendar.get(Calendar.YEAR) < currentCalendar.get(Calendar.YEAR)) ||
                                  (checkCalendar.get(Calendar.MONTH) < currentCalendar.get(Calendar.MONTH))
                                  
                val currentDay = currentCalendar.get(Calendar.DAY_OF_MONTH)
                val isCurrentMonthAndDayPassed = !isPastMonth && currentDay >= config.billingDay
                
                if (isPastMonth || isCurrentMonthAndDayPassed) {
                     val currentBalance = appDao.getBalanceValue() ?: 0.0
                     appDao.updateBalance(Balance(currentBalance = currentBalance + config.amount))
                     appDao.insertSalaryHistory(SalaryHistory(monthYear = monthYear, dateApplied = System.currentTimeMillis()))
                }
            }
            
            checkCalendar.add(Calendar.MONTH, 1)
        }
    }

    suspend fun clearAllData() {
        expenseDao.deleteAllExpenses()
        categoryDao.deleteAllCategories()
        appDao.deleteBalance()
        appDao.deleteSalaryConfig()
        appDao.deleteSalaryHistory()
    }

    // Helper extensions for non-flow access if needed (simplified for this example)
    private suspend fun AppDao.getBalanceValue(): Double? {
        // In a real app, use a proper way to get the latest value from Flow or a separate query
        return null // Placeholder, we'll implement proper sync/async balance management
    }
}
