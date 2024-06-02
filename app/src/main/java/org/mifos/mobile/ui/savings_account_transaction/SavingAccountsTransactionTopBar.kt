package org.mifos.mobile.ui.savings_account_transaction

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.mifos.mobile.R
import org.mifos.mobile.core.ui.theme.MifosMobileTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingAccountsTransactionTopBar(
    navigateBack: () -> Unit,
    filterDialog: () -> Unit
) {

    TopAppBar(
        modifier = Modifier,
        title = { Text(text = stringResource(id = R.string.savings_account_transaction)
            ,  fontSize = 18.sp) },
        navigationIcon = {
            IconButton(
                onClick = { navigateBack.invoke() }
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back Arrow",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        actions = {
            IconButton(onClick = { filterDialog() }) {
                Icon(
                    imageVector = Icons.Filled.FilterList,
                    contentDescription = "Menu",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    )
}

@Preview
@Composable
fun SavingAccountsTransactionTopBarPreview() {
    MifosMobileTheme {
        SavingAccountsTransactionTopBar({},{})
    }
}
