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

    // Lấy Locale (khu vực) hiện tại của thiết bị
    val currentLocale = Locale.getDefault()
    // Lấy danh sách các ngày trong tuần dựa trên Locale
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
                    firstDayOfWeek = daysOfWeek.first() // Sử dụng ngày đầu tuần theo Locale
                )

                // Sử dụng DateTimeFormatter để định dạng tháng/năm theo Locale
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
                    // Lấy tên các ngày trong tuần theo Locale
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
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            val finalStartDate = startDate
                            val finalEndDate = endDate ?: finalStartDate
                            if (finalStartDate != null) {
                                // Đã sửa lỗi NullPointerException tiềm ẩn
                                if (finalEndDate != null) {
                                    onConfirm(finalStartDate, finalEndDate)
                                }
                            }
                        },
                        enabled = startDate != null
                    ) {
                        Text("OK")
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
            // Hợp nhất logic vẽ nền vào một khối duy nhất
            if (inRange || isSelected) {
                val rangeBackgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                val shape: Shape = when {
                    // Xử lý ngày chọn đơn hoặc khoảng chỉ có 1 ngày
                    (isRangeStart && endDate == null) || (isRangeStart && isRangeEnd) -> CircleShape
                    // Ngày bắt đầu của khoảng
                    isRangeStart -> RoundedCornerShape(topStartPercent = 50, bottomStartPercent = 50)
                    // Ngày kết thúc của khoảng
                    isRangeEnd -> RoundedCornerShape(topEndPercent = 50, bottomEndPercent = 50)
                    // Ngày đầu tuần trong khoảng
                    inRange && date.dayOfWeek == DayOfWeek.MONDAY -> RoundedCornerShape(topStartPercent = 50, bottomStartPercent = 50)
                    // Ngày cuối tuần trong khoảng
                    inRange && date.dayOfWeek == DayOfWeek.SUNDAY -> RoundedCornerShape(topEndPercent = 50, bottomEndPercent = 50)
                    // Các ngày khác trong khoảng
                    inRange -> RoundedCornerShape(0.dp)
                    // Mặc định an toàn
                    else -> RectangleShape
                }

                // Vẽ dải nền mờ cho tất cả các ngày trong khoảng, bao gồm cả start và end
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(rangeBackgroundColor, shape)
                )
            }

            // Vẽ viền cho ngày hôm nay (nếu nó không được chọn)
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

            // Vẽ nền đậm hình tròn đè lên trên cho ngày start và end
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                )
            }

            // Chữ số ngày
            Text(
                text = day.date.dayOfMonth.toString(),
                color = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    isToday -> MaterialTheme.colorScheme.primary // Chữ ngày hôm nay có màu primary
                    else -> MaterialTheme.colorScheme.onSurface
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
