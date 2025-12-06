package com.ltcn272.finny.presentation.features.transation.transaction_list.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ltcn272.finny.R
import com.ltcn272.finny.domain.model.DaySection
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

@Composable
fun DaySectionHeader(
    daySection: DaySection,
    onAdd: () -> Unit,
) {
    val context = LocalContext.current
    val today = LocalDate.now()
    val title = when (daySection.date) {
        today -> stringResource(id = R.string.today)
        today.minusDays(1) -> stringResource(id = R.string.yesterday)
        else -> daySection.date.format(DateTimeFormatter.ofPattern("EEE, MMM dd", Locale.getDefault()))
    }

    val totalAmountFormatted = formatAmount(daySection.totalAmount)
    val totalColor =
        if (daySection.totalAmount > 0) Color(0xFF10B981) else if (daySection.totalAmount < 0) Color(0xFFEF4444) else Color.Gray

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (daySection.totalAmount != 0.0) {
                Surface(
                    color = totalColor,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(999.dp),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = totalAmountFormatted,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.padding(
                            horizontal = 6.dp,
                            vertical = 1.dp
                        )
                    )
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                shape = androidx.compose.foundation.shape.CircleShape,
                modifier = Modifier
                    .size(20.dp),
                onClick = onAdd
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add),
                    contentDescription = stringResource(id = R.string.add_transaction),
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

private fun formatAmount(amount: Double): String {
    return String.format(Locale.getDefault(), "%,.0f VNĐ", abs(amount))
}