package linguistic.tech.expenseease.ui.analytics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import linguistic.tech.expenseease.MainActivity
import linguistic.tech.expenseease.R
import linguistic.tech.expenseease.databinding.FragmentAnalyticsBinding

import androidx.fragment.app.activityViewModels
import linguistic.tech.expenseease.ui.MainViewModel
import android.widget.TextView
import android.widget.LinearLayout
import android.view.Gravity

class AnalyticsFragment : Fragment() {

    private var _binding: FragmentAnalyticsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalyticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.currencySymbol.observe(viewLifecycleOwner) { symbol ->
            viewModel.categories.observe(viewLifecycleOwner) { categories ->
                viewModel.allExpenses.observe(viewLifecycleOwner) { expenses ->
                    updateCategorySummary(categories, expenses, symbol)
                }
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                (activity as? MainActivity)?.showInterstitial {
                    isEnabled = false
                    requireActivity().onBackPressed()
                }
            }
        })
    }

    private fun updateCategorySummary(categories: List<linguistic.tech.expenseease.data.entity.Category>, expenses: List<linguistic.tech.expenseease.data.entity.Expense>, currencySymbol: String) {
        binding.llTopCategories.removeAllViews()
        
        if (expenses.isEmpty()) {
            val tv = TextView(requireContext()).apply {
                text = "No data available yet."
                alpha = 0.6f
            }
            binding.llTopCategories.addView(tv)
            binding.pieChart.clear()
            binding.pieChart.invalidate()
            return
        }

        val categorySpent = expenses.groupBy { it.categoryId }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second }

        // Setup Pie Chart
        val entries = ArrayList<com.github.mikephil.charting.data.PieEntry>()
        val colors = ArrayList<Int>()
        
        // Dynamic Colors from resources
        val materialColors = listOf(
            ContextCompat.getColor(requireContext(), R.color.primary),
            ContextCompat.getColor(requireContext(), R.color.primary_dark),
            ContextCompat.getColor(requireContext(), R.color.primary_light),
            ContextCompat.getColor(requireContext(), R.color.accent),
            ContextCompat.getColor(requireContext(), R.color.secondary),
            ContextCompat.getColor(requireContext(), R.color.tertiary)
        )

        categorySpent.forEachIndexed { index, (categoryId, amount) ->
            val category = categories.find { it.id == categoryId }
            val categoryName = category?.name ?: "Unknown"
            
            entries.add(com.github.mikephil.charting.data.PieEntry(amount.toFloat(), categoryName))
            colors.add(materialColors[index % materialColors.size])
            
            // Add list row as before
            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setPadding(0, 8, 0, 8)
            }

            val nameTv = TextView(requireContext()).apply {
                text = categoryName
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium)
            }

            val amountTv = TextView(requireContext()).apply {
                text = String.format("$currencySymbol%.2f", amount)
                setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyLarge)
                setTextColor(ContextCompat.getColor(requireContext(), R.color.primary)) // Use primary color
                gravity = Gravity.END
            }

            row.addView(nameTv)
            row.addView(amountTv)
            binding.llTopCategories.addView(row)
        }
        
        val dataSet = com.github.mikephil.charting.data.PieDataSet(entries, "")
        dataSet.colors = colors
        dataSet.valueTextColor = android.graphics.Color.WHITE
        dataSet.valueTextSize = 12f
        dataSet.sliceSpace = 2f
        
        val data = com.github.mikephil.charting.data.PieData(dataSet)
        data.setValueFormatter(com.github.mikephil.charting.formatter.PercentFormatter(binding.pieChart))
        
        binding.pieChart.apply {
            this.data = data
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(android.graphics.Color.TRANSPARENT)
            setTransparentCircleColor(android.graphics.Color.WHITE)
            setTransparentCircleAlpha(110)
            holeRadius = 50f
            transparentCircleRadius = 55f
            setDrawCenterText(true)
            centerText = "Expenses"
            setCenterTextSize(16f)
            setCenterTextColor(android.graphics.Color.WHITE)
            setUsePercentValues(true)
            legend.isEnabled = false // Hide legend to save space
            animateY(1000)
            invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
