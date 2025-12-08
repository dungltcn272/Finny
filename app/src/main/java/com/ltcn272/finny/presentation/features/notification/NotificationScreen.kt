package com.ltcn272.finny.presentation.features.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ltcn272.finny.R
import com.ltcn272.finny.presentation.common.ui.CircleIconButton
import com.ltcn272.finny.presentation.features.notification.component.NotificationItem
import com.ltcn272.finny.presentation.features.notification.component.NotificationTab
import com.ltcn272.finny.presentation.theme.MainBackgroundBrush
import java.time.ZonedDateTime

data class Notification(
    val id: String,
    val icon: Int,
    val iconBackgroundColor: Color,
    val title: String,
    val content: String,
    val createdAt: ZonedDateTime,
    val isRead: Boolean
)

val fakeNotifications = listOf(
    Notification(
        "1",
        R.drawable.ic_trending_down,
        Color(0xFF4A8BFF),
        "Giao dịch mới",
        "Cà phê Highlands – 85.000 ₫",
        ZonedDateTime.now().minusMinutes(57).minusSeconds(36),
        false
    ),
    Notification(
        "2",
        R.drawable.ic_trending_up,
        Color(0xFFFD7F6B),
        "Vượt ngân sách Ăn uống",
        "Đã dùng 92% ngân sách tuần",
        ZonedDateTime.now().minusHours(2).minusMinutes(42),
        false
    ),
    Notification(
        "3",
        R.drawable.ic_calendar,
        Color(0xFF8A7BFF),
        "Morning Brief",
        "Hôm qua chi 620.000 ₫ - Còn lại 2.800.000 ₫",
        ZonedDateTime.now().minusHours(8).minusMinutes(42),
        true
    ),
    Notification(
        "4",
        R.drawable.ic_check,
        Color(0xFF28B485),
        "Đạt 50% mục tiêu tiết kiệm",
        "15 triệu / 30 triệu",
        ZonedDateTime.now().minusDays(3),
        true
    ),
    Notification(
        "5",
        R.drawable.ic_check,
        Color(0xFF28B485),
        "Đạt 100% mục tiêu tiết kiệm",
        "30 triệu / 30 triệu",
        ZonedDateTime.now().minusDays(5),
        true
    )
)


@Composable
fun NotificationScreen(
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf("Tất cả") }
    val unreadCount = fakeNotifications.count { !it.isRead }

    val notificationsToShow = if (selectedTab == "Tất cả") {
        fakeNotifications
    } else {
        fakeNotifications.filter { !it.isRead }
    }
    val groupedNotifications = notificationsToShow.groupBy { it.createdAt.toLocalDate() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                MainBackgroundBrush
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            CircleIconButton(
                onClick = onBack,
                icon = R.drawable.ic_left,
                modifier = Modifier.align(Alignment.CenterStart)
            )
            Text(
                "Thông báo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NotificationTab(
                text = "Tất cả",
                isSelected = selectedTab == "Tất cả",
                onClick = { selectedTab = "Tất cả" },
                modifier = Modifier.weight(1f)
            )
            NotificationTab(
                text = "Chưa đọc",
                count = unreadCount,
                isSelected = selectedTab == "Chưa đọc",
                onClick = { selectedTab = "Chưa đọc" },
                modifier = Modifier.weight(1f)
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            groupedNotifications.forEach { (date, notifications) ->
                item {
                    Text(
                        text = when {
                            date.isEqual(ZonedDateTime.now().toLocalDate()) -> "Hôm nay"
                            date.isEqual(
                                ZonedDateTime.now().toLocalDate().minusDays(1)
                            ) -> "Hôm qua"

                            else -> date.toString()
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                items(notifications, key = { it.id }) { notification ->
                    NotificationItem(
                        notification = notification,
                        modifier = Modifier.animateItem().padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}