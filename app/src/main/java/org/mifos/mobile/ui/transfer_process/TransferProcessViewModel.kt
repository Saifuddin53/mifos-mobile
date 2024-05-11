package org.mifos.mobile.ui.transfer_process

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import org.mifos.mobile.models.payload.TransferPayload
import org.mifos.mobile.repositories.TransferRepository
import org.mifos.mobile.ui.enums.TransferType
import org.mifos.mobile.utils.TransferUiState
import javax.inject.Inject

@HiltViewModel
class TransferProcessViewModel @Inject constructor(private val transferRepositoryImp: TransferRepository) :
    ViewModel() {

    private val _transferUiState = MutableStateFlow<TransferUiState>(TransferUiState.Initial)
    val transferUiState: StateFlow<TransferUiState> get() = _transferUiState

    private var _transferPayload: MutableStateFlow<TransferPayload?> = MutableStateFlow(null)
    val transferPayload: StateFlow<TransferPayload?> get() = _transferPayload

    private var _transferType: MutableStateFlow<TransferType?> = MutableStateFlow(null)
    val transferType: StateFlow<TransferType?> get() = _transferType

    fun makeTransfer(payload: TransferPayload) {
        viewModelScope.launch {
            _transferUiState.value = TransferUiState.Loading
            transferRepositoryImp.makeTransfer(
                payload.fromOfficeId,
                payload.fromClientId,
                payload.fromAccountType,
                payload.fromAccountId,
                payload.toOfficeId,
                payload.toClientId,
                payload.toAccountType,
                payload.toAccountId,
                payload.transferDate,
                payload.transferAmount,
                payload.transferDescription,
                payload.dateFormat,
                payload.locale,
                payload.fromAccountNumber,
                payload.toAccountNumber,
                transferType.value
            ).catch { e ->
                _transferUiState.value = TransferUiState.Error(e)
            }.collect {
                _transferUiState.value = TransferUiState.TransferSuccess
            }
        }
    }

    fun setContent(payload: TransferPayload) {
        _transferPayload.value = payload
    }

    fun setTransferType(transferType: TransferType) {
        _transferType.value = transferType
    }

}