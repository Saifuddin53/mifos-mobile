package org.mifos.mobile.ui.savings_account_transaction

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerFormatter
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.mifos.mobile.R
import org.mifos.mobile.models.CheckboxStatus
import org.mifos.mobile.utils.DateHelper
import org.mifos.mobile.utils.DateHelper.getDateAsStringFromLong
import org.mifos.mobile.utils.DatePick
import java.time.Instant

@Composable
fun SavingAccountsTransactionFilterDialog(
    viewModel: SavingAccountsTransactionViewModel,
    filterByDateAndType: (Long?, Long?, List<CheckboxStatus?>?) -> Unit,
    filterByDate: (Long?, Long?) -> Unit,
    filterByType: (List<CheckboxStatus?>?) -> Unit,
    context: Context
) {

    val datePickerState = remember { mutableStateOf(false) }
    val datePick: MutableState<DatePick?> = remember {
        mutableStateOf(null)
    }

    val checkboxStates by viewModel.checkboxStates.collectAsState()
    val selectedCheckboxOptionIndex by viewModel.selectedCheckboxIndexList.collectAsState()

    val selectedOptionIndex by viewModel.selectedOptionIndex.collectAsState()

    AlertDialog(
        onDismissRequest = {  },
        confirmButton = {
            LazyColumn(
                modifier = Modifier.height(70.dp)
                    .fillMaxWidth()
                    .padding(0.dp,),
                horizontalAlignment = Alignment.End
            ) {
                item {
                    Button(
                        onClick = {
                             if(viewModel.transactionPeriodCheck.value && selectedCheckboxOptionIndex.isNotEmpty()) {
                                 when (selectedOptionIndex) {
                                     2 -> {
                                         viewModel.setStartDate(DateHelper.subtractWeeks(4))
                                         viewModel.setEndDate(System.currentTimeMillis())
                                     }
                                     3 -> {
                                         viewModel.setStartDate(DateHelper.subtractMonths(3))
                                         viewModel.setEndDate(System.currentTimeMillis())
                                     }
                                     4 -> {
                                         viewModel.setStartDate(DateHelper.subtractMonths(6))
                                         viewModel.setEndDate(System.currentTimeMillis())
                                     }
                                 }
                                 filterByDateAndType(
                                     viewModel.startDate.value,
                                     viewModel.endDate.value,
                                     checkboxStates
                                 )
                                 viewModel.setDialogOpen(false)
                             }else if (viewModel.transactionPeriodCheck.value) {
                                 when (selectedOptionIndex) {
                                     2 -> {
                                         viewModel.setStartDate(DateHelper.subtractWeeks(4))
                                         viewModel.setEndDate(System.currentTimeMillis())
                                     }
                                     3 -> {
                                         viewModel.setStartDate(DateHelper.subtractMonths(3))
                                         viewModel.setEndDate(System.currentTimeMillis())
                                     }
                                     4 -> {
                                         viewModel.setStartDate(DateHelper.subtractMonths(6))
                                         viewModel.setEndDate(System.currentTimeMillis())
                                     }
                                 }
                                 filterByDate(
                                     viewModel.startDate.value,
                                     viewModel.endDate.value
                                 )
                                 viewModel.setDialogOpen(false)
                             }else if(selectedCheckboxOptionIndex.isNotEmpty()) {
                                 filterByType(checkboxStates)
                                 viewModel.setDialogOpen(false)
                             }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                    ) {
                        Text(text = stringResource(id = R.string.filter))
                    }
                    Button(
                        onClick = { viewModel.setDialogOpen(false) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(text = stringResource(id = R.string.cancel))
                    }
                    Button(
                        onClick = {
                            viewModel.setTransactionPeriodCheck(false)
                            viewModel.setSelectOptionIndex(0)
                            viewModel.setCheckboxStatesList(context)
                            viewModel.setStartDate(System.currentTimeMillis())
                            viewModel.setEndDate(System.currentTimeMillis())
                            viewModel.clearCheckboxIndexList()
                            viewModel.setDialogOpen(false)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(text = stringResource(id = R.string.clear_filters))
                    }
                }
            }
        },
        title = {
            Text(
                text = stringResource(id = R.string.select_you_want),
                fontSize = 14.sp
            )
        },
        text = {
            Box(modifier = Modifier) {
                SavingAccountsTransactionFilterDialogContent(
                    checkboxStates,
                    viewModel,
                    selectedCheckboxOptionIndex,
                    viewModel.radioGroup,
                    selectedOptionIndex,
                    datePickerState,
                    datePick,
                )

                if(datePickerState.value) {
                    DatePickerContent(
                        datePickerState = datePickerState,
                        datePick = datePick
                        ) {
                        if(datePick.value == DatePick.START) {
                            viewModel.setStartDate(it)
                            datePick.value = DatePick.END
                        }else   {
                            viewModel.setEndDate(it)
                        }
                    }
                }
            }
        },
        modifier = Modifier
            .height(580.dp)
            .padding(horizontal = 10.dp)
    )
}


@Composable
fun SavingAccountsTransactionFilterDialogContent(
    checkboxStates: List<CheckboxStatus?>?,
    viewModel: SavingAccountsTransactionViewModel,
    selectedCheckboxOptionIndex: List<Int>,
    radioGroup: List<String>,
    selectedOptionIndex: Int,
    datePickerState: MutableState<Boolean>,
    datePick: MutableState<DatePick?>
) {
    val selectedOption = radioGroup[selectedOptionIndex]

    val isStartEnable = selectedOptionIndex == 1
    val isEndEnable = datePick.value == DatePick.END

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 13.dp)
            ) {
                Row(
                    modifier = Modifier
                        .height(height = 20.dp)
                ) {
                    Checkbox(
                        checked = viewModel.transactionPeriodCheck.collectAsState().value,
                        onCheckedChange = {
                            viewModel.setTransactionPeriodCheck(!viewModel.transactionPeriodCheck.value)
                            viewModel.setSelectOptionIndex(if(viewModel.transactionPeriodCheck.value) 1 else 0)
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary,
                            checkmarkColor = MaterialTheme.colorScheme.onPrimary,
                            uncheckedColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.offset(x = (-15).dp, y = 0.dp)
                    )
                    Text(
                        text = stringResource(id = R.string.transaction_period),
                        modifier = Modifier.offset(x = (-18).dp, y = 0.dp)
                    )
                }
            }

            RadioButtonOption(
                text = (stringResource(id = R.string.date)),
                radioGroup = radioGroup,
                selectedOption = selectedOption,
                currentOptionIndex = 1,
                viewModel = viewModel
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 13.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .height(15.dp)
                        .clickable {
                            if (isStartEnable) {
                                datePickerState.value = true
                            }
                        }
                ) {
                    val contentColor = if (isStartEnable) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        Color.Gray
                    }
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "",
                        tint = contentColor,
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = getDateAsStringFromLong(viewModel.startDate.value ?: System.currentTimeMillis()),
                        color = contentColor,
                        fontSize = 12.sp
                    )
                }
                Row(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .height(15.dp)
                        .clickable {
                            if (isEndEnable) {
                                datePickerState.value = true
                            }
                        }
                ) {
                    val contentColor = if (isEndEnable) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        Color.Gray
                    }
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "",
                        tint = contentColor,
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = getDateAsStringFromLong(viewModel.endDate.value ?: System.currentTimeMillis()),
                        fontSize = 12.sp,
                        color = contentColor
                    )
                }
            }

            RadioButtonOption(
                text = (stringResource(id = R.string.four_weeks)),
                radioGroup = radioGroup,
                selectedOption = selectedOption,
                currentOptionIndex = 2,
                viewModel = viewModel
            )

            RadioButtonOption(
                text = (stringResource(id = R.string.three_months)),
                radioGroup = radioGroup,
                selectedOption = selectedOption,
                currentOptionIndex = 3,
                viewModel = viewModel
            )

            RadioButtonOption(
                text = (stringResource(id = R.string.six_months)),
                radioGroup = radioGroup,
                selectedOption = selectedOption,
                currentOptionIndex = 4,
                viewModel = viewModel
            )

            checkboxStates?.take(4)?.forEachIndexed { index, checkboxStatus ->
                CheckBoxButtonOption(
                    index = index,
                    selectedOptionIndex = selectedCheckboxOptionIndex,
                    checkboxStatus = checkboxStatus ?: CheckboxStatus("", 1, false)
                ) {
                    if(it) {
                        viewModel.addCheckboxIndex(index)
                    } else {
                        viewModel.removeCheckboxIndex(index)
                    }
                    viewModel.updateCheckboxStatesList(checkboxStatus?.copy(isChecked = it))
                }
            }
        }
    }
}

