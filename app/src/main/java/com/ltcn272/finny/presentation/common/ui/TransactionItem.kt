package com.ltcn272.finny.presentation.common.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ltcn272.finny.R
import com.ltcn272.finny.domain.model.Transaction
import com.ltcn272.finny.domain.model.TransactionType
import com.ltcn272.finny.presentation.common.util.CategoryUtils
import java.text.NumberFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TransactionItem(
    transaction: Transaction,
    currency: String,
    modifier: Modifier = Modifier,
    onClick: (Transaction) -> Unit
) {

    val formatter = NumberFormat.getNumberInstance(Locale.getDefault())

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        onClick = { onClick(transaction) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val style = CategoryUtils.getStyle(transaction.category)

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(style.backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = style.icon,
                    contentDescription = transaction.category.name,
                    tint = style.color,
                    modifier = Modifier
                        .padding(10.dp)
                        .size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                val transactionTitle = "${transaction.category.name.replaceFirstChar { it.titlecase() }} - ${transaction.name}"
                Text(
                    text = transactionTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    modifier = Modifier.basicMarquee(),
                    overflow = TextOverflow.Ellipsis
                )

                val localDateTime = transaction.dateTime.withZoneSameInstant(ZoneId.systemDefault())
                val time = localDateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
                val description = transaction.description ?: ""
                val location = transaction.location

                val subtitleParts = mutableListOf<String>()
                if (time.isNotEmpty()) subtitleParts.add(time)
                if (description.isNotEmpty()) subtitleParts.add(description)

                val timeAndDescText = subtitleParts.joinToString(" - ")
                val locationText = "" + location?.name.let { if (it.isNullOrBlank()) "" else "· $it" }

                val fullSubtitleText = if (timeAndDescText.isNotEmpty() && locationText.isNotEmpty()) {
                    "$timeAndDescText $locationText"
                } else {
                    timeAndDescText + locationText.replaceFirst("· ", "")
                }


                if (fullSubtitleText.isNotBlank()) {
                    Text(
                        text = fullSubtitleText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        maxLines = 1,
                        modifier = Modifier.basicMarquee(),
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                val sign = if (transaction.type == TransactionType.OUTCOME) "-" else "+"
                val formattedAmount =
                    formatter.format(transaction.amount)

                Text(
                    text = "$sign$formattedAmount $currency",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    painter = painterResource(R.drawable.ic_right),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
