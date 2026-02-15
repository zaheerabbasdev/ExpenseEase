package linguistic.tech.expenseease

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import linguistic.tech.expenseease.databinding.FragmentFirstBinding
import linguistic.tech.expenseease.ui.MainViewModel
import linguistic.tech.expenseease.ui.TransactionAdapter
import java.util.*

class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.currencySymbol.observe(viewLifecycleOwner) { symbol ->
            viewModel.balance.observe(viewLifecycleOwner) { balance ->
                binding.tvBalance.text = String.format(Locale.getDefault(), "$symbol%.2f", balance)
            }

            viewModel.dailyExpense.observe(viewLifecycleOwner) { expense ->
                binding.tvDailyExpense.text = String.format(Locale.getDefault(), "$symbol%.2f", expense)
            }

            viewModel.weeklyExpense.observe(viewLifecycleOwner) { expense ->
                binding.tvWeeklyExpense.text = String.format(Locale.getDefault(), "$symbol%.2f", expense)
            }

            viewModel.monthlyExpense.observe(viewLifecycleOwner) { expense ->
                binding.tvMonthlyExpense.text = String.format(Locale.getDefault(), "$symbol%.2f", expense)
            }

            viewModel.yearlyExpense.observe(viewLifecycleOwner) { expense ->
                binding.tvYearlyExpense.text = String.format(Locale.getDefault(), "$symbol%.2f", expense)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
