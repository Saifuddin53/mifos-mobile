package org.mifos.mobile.ui.savings_account_transaction


import android.content.Context
import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.mifos.mobile.MifosSelfServiceApp
import org.mifos.mobile.R
import org.mifos.mobile.core.ui.component.EmptyDataComponentWithModifiedMessageAndIcon
import org.mifos.mobile.core.ui.component.EmptyDataView
import org.mifos.mobile.core.ui.component.MFScaffold
import org.mifos.mobile.core.ui.component.MifosErrorComponent
import org.mifos.mobile.core.ui.component.MifosProgressIndicatorOverlay
import org.mifos.mobile.core.ui.theme.MifosMobileTheme
import org.mifos.mobile.models.CheckboxStatus
import org.mifos.mobile.models.accounts.savings.SavingsWithAssociations
import org.mifos.mobile.models.accounts.savings.TransactionType
import org.mifos.mobile.models.accounts.savings.Transactions
import org.mifos.mobile.utils.CheckBoxStatusUtil
import org.mifos.mobile.utils.CurrencyUtil
import org.mifos.mobile.utils.DateHelper.getDateAsString
import org.mifos.mobile.utils.Network
import org.mifos.mobile.utils.SavingsAccountUiState
import org.mifos.mobile.utils.Utils

@Composable
fun SavingsAccountTransactionScreen(
    viewModel: SavingAccountsTransactionViewModel = hiltViewModel(),
    navigateBack: () -> Unit,
) {

    val uiState by viewModel.savingAccountsTransactionUiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(key1 = Unit) {
        viewModel.setCheckboxStatesList(context)
    }

    SavingsAccountTransactionScreen(
        uiState = uiState,
        navigateBack = navigateBack,
        retryConnection = { viewModel.loadSavingsWithAssociations(viewModel.savingsId) },
    )
}

@Composable
fun SavingsAccountTransactionScreen(
    uiState: SavingsAccountTransactionUiState,
    navigateBack: () -> Unit,
    retryConnection: () -> Unit,
) {
    val context = LocalContext.current
    var transactionList by rememberSaveable { mutableStateOf(listOf<Transactions>()) }
    var isDialogOpen by rememberSaveable { mutableStateOf(false) }

    MFScaffold(
        topBar = {
            SavingAccountsTransactionTopBar(
                navigateBack = navigateBack,
                openFilterDialog = { isDialogOpen = true }
            )
        },
        scaffoldContent = { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues = paddingValues)) {
                when (uiState) {
                    is SavingsAccountTransactionUiState.Loading -> {
                        MifosProgressIndicatorOverlay()
                    }

                    is SavingsAccountTransactionUiState.Error -> {
                        MifosErrorComponent(
                            isNetworkConnected = Network.isConnected(context),
                            isEmptyData = false,
                            isRetryEnabled = true,
                            onRetry = retryConnection
                        )
                    }

                    is SavingsAccountTransactionUiState.Success -> {
                        if (uiState.savingAccountsTransactionList.isNullOrEmpty()) {
                            EmptyDataView(
                                icon = R.drawable.ic_compare_arrows_black_24dp,
                                error = R.string.no_transaction_found
                            )
                        } else {
                            transactionList = uiState.savingAccountsTransactionList
                            SavingsAccountTransactionContent(transactionList = transactionList)
                        }
                    }
                }
            }
        }
    )

    if (isDialogOpen) {
//        SavingAccountsTransactionFilterDialog(
//            filterByDateAndType = { startDate, endDate, statusModelList ->
//                val transactionListToFilter = filterSavingsAccountTransactionsByType(
//                    statusModelList,
//                    viewModel,
//                    transactionList,
//                    context
//                )
//                Toast.makeText(context, R.string.filtered, Toast.LENGTH_SHORT).show()
//                viewModel.filterTransactionList(
//                    transactionListToFilter,
//                    startDate,
//                    endDate,
//                )
//            },
//            filterByDate = { startDate, endDate ->
//                Toast.makeText(context, R.string.filtered, Toast.LENGTH_SHORT).show()
//                viewModel.filterTransactionList(
//                    transactionList,
//                    startDate,
//                    endDate,
//                )
//            },
//            filterByType = { statusModelList ->
//                val transactionListToFilter = filterSavingsAccountTransactionsByType(
//                    statusModelList,
//                    viewModel,
//                    transactionList,
//                    context
//                )
//                Toast.makeText(context, R.string.filtered, Toast.LENGTH_SHORT).show()
//                viewModel.filterTransactionList(
//                    transactionListToFilter,
//                    null,
//                    null,
//                )
//            }
//        )
    }
}

