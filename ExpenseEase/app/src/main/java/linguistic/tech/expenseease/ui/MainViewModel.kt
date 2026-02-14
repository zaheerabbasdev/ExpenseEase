package linguistic.tech.expenseease.ui

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import linguistic.tech.expenseease.data.AppDatabase
import linguistic.tech.expenseease.data.entity.Category
import linguistic.tech.expenseease.data.entity.Expense
import linguistic.tech.expenseease.data.entity.Salary
import linguistic.tech.expenseease.data.repository.ExpenseRepository
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ExpenseRepository

    val balance: LiveData<Double>
    val categories: LiveData<List<Category>>
    val salaryConfig: LiveData<Salary?>

    val dailyExpense: LiveData<Double>
    val weeklyExpense: LiveData<Double>
    val monthlyExpense: LiveData<Double>
    val yearlyExpense: LiveData<Double>
    val allExpenses: LiveData<List<Expense>>
    
    val currencySymbol: LiveData<String>

    init {
        val db = AppDatabase.getDatabase(application)
        repository = ExpenseRepository(db.expenseDao(), db.categoryDao(), db.appDao())

        balance = repository.balance.asLiveData().map { it?.currentBalance ?: 0.0 }
        categories = repository.allCategories.asLiveData()
        salaryConfig = repository.salaryConfig.asLiveData()

        dailyExpense = repository.getTotalExpenseSince(getStartOfDay()).asLiveData().map { it ?: 0.0 }
        weeklyExpense = repository.getTotalExpenseSince(getStartOfWeek()).asLiveData().map { it ?: 0.0 }
        monthlyExpense = repository.getTotalExpenseSince(getStartOfMonth()).asLiveData().map { it ?: 0.0 }
        yearlyExpense = repository.getTotalExpenseSince(getStartOfYear()).asLiveData().map { it ?: 0.0 }
        allExpenses = repository.allExpenses.asLiveData()
        currencySymbol = repository.currencySymbol.asLiveData()
        
        viewModelScope.launch {
            repository.checkAndApplySalary()
            
            // Initialize default categories if none exist
            repository.allCategories.first().let { currentCategories ->
                if (currentCategories.isEmpty()) {
                    val defaultCategories = listOf("Food", "Transport", "Shopping", "Bills", "Entertainment", "Other")
                    defaultCategories.forEach { name ->
                        addCategory(name)
                    }
                }
            }
        }
    }

    fun addExpense(categoryId: Long, amount: Double, date: Long = System.currentTimeMillis()) {
        viewModelScope.launch {
            val expense = Expense(
                categoryId = categoryId,
                amount = amount,
                date = date
            )
            repository.insertExpense(expense)
        }
    }

    fun updateExpense(expense: Expense) {
        viewModelScope.launch {
            repository.updateExpense(expense)
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    fun addCategory(name: String) {
        viewModelScope.launch {
            repository.insertCategory(Category(name = name))
        }
    }
    
    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }

    fun updateSalary(amount: Double, day: Int) {
        viewModelScope.launch {
            repository.updateSalaryConfig(Salary(amount = amount, billingDay = day))
        }
    }

    fun updateBalance(amount: Double) {
        viewModelScope.launch {
            val currentBalance = repository.balance.first()?.currentBalance ?: 0.0
            repository.updateBalance(currentBalance + amount)
        }
    }

    fun updateCurrency(symbol: String) {
        viewModelScope.launch {
            repository.updateCurrency(symbol)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            // Re-initialize default categories
            addCategory("Food")
            addCategory("Transport")
            addCategory("Shopping")
            addCategory("Bills")
            addCategory("Entertainment")
            addCategory("Other")
        }
    }

    private fun getStartOfDay(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun getStartOfWeek(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun getStartOfMonth(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun getStartOfYear(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_YEAR, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
