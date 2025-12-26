package com.ltcn272.finny.presentation.features.transaction.create_edit.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarViewDay
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.ltcn272.finny.R
import com.ltcn272.finny.domain.model.RecurringIntervalUnit
import dev.muazkadan.switchycompose.ISwitch
import java.time.ZonedDateTime

@Composable
fun RecurringSection(
    isRecurring: Boolean,
    recurringStartDate: ZonedDateTime,
    recurringIntervalUnit: RecurringIntervalUnit,
    showIntervalPicker: Boolean,
    onRecurringToggled: (Boolean) -> Unit,
    onStartDateClick: () -> Unit,
    onIntervalClick: () -> Unit,
    onIntervalPickerDismiss: () -> Unit,
    onIntervalUnitSelected: (RecurringIntervalUnit) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.Sync,
                        null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        stringResource(R.string.recurring_transaction),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                ISwitch(
                    checked = isRecurring,
                    onCheckedChange = onRecurringToggled,
                    buttonHeight = 28.dp,
                    innerPadding = 3.dp
                )
            }
        }

        AnimatedVisibility(visible = isRecurring) {
            Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {
                Column {
                    DateSelectionRow(
                        icon = Icons.Default.CalendarViewDay,
                        label = stringResource(id = R.string.recurring_start_date),
                        date = recurringStartDate,
                        onClick = onStartDateClick
                    )
                    Box {
                        ValueSelectionRow(
                            icon = Icons.Default.Schedule,
                            label = stringResource(id = R.string.recurring_interval),
                            value = "1 ${
                                when (recurringIntervalUnit) {
                                    RecurringIntervalUnit.DAY -> stringResource(id = R.string.unit_day)
                                    RecurringIntervalUnit.WEEK -> stringResource(id = R.string.unit_week)
                                    RecurringIntervalUnit.MONTH -> stringResource(id = R.string.unit_month)
                                    RecurringIntervalUnit.YEAR -> stringResource(id = R.string.unit_year)
                                }
                            }",
                            onClick = onIntervalClick
                        )
                        RecurringIntervalPickerPopup(
                            expanded = showIntervalPicker,
                            onDismissRequest = onIntervalPickerDismiss,
                            selectedUnit = recurringIntervalUnit,
                            onUnitSelected = onIntervalUnitSelected,
                            offset = DpOffset(x = 0.dp, y = (56).dp)
                        )
                    }
                }
            }
        }
    }
}
