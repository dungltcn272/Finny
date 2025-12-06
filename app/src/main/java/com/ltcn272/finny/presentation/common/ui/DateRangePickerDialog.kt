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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
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
import com.ltcn272.finny.R
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun DateRangePickerDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (start: LocalDate, end: LocalDate) -> Unit
) {
    var startDate by remember { mutableStateOf<LocalDate?>(null) }
    var endDate by remember { mutableStateOf<LocalDate?>(null) }

    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(24) }
    val endMonth = remember { currentMonth.plusMonths(24) }

    val currentLocale = Locale.getDefault()
    val daysOfWeek = remember { daysOfWeek(firstDayOfWeek = DayOfWeek.from(java.time.temporal.WeekFields.of(currentLocale).firstDayOfWeek)) }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .wrapContentHeight()
                    .padding(vertical = 15.dp, horizontal = 5.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val calendarState = rememberCalendarState(
                    startMonth = startMonth,
                    endMonth = endMonth,
                    firstVisibleMonth = currentMonth,
                    firstDayOfWeek = daysOfWeek.first()
                )

                val monthYearFormatter = remember { DateTimeFormatter.ofPattern("MMMM yyyy", currentLocale) }
                Text(
                    text = calendarState.firstVisibleMonth.yearMonth.format(monthYearFormatter),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 20.dp),
                    fontSize = 16.sp
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
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
                        Day(day, startDate, endDate) { clickedDate ->
                            if (startDate == null || endDate != null) {
                                startDate = clickedDate
                                endDate = null
                            } else if (clickedDate < startDate!!) {
                                startDate = clickedDate
                            } else if (clickedDate == startDate!!) {
                                startDate = null
                                endDate = null
                            } else {
                                endDate = clickedDate
                            }
                        }
                    }
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 10.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text(stringResource(id = R.string.cancel))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            val finalStartDate = startDate
                            val finalEndDate = endDate ?: finalStartDate
                            if (finalStartDate != null) {
                                if (finalEndDate != null) {
                                    onConfirm(finalStartDate, finalEndDate)
                                }
                            }
                        },
                        enabled = startDate != null
                    ) {
                        Text(stringResource(id = R.string.ok))
                    }
                }
            }
        }
    }
}

@Composable
private fun Day(
    day: CalendarDay,
    startDate: LocalDate?,
    endDate: LocalDate?,
    onClick: (LocalDate) -> Unit
) {
    val date = day.date
    val isRangeStart = date == startDate
    val isRangeEnd = date == endDate
    val isSelected = isRangeStart || isRangeEnd
    val inRange = startDate != null && endDate != null && date > startDate && date < endDate
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
            if (inRange || isSelected) {
                val rangeBackgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                val shape: Shape = when {
                    (isRangeStart && endDate == null) || (isRangeStart && isRangeEnd) -> CircleShape
                    isRangeStart -> RoundedCornerShape(topStartPercent = 50, bottomStartPercent = 50)
                    isRangeEnd -> RoundedCornerShape(topEndPercent = 50, bottomEndPercent = 50)
                    inRange -> when {
                        date.dayOfWeek == DayOfWeek.MONDAY -> RoundedCornerShape(topStartPercent = 50, bottomStartPercent = 50)
                        date.dayOfWeek == DayOfWeek.SUNDAY -> RoundedCornerShape(topEndPercent = 50, bottomEndPercent = 50)
                        else -> RoundedCornerShape(0.dp)
                    }
                    else -> RectangleShape
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(rangeBackgroundColor, shape)
                )
            }

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
