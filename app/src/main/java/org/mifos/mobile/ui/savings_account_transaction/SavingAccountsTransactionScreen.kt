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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
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

@Composable
fun SavingsAccountTransactionScreen(
    viewModel: SavingAccountsTransactionViewModel = hiltViewModel(),
    navigateBack: () -> Unit,
) {

    val uiState by viewModel.savingAccountsTransactionUiState.collectAsStateWithLifecycle()
    val dialogState by viewModel.isDialogOpen.collectAsStateWithLifecycle()
    val context: Context = LocalContext.current
    viewModel.setCheckboxStatesList(context)

    SavingsAccountTransactionScreen(
        uiState = uiState,
        viewModel = viewModel,
        navigateBack = navigateBack,
        retryConnection = { viewModel.loadSavingsWithAssociations(viewModel.savingsId) },
        dialogState = dialogState,
        context = context
    )
}

@Composable
fun SavingsAccountTransactionScreen(
    uiState: SavingsAccountUiState,
    viewModel: SavingAccountsTransactionViewModel,
    navigateBack: () -> Unit,
    retryConnection: () -> Unit,
    dialogState: Boolean,
    context: Context
) {
    val savingAccount = rememberSaveable { mutableStateOf(SavingsWithAssociations()) }
    val transactionList: List<Transactions?> = savingAccount.value.transactions.toList()

    Column(modifier = Modifier.fillMaxSize()) {

        SavingAccountsTransactionTopBar(
            navigateBack = navigateBack,
            filterDialog = { viewModel.setDialogOpen(true) }
        )

        Box(modifier = Modifier.weight(1f)) {

            when (uiState) {
                is SavingsAccountUiState.SuccessLoadingSavingsWithAssociations -> {
                    if(uiState.savingAccount.transactions.isNotEmpty()) {
                        savingAccount.value = uiState.savingAccount
                        SavingsAccountTransactionContent(transactionList = transactionList)
                    } else {
                        MifosErrorComponent(isEmptyData = true)
                    }
                }

                is SavingsAccountUiState.Loading -> {
                    MifosProgressIndicatorOverlay()
                }

                is SavingsAccountUiState.Error -> {
                    MifosErrorComponent(
                        isNetworkConnected = Network.isConnected(context),
                        isEmptyData = false,
                        isRetryEnabled = true,
                        onRetry = retryConnection
                    )
                }

                is SavingsAccountUiState.ShowFilteredTransactionsList -> {
                    if(uiState.savingAccountsTransactionList != null) {
                        if(uiState.savingAccountsTransactionList.isNotEmpty()) {
                            SavingsAccountTransactionContent(
                                transactionList = uiState.savingAccountsTransactionList
                            )
                        }else {
                            EmptyDataComponentWithModifiedMessageAndIcon(isEmptyData = true,
                                message = stringResource(id = R.string.no_transaction_found),
                                icon = Icons.Filled.CompareArrows)
                        }
                    } else {
                        MifosErrorComponent(isEmptyData = true)
                    }
                }

                is SavingsAccountUiState.Initial -> Unit

                else -> Unit
            }
        }

        if(dialogState) {
            SavingAccountsTransactionFilterDialog(
                viewModel = viewModel,
                filterByDateAndType = { startDate, endDate, statusModelList ->
                    val transactionListToFilter = filterSavingsAccountTransactionsByType(
                        statusModelList,
                        viewModel,
                        transactionList,
                        context
                    )
                    Toast.makeText(context, R.string.filtered, Toast.LENGTH_SHORT).show()
                    viewModel.filterTransactionList(
                        transactionListToFilter,
                        startDate,
                        endDate,
                    )
                },
                filterByDate = { startDate, endDate ->
                    Toast.makeText(context, R.string.filtered, Toast.LENGTH_SHORT).show()
                    viewModel.filterTransactionList(
                        transactionList,
                        startDate,
                        endDate,
                    )
                },
                filterByType = { statusModelList ->
                    val transactionListToFilter = filterSavingsAccountTransactionsByType(
                        statusModelList,
                        viewModel,
                        transactionList,
                        context
                    )
                    Toast.makeText(context, R.string.filtered, Toast.LENGTH_SHORT).show()
                    viewModel.filterTransactionList(
                        transactionListToFilter,
                        null,
                        null,
                    )
                },
                context = context
            )
        }
    }
}