@Composable
fun SavingsAccountTransactionContent(
    transactionList: List<Transactions>,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        LazyColumn {
            items(items = transactionList) {
                SavingsAccountTransactionListItem(it)
                HorizontalDivider(
                    thickness = 1.dp,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 3.dp)
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = stringResource(id = R.string.need_help),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(id = R.string.help_line_number),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}


@Composable
fun SavingsAccountTransactionListItem(transaction: Transactions) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Image(
            painter = painterResource(
                id = getTransactionTriangleResId(transaction.transactionType)
            ),
            contentDescription = stringResource(id = R.string.savings_account_transaction),
            modifier = Modifier.size(56.dp).padding(4.dp)
        )
        Column(
            modifier = Modifier.padding(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = getDateAsString(transaction.date),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(
                        id = R.string.string_and_string,
                        transaction.currency?.displaySymbol ?: transaction.currency?.code ?: "",
                        CurrencyUtil.formatCurrency(context, transaction.amount,)
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = transaction.transactionType?.value ?: "",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.alpha(0.7f),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(
                        id = R.string.string_and_string,
                        transaction.currency?.displaySymbol ?: transaction.currency?.code ?: "",
                        CurrencyUtil.formatCurrency(context, transaction.runningBalance)
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.alpha(0.7f),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = transaction.paymentDetailData?.paymentType?.name.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.alpha(0.7f),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

private fun filterSavingsAccountTransactionsByType(
    statusModelList: List<CheckboxStatus?>?,
    viewModel: SavingAccountsTransactionViewModel,
    transactionsList: List<Transactions?>,
    context: Context?
): List<Transactions?> {
    val filteredSavingsTransactions: MutableList<Transactions?> = ArrayList()
    for (status in viewModel.getCheckedStatus(statusModelList)!!) {
        viewModel
            .filterTransactionListByType(
                transactionsList,
                status,
                getCheckBoxStatusStrings(context)
            )
            ?.let { filteredSavingsTransactions.addAll(it) }
    }
    return filteredSavingsTransactions
}

private fun getCheckBoxStatusStrings(context: Context?): CheckBoxStatusUtil {
    return CheckBoxStatusUtil().apply {
        this.depositString = context?.getString(R.string.deposit)
        this.dividendPayoutString = context?.getString(R.string.dividend_payout)
        this.withdrawalString = context?.getString(R.string.withdrawal)
        this.interestPostingString = context?.getString(R.string.interest_posting)
        this.feeDeductionString = context?.getString(R.string.fee_deduction)
        this.withdrawalTransferString = context?.getString(R.string.withdrawal_transfer)
        this.rejectedTransferString = context?.getString(R.string.rejected_transfer)
        this.overdraftFeeString = context?.getString(R.string.overdraft_fee)
    }
}

class SavingsAccountTransactionUiStatesParameterProvider :
    PreviewParameterProvider<SavingsAccountTransactionUiState> {
    override val values: Sequence<SavingsAccountTransactionUiState>
        get() = sequenceOf(
            SavingsAccountTransactionUiState.Success(listOf()),
            SavingsAccountTransactionUiState.Error(""),
            SavingsAccountTransactionUiState.Loading,
        )
}


@Preview(showSystemUi = true)
@Composable
fun SavingsAccountTransactionScreenPreview(
    @PreviewParameter(SavingsAccountTransactionUiStatesParameterProvider::class) savingsAccountUiState: SavingsAccountTransactionUiState
) {
    MifosMobileTheme {
        SavingsAccountTransactionScreen(
            uiState = savingsAccountUiState,
            navigateBack = {},
            retryConnection = {}
        )
    }
}

