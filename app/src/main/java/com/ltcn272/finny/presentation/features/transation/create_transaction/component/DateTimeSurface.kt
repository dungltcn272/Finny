package com.ltcn272.finny.presentation.features.transation.create_transaction.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ltcn272.finny.R
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun DateTimeSurface(
    dateTime: LocalDateTime,
    onDateTimeClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    val dateText = if (dateTime.toLocalDate().isEqual(LocalDate.now())) {
        "Today"
    } else {
        dateTime.format(DateTimeFormatter.ofPattern("d MMM yyyy"))
    }
    val timeText = dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))

    Surface(
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.LightGray),
        shadowElevation = 1.dp,
        modifier = modifier.height(48.dp),
        onClick = onDateTimeClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(painter = painterResource(R.drawable.ic_calendar), contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(
                text = dateText,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.weight(1f))

            Text(
                text = timeText,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