@Composable
fun RadioButtonOption(
    text: String,
    radioGroup: List<String>,
    selectedOption: String,
    currentOptionIndex: Int,
    viewModel: SavingAccountsTransactionViewModel
){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 13.dp)
    ) {
        Row(
            modifier = Modifier
                .height(height = 20.dp)
                .clickable {
                    viewModel.setTransactionPeriodCheck(true)
                    viewModel.setSelectOptionIndex(currentOptionIndex)
                }
        ) {
            RadioButton(
                selected =  radioGroup[currentOptionIndex] == selectedOption ,
                onClick = {
                    viewModel.setTransactionPeriodCheck(true)
                    viewModel.setSelectOptionIndex(currentOptionIndex)
                },
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary,
                ),
                modifier = Modifier.offset(x = (-15).dp, y = 0.dp)
            )
            Text(
                text = text,
                modifier = Modifier.offset(x = (-18).dp, y = 0.dp)
            )
        }
    }
}

@Composable
fun CheckBoxButtonOption(
    index: Int,
    checkboxStatus: CheckboxStatus,
    selectedOptionIndex: List<Int>,
    onCheckedChange: (Boolean) -> Unit
){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .height(height = 20.dp)
        ) {
            Checkbox(
                checked = selectedOptionIndex.contains(index),
                onCheckedChange = {
                    onCheckedChange(it)
                },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(checkboxStatus.color),
                    checkmarkColor = MaterialTheme.colorScheme.onPrimary,
                    uncheckedColor = Color(checkboxStatus.color)
                ),
                modifier = Modifier.offset(x = (-15).dp, y = 0.dp)
            )
            Text(
                text = checkboxStatus.status ?: "",
                modifier = Modifier.offset(x = (-18).dp, y = 0.dp)
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerContent(
    datePickerState: MutableState<Boolean>,
    datePick: MutableState<DatePick?>,
    selectedStartDate: (Long?) -> Unit
) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = Instant.now().toEpochMilli()
    )

    Column {
        DatePickerDialog(
            onDismissRequest = {
                datePickerState.value = false
            },
            confirmButton = {
                Row {
                    Button(
                        onClick = { datePickerState.value = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(text = "Cancel")
                    }
                    Button(
                        onClick = {
                            datePick.value = if(datePick.value == null) DatePick.START else DatePick.END
                            selectedStartDate(state.selectedDateMillis)
                            datePickerState.value = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(text = "Ok")
                    }
                }
            },
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            Column {
                DatePicker(
                    state = state,
                )
            }
        }
    }
}
