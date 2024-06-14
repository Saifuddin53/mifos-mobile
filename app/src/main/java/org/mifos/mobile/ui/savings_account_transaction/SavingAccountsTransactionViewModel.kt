package org.mifos.mobile.ui.savings_account_transaction


import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Observable
import io.reactivex.functions.Predicate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import org.mifos.mobile.R
import org.mifos.mobile.models.CheckboxStatus
import org.mifos.mobile.models.accounts.savings.SavingsWithAssociations
import org.mifos.mobile.models.accounts.savings.TransactionType
import org.mifos.mobile.models.accounts.savings.Transactions
import org.mifos.mobile.models.templates.account.AccountOptionsTemplate
import org.mifos.mobile.models.templates.savings.SavingsAccountTemplate
import org.mifos.mobile.repositories.SavingsAccountRepository
import org.mifos.mobile.utils.CheckBoxStatusUtil
import org.mifos.mobile.utils.Constants
import org.mifos.mobile.utils.DateHelper
import org.mifos.mobile.utils.SavingsAccountUiState
import org.mifos.mobile.utils.StatusUtils
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class SavingAccountsTransactionViewModel @Inject constructor(private val savingsAccountRepositoryImp: SavingsAccountRepository) :
    ViewModel() {

    private val _savingAccountsTransactionUiState = MutableStateFlow<SavingsAccountTransactionUiState>(SavingsAccountTransactionUiState.Loading)
    val savingAccountsTransactionUiState: StateFlow<SavingsAccountTransactionUiState> get() = _savingAccountsTransactionUiState

    private var _savingsId: Long = 0
    val savingsId get() = _savingsId

    private val _isDialogOpen = MutableStateFlow(false)
    val isDialogOpen: StateFlow<Boolean> get() = _isDialogOpen

    private val _startDate =
        MutableStateFlow<Long?>(Instant.now().toEpochMilli())
    val startDate: StateFlow<Long?> get() = _startDate

    private val _endDate = MutableStateFlow<Long?>(Instant.now().toEpochMilli())
    val endDate: StateFlow<Long?> get() = _endDate

    val radioGroup = listOf("", "date", "fourWeeks", "threeMonths", "sixMonths")

    private val _selectedOptionIndex =
        MutableStateFlow(0)
    val selectedOptionIndex: StateFlow<Int> get() = _selectedOptionIndex

    private val _checkboxStates =
        MutableStateFlow<List<CheckboxStatus?>?>(null)

    val checkboxStates: StateFlow<List<CheckboxStatus?>?> get() = _checkboxStates

    private val _transactionPeriodCheck = MutableStateFlow(false)

    val transactionPeriodCheck: StateFlow<Boolean> get() = _transactionPeriodCheck

    private val _selectedCheckboxIndexList =
        MutableStateFlow<List<Int>>(emptyList())

    val selectedCheckboxIndexList: StateFlow<List<Int>> get() = _selectedCheckboxIndexList

    fun setSavingsId(savingsId: Long) {
        _savingsId = savingsId
        loadSavingsWithAssociations(savingsId)
    }
    /**
     * Filters [List] of [CheckboxStatus]
     * @param statusModelList [List] of [CheckboxStatus]
     * @return Returns [List] of [CheckboxStatus] which have
     * `checkboxStatus.isChecked()` as true.
     */
    fun getCheckedStatus(statusModelList: List<CheckboxStatus?>?): List<CheckboxStatus?>? {
        return Observable.fromIterable(statusModelList)
            .filter { (_, _, isChecked) -> isChecked }.toList().blockingGet()
    }

    /**
     * Load details of a particular saving account from the server and notify the view
     * to display it. Notify the view, in case there is any error in fetching
     * the details from server.
     *
     * @param accountId Id of Savings Account
     */
    fun loadSavingsWithAssociations(accountId: Long) {
        viewModelScope.launch {
            _savingAccountsTransactionUiState.value = SavingsAccountTransactionUiState.Loading
            savingsAccountRepositoryImp.getSavingsWithAssociations(
                accountId,
                Constants.TRANSACTIONS,
            ).catch {
                _savingAccountsTransactionUiState.value = SavingsAccountTransactionUiState.Error(it.message)
            }.collect {
                _savingAccountsTransactionUiState.value = SavingsAccountTransactionUiState.Success(it.transactions)
            }
        }
    }

    /**
     * Used for filtering [List] of [Transactions] according to `startDate` and
     * `lastDate`
     *
     * @param savingAccountsTransactionList [List] of [Transactions]
     * @param startDate                     Starting date for filtering
     * @param lastDate                      Last date for filtering
     */
    fun filterTransactionList(
        savingAccountsTransactionList: List<Transactions>,
        startDate: Long?,
        lastDate: Long?,
    ) {
        if (startDate == null && lastDate == null) {
            _savingAccountsTransactionUiState.value = SavingsAccountTransactionUiState.Success(savingAccountsTransactionList)
            return
        }
        val list = when {
            (startDate != null && lastDate != null) -> {
                Observable.fromIterable(savingAccountsTransactionList)
                    .filter { (_, _, _, _, date) ->
                        (DateHelper.getDateAsLongFromList(date) in startDate..lastDate)
                    }
                    .toList().blockingGet()
            }
            else -> null
        }
        _savingAccountsTransactionUiState.value = SavingsAccountTransactionUiState.Success(list)
    }

    /**
     * Filters [List] of [Transactions] according to [CheckboxStatus]
     * @param savingAccountsTransactionList [List] of filtered [Transactions]
     * @param status Used for filtering the [List]
     * @return Returns [List] of filtered [Transactions] according to the
     * `status` provided.
     */
    fun filterTransactionListByType(
        savingAccountsTransactionList: List<Transactions?>?,
        status: CheckboxStatus?,
        checkBoxStatusUtil: CheckBoxStatusUtil
    ): Collection<Transactions?>? {
        return Observable.fromIterable(savingAccountsTransactionList)
            .filter(
                Predicate { (_, transactionType) ->

                    when {
                        ((checkBoxStatusUtil.depositString?.let { status?.status?.compareTo(it) } == 0) && (transactionType?.deposit!!)) ->
                            return@Predicate true

                        ((checkBoxStatusUtil.dividendPayoutString?.let {
                            status?.status?.compareTo(
                                it
                            )
                        } == 0) && (transactionType?.dividendPayout!!)) ->
                            return@Predicate true

                        ((checkBoxStatusUtil.withdrawalString?.let { status?.status?.compareTo(it) } == 0) && (transactionType?.withdrawal!!)) ->
                            return@Predicate true

                        ((checkBoxStatusUtil.interestPostingString?.let {
                            status?.status?.compareTo(
                                it
                            )
                        } == 0) && (transactionType?.interestPosting!!)) ->
                            return@Predicate true

                        ((checkBoxStatusUtil.feeDeductionString?.let { status?.status?.compareTo(it) } == 0) && (transactionType?.feeDeduction!!)) ->
                            return@Predicate true

                        ((checkBoxStatusUtil.withdrawalTransferString?.let {
                            status?.status?.compareTo(
                                it
                            )
                        } == 0) && (transactionType?.withdrawTransfer!!)) ->
                            return@Predicate true

                        ((checkBoxStatusUtil.rejectedTransferString?.let {
                            status?.status?.compareTo(
                                it
                            )
                        } == 0) && (transactionType?.rejectTransfer!!)) ->
                            return@Predicate true

                        ((checkBoxStatusUtil.overdraftFeeString?.let { status?.status?.compareTo(it) } == 0) && (transactionType?.overdraftFee!!)) ->
                            return@Predicate true

                        else -> false
                    }
                },
            ).toList().blockingGet()
    }

    fun setDialogOpen(isOpen: Boolean) {
        _isDialogOpen.value = isOpen
    }

    fun setStartDate(startDate: Long?) {
        _startDate.value = startDate
    }

    fun setEndDate(endDate: Long?) {
        _endDate.value = endDate
    }

    fun setSelectOptionIndex(index: Int) {
        _selectedOptionIndex.value = index
    }

    fun setCheckboxStatesList(context: Context?) {
        _checkboxStates.value = StatusUtils.getSavingsAccountTransactionList(context)
    }

    fun updateCheckboxStatesList(status: CheckboxStatus?) {
        _checkboxStates.value = _checkboxStates.value?.map { checkboxStatus ->
            if (checkboxStatus?.status == status?.status) {
                checkboxStatus?.isChecked = status?.isChecked ?: false
            }
            checkboxStatus
        }
    }

    fun addCheckboxIndex(index: Int) {
        _selectedCheckboxIndexList.value += index
    }

    fun removeCheckboxIndex(index: Int) {
        _selectedCheckboxIndexList.value -= index
    }

    fun clearCheckboxIndexList() {
        _selectedCheckboxIndexList.value = emptyList()
    }

    fun setTransactionPeriodCheck(check: Boolean) {
        _transactionPeriodCheck.value = check
    }
}

sealed class SavingsAccountTransactionUiState {
    data object Loading: SavingsAccountTransactionUiState()
    data class Error(val errorMessage: String?): SavingsAccountTransactionUiState()
    data class Success(val savingAccountsTransactionList: List<Transactions>?): SavingsAccountTransactionUiState()
}


fun getTransactionTriangleResId(transactionType: TransactionType?): Int {
    return transactionType?.run {
        when {
            deposit == true -> R.drawable.triangular_green_view
            dividendPayout == true -> R.drawable.triangular_red_view
            withdrawal == true -> R.drawable.triangular_red_view
            interestPosting == true -> R.drawable.triangular_green_view
            feeDeduction == true -> R.drawable.triangular_red_view
            initiateTransfer == true -> R.drawable.triangular_red_view
            approveTransfer == true -> R.drawable.triangular_red_view
            withdrawTransfer == true -> R.drawable.triangular_red_view
            rejectTransfer == true -> R.drawable.triangular_green_view
            overdraftFee == true -> R.drawable.triangular_red_view
            else -> R.drawable.triangular_green_view
        }
    } ?: R.drawable.triangular_red_view
}