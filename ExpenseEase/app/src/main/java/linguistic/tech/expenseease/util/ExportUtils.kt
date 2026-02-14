package linguistic.tech.expenseease.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import linguistic.tech.expenseease.data.entity.Expense
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

object ExportUtils {

    fun exportExpensesToCSV(context: Context, expenses: List<Expense>, categoryMap: Map<Long, String>) {
        val fileName = "expenses_${System.currentTimeMillis()}.csv"
        val file = File(context.cacheDir, fileName)
        
        try {
            val writer = FileWriter(file)
            writer.append("\"ID\",\"Category\",\"Amount\",\"Date\"\n")
            
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            
            for (expense in expenses) {
                val categoryName = categoryMap[expense.categoryId] ?: "Unknown"
                writer.append("\"${expense.id}\",")
                writer.append("\"${categoryName}\",")
                writer.append("\"${String.format("%.2f", expense.amount)}\",")
                writer.append("\"${sdf.format(Date(expense.date))}\"\n")
            }
            
            writer.flush()
            writer.close()
            
            shareFile(context, file)
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun shareFile(context: Context, file: File) {
        val contentUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        context.startActivity(Intent.createChooser(intent, "Export Expenses"))
    }
}
