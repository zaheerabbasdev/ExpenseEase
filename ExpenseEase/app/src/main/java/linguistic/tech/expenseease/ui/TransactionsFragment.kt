package linguistic.tech.expenseease.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import linguistic.tech.expenseease.MainActivity
import linguistic.tech.expenseease.databinding.FragmentTransactionsBinding

class TransactionsFragment : Fragment() {

    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            val categoryMap = categories.associate { it.id to it.name }
            viewModel.allExpenses.observe(viewLifecycleOwner) { expenses ->
                viewModel.currencySymbol.observe(viewLifecycleOwner) { symbol ->
                    val sortedExpenses = expenses.sortedByDescending { it.date }
                    binding.rvAllTransactions.apply {
                        layoutManager = LinearLayoutManager(requireContext())
                        adapter = TransactionAdapter(sortedExpenses, categoryMap, symbol) { expense ->
                            val bottomSheet = AddExpenseBottomSheet()
                            val bundle = Bundle().apply {
                                putLong("expense_id", expense.id)
                                putLong("category_id", expense.categoryId)
                                putDouble("amount", expense.amount)
                                putLong("date", expense.date)
                            }
                            bottomSheet.arguments = bundle
                            bottomSheet.show(parentFragmentManager, "AddExpenseBottomSheet")
                        }
                    }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
