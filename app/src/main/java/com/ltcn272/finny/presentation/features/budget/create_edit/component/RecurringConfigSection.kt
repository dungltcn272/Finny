package com.ltcn272.finny.presentation.features.budget.create_edit.component

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ltcn272.finny.R
import com.ltcn272.finny.domain.model.RecurringIntervalUnit
import com.ltcn272.finny.presentation.common.ui.SegmentedControl
import com.ltcn272.finny.presentation.common.util.NumberCommaVisualTransformation
import com.ltcn272.finny.presentation.common.util.formatDate
import java.time.ZonedDateTime

@Composable
fun RecurringConfigSection(
    recurringTopupAmount: String,
    onRecurringTopupAmountChange: (String) -> Unit,
    intervalUnit: RecurringIntervalUnit,
    intervalUnitStrings: Map<RecurringIntervalUnit, String>,
    onIntervalSelected: (RecurringIntervalUnit) -> Unit,
    startDate: ZonedDateTime,
    onStartDateClick: () -> Unit,
    nextRunAt: ZonedDateTime?,
    modifier: Modifier = Modifier
) {
    val numberVisualTransformation = remember { NumberCommaVisualTransformation() }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BasicTextField(
            value = recurringTopupAmount,
            onValueChange = onRecurringTopupAmountChange,
            visualTransformation = numberVisualTransformation,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(Color.White, shape = RoundedCornerShape(16.dp)),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            textStyle = TextStyle(
                textAlign = TextAlign.Center,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
        ) { innerTextField ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (recurringTopupAmount.isEmpty()) {
                    Text(
                        text = stringResource(R.string.recurring_topup_amount),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        // --- SỬA CỠ CHỮ HINT ---
                        fontSize = 24.sp,
                        // -----------------------
                        fontWeight = FontWeight.Bold
                    )
                }
                innerTextField()
            }
        }

        // Các lựa chọn Khoảng thời gian và Ngày bắt đầu
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(horizontal = 10.dp, vertical = 16.dp)) {
                // Interval Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 6.dp)
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(stringResource(id = R.string.recurring_interval))
                }
                Spacer(Modifier.height(10.dp))
                SegmentedControl(
                    options = RecurringIntervalUnit.entries,
                    selected = intervalUnit,
                    onOptionClicked = onIntervalSelected,
                    titleForItem = { unit -> intervalUnitStrings[unit] ?: "" },
                    indicatorPadding = 2.dp
                )
                HorizontalDivider(
                    color = Color.Gray.copy(alpha = 0.1f),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 16.dp)
                )
                // Start Date Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 6.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onStartDateClick
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(stringResource(R.string.recurring_start_date))
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.LightGray.copy(alpha = 0.2f),
                    ) {
                        Text(
                            text = formatDate(startDate),
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .basicMarquee()
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
                if (nextRunAt != null) {
                    HorizontalDivider(
                        color = Color.Gray.copy(alpha = 0.1f),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 16.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Update,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(stringResource(R.string.next_top_up_on))
                        }
                        Text(
                            text = formatDate(nextRunAt),
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}
