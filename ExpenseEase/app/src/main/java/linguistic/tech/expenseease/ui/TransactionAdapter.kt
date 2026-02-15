package linguistic.tech.expenseease.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import linguistic.tech.expenseease.R
import linguistic.tech.expenseease.data.entity.Expense
import java.text.SimpleDateFormat
import java.util.*

class TransactionAdapter(
    private var expenses: List<Expense>,
    private val categoryMap: Map<Long, String>,
    private val currencySymbol: String,
    private val onItemClick: (Expense) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val categoryTv: TextView = view.findViewById(R.id.tv_item_category)
        val dateTv: TextView = view.findViewById(R.id.tv_item_date)
        val amountTv: TextView = view.findViewById(R.id.tv_item_amount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val expense = expenses[position]
        holder.categoryTv.text = categoryMap[expense.categoryId] ?: "Unknown"
        holder.amountTv.text = String.format("-$currencySymbol%.2f", expense.amount)
        
        val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        holder.dateTv.text = sdf.format(Date(expense.date))
        
        holder.itemView.setOnClickListener { onItemClick(expense) }
    }

    override fun getItemCount() = expenses.size

    fun updateData(newExpenses: List<Expense>) {
        expenses = newExpenses
        notifyDataSetChanged()
    }
}
