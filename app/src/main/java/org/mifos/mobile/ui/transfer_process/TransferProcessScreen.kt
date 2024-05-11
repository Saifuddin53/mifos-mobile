package org.mifos.mobile.ui.transfer_process

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.mifos.mobile.R
import org.mifos.mobile.core.ui.component.MifosProgressIndicator
import org.mifos.mobile.core.ui.component.NoInternet
import org.mifos.mobile.models.payload.TransferPayload
import org.mifos.mobile.ui.savings_account_withdraw.ErrorComponent
import org.mifos.mobile.utils.MFErrorParser
import org.mifos.mobile.utils.Network
import org.mifos.mobile.utils.TransferUiState


@Composable
fun TransferProcessScreen(
    viewModel: TransferProcessViewModel = hiltViewModel(),
    cancel: () -> Unit
) {
    val uiState by viewModel.transferUiState.collectAsStateWithLifecycle()
    val payload by viewModel.transferPayload.collectAsStateWithLifecycle()

    TransferProcessScreen(
        uiState = uiState,
        transfer = { payload?.let { viewModel.makeTransfer(it) } },
        payload = payload,
        cancel = cancel
    )
}

@Composable
fun TransferProcessScreen(
    uiState: TransferUiState,
    payload: TransferPayload?,
    transfer: () -> Unit,
    cancel: () -> Unit
) {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {

        Box(modifier= Modifier.weight(1f)) {
            TransferProcessContent(payload = payload, transfer = transfer, cancel = cancel)

            when (uiState) {
                is TransferUiState.Loading -> {
                    MifosProgressIndicator(modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.7f))
                    )
                }

                is TransferUiState.TransferSuccess -> {
                    Toast.makeText(context, R.string.transferred_successfully, Toast.LENGTH_SHORT).show()
                }

                is TransferUiState.Error -> {
                    ErrorComponent(errorToast = MFErrorParser.errorMessage(uiState.errorMessage))
                }

                is TransferUiState.Initial -> Unit
            }
        }

    }
}

@Composable
fun ErrorComponent(
    errorToast: String?,
) {
    val context = LocalContext.current
    if (!Network.isConnected(context)) {
        NoInternet(
            icon = R.drawable.ic_portable_wifi_off_black_24dp,
            error = R.string.no_internet_connection,
            isRetryEnabled = false,
        )
    } else {
        LaunchedEffect(errorToast) {
            Toast.makeText(context, errorToast, Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
fun TransferProcessContent(
    payload: TransferPayload?,
    transfer: () -> Unit,
    cancel: () -> Unit
) {
    val navigateBack: (Boolean) -> Unit = {}
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        border = BorderStroke(1.dp, Color.LightGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = stringResource(id = R.string.amount),
                    color = MaterialTheme.colorScheme.primary)

                Text(text = payload?.transferAmount.toString())
            }
            Text(text = stringResource(id = R.string.transfer_from_savings),
                fontWeight = FontWeight(500),
                color = Color.Gray
            )

            Text(text = stringResource(id = R.string.pay_to),
                modifier = Modifier.padding(top = 8.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Text(text = payload?.fromAccountNumber.toString(),
                modifier = Modifier
                    .padding(top = 4.dp, bottom = 2.dp)
            )
            Divider()

            Text(text = stringResource(id = R.string.pay_from),
                modifier = Modifier.padding(top = 8.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Text(text = payload?.fromAccountNumber.toString(),
                modifier = Modifier
                    .padding(top = 4.dp, bottom = 2.dp)
            )
            Divider()

            Text(text = stringResource(id = R.string.date),
                modifier = Modifier.padding(top = 8.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Text(text = payload?.transferDate.toString(),
                modifier = Modifier
                    .padding(top = 4.dp, bottom = 2.dp)
            )
            Divider()

            Text(text = stringResource(id = R.string.remark),
                modifier = Modifier.padding(top = 8.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Text(text = payload?.transferDescription.toString(),
                modifier = Modifier
                    .padding(top = 4.dp, bottom = 2.dp)
            )
            Divider()

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(30.dp),
                ) {
                    Button(
                        onClick = {
                                  cancel()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(text = stringResource(id = R.string.cancel))
                    }
                    Button(
                        onClick = {
                            transfer()
                        }
                    ) {
                        Text(text = stringResource(id = R.string.transfer))
                    }
                }
            }
        }
    }
}