@Composable
fun SavingsAccountTransactionListItem(transaction: Transactions?) {
    val color = getColor(transaction?.transactionType)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Image(
            painter = painterResource(
                id = if(color == ColorSelect.GREEN) R.drawable.triangular_green_view
                else R.drawable.triangular_red_view),
            contentDescription = stringResource(id = R.string.savings_account_transaction),
            modifier = Modifier
                .size(55.dp)
                .padding(6.dp)
        )
        Column(
            modifier = Modifier.padding(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = getDateAsString(transaction?.date),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(
                        id = R.string.string_and_string,
                        transaction?.currency?.displaySymbol ?: transaction?.currency?.code ?: "",
                        CurrencyUtil.formatCurrency(
                            MifosSelfServiceApp.context ,
                            transaction?.amount,
                        )
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
                    text = transaction?.transactionType?.value ?: "",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.alpha(0.7f),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(
                        id = R.string.string_and_string,
                        transaction?.currency?.displaySymbol ?: transaction?.currency?.code ?: "",
                        CurrencyUtil.formatCurrency(
                            MifosSelfServiceApp.context ,
                            transaction?.runningBalance ,
                        )
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier
                        .alpha(0.7f),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = transaction?.paymentDetailData?.paymentType?.name.toString(),
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
    for (status in viewModel
        .getCheckedStatus(statusModelList)!!) {
        viewModel
            .filterTransactionListByType(transactionsList, status, getCheckBoxStatusStrings(context))
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



@Composable
fun SavingsAccountTransactionContent(
    transactionList: List<Transactions?>,
) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        LazyColumn {
            items(items = transactionList) {
                SavingsAccountTransactionListItem(it)
                Divider(
                    thickness = 1.dp,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 3.dp)
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Text(text = stringResource(id = R.string.need_help),
                color = MaterialTheme.colorScheme.onSurface)
            Text(text = stringResource(id = R.string.help_line_number), 
                color = MaterialTheme.colorScheme.primary)
        }
    }
}

private enum class ColorSelect {
    RED, GREEN
}

private fun getColor(transactionType: TransactionType?): ColorSelect {
    if (transactionType?.deposit == true) {
        return ColorSelect.GREEN
    }
    if (transactionType?.dividendPayout == true) {
        return ColorSelect.RED
    }
    if (transactionType?.withdrawal == true) {
        return ColorSelect.RED
    }
    if (transactionType?.interestPosting == true) {
        return ColorSelect.GREEN
    }
    if (transactionType?.feeDeduction == true) {
        return ColorSelect.RED
    }
    if (transactionType?.initiateTransfer == true) {
        return ColorSelect.RED
    }
    if (transactionType?.approveTransfer == true) {
        return ColorSelect.RED
    }
    if (transactionType?.withdrawTransfer == true) {
        return ColorSelect.RED
    }
    if (transactionType?.rejectTransfer == true) {
        return ColorSelect.GREEN
    }
    return if (transactionType?.overdraftFee == true) {
        ColorSelect.RED
    } else {
        ColorSelect.GREEN
    }
}

class SavingsAccountTransactionUiStatesParameterProvider :
    PreviewParameterProvider<SavingsAccountUiState> {
    override val values: Sequence<SavingsAccountUiState>
        get() = sequenceOf(
            SavingsAccountUiState.SuccessLoadingSavingsWithAssociations(SavingsWithAssociations()),
            SavingsAccountUiState.Error,
            SavingsAccountUiState.Error,
            SavingsAccountUiState.Loading,
        )
}


@Preview(showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SavingsAccountTransactionScreenPreview(
    @PreviewParameter(SavingsAccountTransactionUiStatesParameterProvider::class) savingsAccountUiState: SavingsAccountUiState
) {
    MifosMobileTheme {
        SavingsAccountTransactionScreen(
            viewModel = hiltViewModel(),
            navigateBack = {}
        )
    }
}

