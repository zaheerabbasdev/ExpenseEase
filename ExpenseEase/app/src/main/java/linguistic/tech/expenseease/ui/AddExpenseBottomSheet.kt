package linguistic.tech.expenseease.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import linguistic.tech.expenseease.data.entity.Category
import linguistic.tech.expenseease.databinding.BottomSheetAddExpenseBinding
import java.util.Calendar

class AddExpenseBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetAddExpenseBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()
    private var selectedDate: Calendar = Calendar.getInstance()
    private var editingExpenseId: Long? = null
    private var selectedCategory: Category? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetAddExpenseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Check for Edit Mode arguments
        arguments?.let { args ->
            if (args.containsKey("expense_id")) {
                editingExpenseId = args.getLong("expense_id")
                val amount = args.getDouble("amount")
                val startCategoryId = args.getLong("category_id")
                val date = args.getLong("date")

                binding.etAmount.setText(amount.toString())
                selectedDate.timeInMillis = date
                binding.etDate.setText(java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(date))
                binding.btnSaveExpense.text = "Update Transaction"
                
                binding.btnDeleteExpense.visibility = View.VISIBLE
                binding.btnDeleteExpense.setOnClickListener {
                    android.app.AlertDialog.Builder(requireContext())
                        .setTitle("Delete Expense")
                        .setMessage("Are you sure you want to delete this expense?")
                        .setPositiveButton("Delete") { _, _ ->
                            val originalAmount = args.getDouble("amount")
                            val originalCategoryId = args.getLong("category_id")
                            val originalDate = args.getLong("date")
                            
                            viewModel.deleteExpense(
                                linguistic.tech.expenseease.data.entity.Expense(
                                    id = editingExpenseId!!,
                                    categoryId = originalCategoryId,
                                    amount = originalAmount,
                                    date = originalDate
                                )
                            )
                            dismiss()
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
                
                // Note: Category selection needs to happen after categories are observed
            }
        } ?: run {
             // New Expense Mode: Set Date to Today's formatted date
             binding.etDate.setText(java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(selectedDate.time))
        }
        
        // Date Picker
        binding.etDate.setOnClickListener {
            val datePicker = android.app.DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    selectedDate.set(year, month, dayOfMonth)
                    binding.etDate.setText(java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(selectedDate.time))
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }

        viewModel.currencySymbol.observe(viewLifecycleOwner) { symbol ->
            binding.tilAmount.prefixText = symbol
        }

        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            val categoryNames = categories.map { it.name }
            val adapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categoryNames)
            binding.actvCategory.setAdapter(adapter)

            // If editing, select the category
            arguments?.let { args ->
                if (args.containsKey("category_id")) {
                    val catId = args.getLong("category_id")
                    val existingCategory = categories.find { it.id == catId }
                    if (existingCategory != null) {
                        selectedCategory = existingCategory
                        binding.actvCategory.setText(existingCategory.name, false)
                    }
                }
            }

            binding.actvCategory.setOnItemClickListener { _, _, position, _ ->
                selectedCategory = categories[position]
            }
        }

        binding.btnSaveExpense.setOnClickListener {
            val amount = binding.etAmount.text.toString().toDoubleOrNull()

            if (amount != null && selectedCategory != null) {
                if (editingExpenseId != null) {
                    // Update
                    viewModel.updateExpense(
                        linguistic.tech.expenseease.data.entity.Expense(
                            id = editingExpenseId!!,
                            categoryId = selectedCategory!!.id,
                            amount = amount,
                            date = selectedDate.timeInMillis
                        )
                    )
                } else {
                    // Create
                    viewModel.addExpense(selectedCategory!!.id, amount, selectedDate.timeInMillis)
                }
                dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
