package com.ltcn272.finny.presentation.common.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.nextMonth
import com.kizitonwose.calendar.core.previousMonth
import com.ltcn272.finny.R
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun FinnyDatePickerDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (date: LocalDate) -> Unit,
    initialDate: LocalDate? = null
) {
    var selectedDate by remember { mutableStateOf(initialDate) }
    val coroutineScope = rememberCoroutineScope()

    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(24) }
    val endMonth = remember { currentMonth.plusMonths(24) }
    val currentLocale = Locale.getDefault()
    val daysOfWeek =
        remember { daysOfWeek(firstDayOfWeek = DayOfWeek.from(java.time.temporal.WeekFields.of(currentLocale).firstDayOfWeek)) }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(vertical = 15.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val calendarState = rememberCalendarState(
                    startMonth = startMonth,
                    endMonth = endMonth,
                    firstVisibleMonth = initialDate?.let { YearMonth.from(it) } ?: currentMonth,
                    firstDayOfWeek = daysOfWeek.first()
                )

                val monthYearFormatter =
                    remember { DateTimeFormatter.ofPattern("MMMM yyyy", currentLocale) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CircleNavigationButton(
                        icon = Icons.AutoMirrored.Default.KeyboardArrowLeft,
                        onClick = {
                            coroutineScope.launch {
                                calendarState.animateScrollToMonth(calendarState.firstVisibleMonth.yearMonth.previousMonth)
                            }
                        }
                    )
                    Text(
                        text = calendarState.firstVisibleMonth.yearMonth.format(monthYearFormatter),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    CircleNavigationButton(
                        icon = Icons.AutoMirrored.Default.KeyboardArrowRight,
                        onClick = {
                            coroutineScope.launch {
                                calendarState.animateScrollToMonth(calendarState.firstVisibleMonth.yearMonth.nextMonth)
                            }
                        }
                    )
                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    for (dayOfWeek in daysOfWeek) {
                        Text(
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            text = dayOfWeek.getDisplayName(TextStyle.SHORT, currentLocale),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                HorizontalCalendar(
                    state = calendarState,
                    dayContent = { day ->
                        Day(day, selectedDate) { clickedDate ->
                            selectedDate = if (selectedDate == clickedDate) null else clickedDate
                        }
                    }
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onDismissRequest,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Gray.copy(alpha = 0.2f),
                            contentColor = Color.Gray.copy(alpha = 0.8f)
                        )
                    ) {
                        Text(
                            text = stringResource(id = R.string.cancel),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Button(
                        onClick = {
                            selectedDate?.let { onConfirm(it) }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50),
                            contentColor = Color.White
                        ),
                        enabled = selectedDate != null
                    ) {
                        Text(
                            text = stringResource(id = R.string.confirm),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Day(
    day: CalendarDay,
    selectedDate: LocalDate?,
    onClick: (LocalDate) -> Unit
) {
    val date = day.date
    val isSelected = date == selectedDate
    val isToday = date == LocalDate.now()
    val isVisible = day.position == DayPosition.MonthDate

    Box(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .aspectRatio(1f)
            .clickable(
                enabled = isVisible,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { if (isVisible) onClick(date) }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isVisible) {
            if (isToday && !isSelected) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                )
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                )
            }

            Text(
                text = day.date.dayOfMonth.toString(),
                color = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    isToday -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurface
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
