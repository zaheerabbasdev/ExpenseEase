package linguistic.tech.expenseease

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import linguistic.tech.expenseease.databinding.FragmentSecondBinding
import linguistic.tech.expenseease.ui.MainViewModel
import linguistic.tech.expenseease.util.ExportUtils
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.salaryConfig.observe(viewLifecycleOwner) { salary ->
            if (salary != null) {
                binding.etSalaryAmount.setText(salary.amount.toString())
                binding.etBillingDay.setText(salary.billingDay.toString())
            }
        }

        val currencies = listOf("$", "Rs", "€", "£", "SAR", "AED", "¥", "₹")
        val adapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, currencies)
        binding.actvCurrency.setAdapter(adapter)

        viewModel.currencySymbol.observe(viewLifecycleOwner) { symbol ->
            binding.actvCurrency.setText(symbol, false)
        }

        binding.actvCurrency.setOnItemClickListener { _, _, position, _ ->
            viewModel.updateCurrency(currencies[position])
        }

        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            binding.cgSettingsCategories.removeAllViews()
            categories.forEach { category ->
                val chip = Chip(requireContext()).apply {
                    text = category.name
                    isCloseIconVisible = true
                    setOnCloseIconClickListener { viewModel.deleteCategory(category) }
                }
                binding.cgSettingsCategories.addView(chip)
            }
        }

        binding.etBillingDay.setOnClickListener {
            val calendar = java.util.Calendar.getInstance()
            val datePickerDialog = android.app.DatePickerDialog(
                requireContext(),
                { _, _, _, dayOfMonth ->
                    binding.etBillingDay.setText(dayOfMonth.toString())
                },
                calendar.get(java.util.Calendar.YEAR),
                calendar.get(java.util.Calendar.MONTH),
                calendar.get(java.util.Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        binding.btnSaveSalary.setOnClickListener {
            val amount = binding.etSalaryAmount.text.toString().toDoubleOrNull()
            val day = binding.etBillingDay.text.toString().toIntOrNull()

            if (amount != null && day != null) {
                viewModel.updateSalary(amount, day)
            }
        }

        binding.btnUpdateBalance.setOnClickListener {
            val amount = binding.etManualBalance.text.toString().toDoubleOrNull()

            if (amount != null) {
                viewModel.updateBalance(amount)
                binding.etManualBalance.text?.clear()
            }
        }

        binding.btnAddCategory.setOnClickListener {
            val name = binding.etCategoryName.text.toString()

            if (name.isNotEmpty()) {
                viewModel.addCategory(name)
                binding.etCategoryName.text?.clear()
            }
        }

        binding.btnExport.setOnClickListener {
            viewModel.categories.observe(viewLifecycleOwner) { categories ->
                val categoryMap = categories.associate { it.id to it.name }
                viewModel.allExpenses.observe(viewLifecycleOwner) { expenses ->
                    if (expenses.isNotEmpty()) {
                        ExportUtils.exportExpensesToCSV(requireContext(), expenses, categoryMap)
                    }
                }
            }
        }

        binding.btnClearData.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Clear All Data")
                .setMessage("Are you sure you want to delete all expenses, categories, and settings? This action cannot be undone.")
                .setPositiveButton("Clear Everything") { _, _ ->
                    viewModel.clearAllData()
                    android.widget.Toast.makeText(requireContext(), "All data cleared", android.widget.Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
