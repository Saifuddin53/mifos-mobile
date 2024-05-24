package org.mifos.mobile.ui.savings_account_transaction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.mifos.mobile.core.ui.component.mifosComposeView
import org.mifos.mobile.ui.activities.SavingsAccountContainerActivity
import org.mifos.mobile.ui.fragments.base.BaseFragment
import org.mifos.mobile.utils.Constants


@AndroidEntryPoint
class SavingAccountsTransactionFragment: BaseFragment() {

    private val viewModel: SavingAccountsTransactionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as? SavingsAccountContainerActivity)?.hideToolbar()
        if (arguments != null) viewModel.setSavingsId(arguments?.getLong(Constants.SAVINGS_ID)!!)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        if (savedInstanceState == null) {
            viewModel.loadSavingsWithAssociations(viewModel.savingsId)
        }
        return mifosComposeView(requireContext()) {
            activity?.let {
                SavingsAccountTransactionScreen(
                    navigateBack = { activity?.supportFragmentManager?.popBackStack() },
                )
            }
        }
    }


//    private fun filterSavingsAccountTransactionsByType(statusModelList: List<CheckboxStatus?>?): List<Transactions?> {
//        val filteredSavingsTransactions: MutableList<Transactions?> = ArrayList()
//        for (status in viewModel
//            .getCheckedStatus(statusModelList)!!) {
//            viewModel
//                .filterTransactionListByType(transactionsList, status, getCheckBoxStatusStrings())
//                ?.let { filteredSavingsTransactions.addAll(it) }
//        }
//        return filteredSavingsTransactions
//    }

    companion object {
        fun newInstance(savingsId: Long?): SavingAccountsTransactionFragment {
            val fragment = SavingAccountsTransactionFragment()
            val args = Bundle()
            if (savingsId != null) args.putLong(Constants.SAVINGS_ID, savingsId)
            fragment.arguments = args
            return fragment
        }
    }
}
