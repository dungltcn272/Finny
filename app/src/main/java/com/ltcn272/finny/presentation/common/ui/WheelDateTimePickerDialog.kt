package com.ltcn272.finny.presentation.common.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.commandiron.wheel_picker_compose.WheelDateTimePicker
import com.ltcn272.finny.R
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Composable
fun WheelDateTimePickerDialog(
    startDateTime: LocalDateTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalDateTime) -> Unit
) {
    var snappedDateTime by remember { mutableStateOf(startDateTime) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.medium, color = Color.White) {
            Column(modifier = Modifier.padding(12.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(), contentAlignment = Alignment.Center
                ) {
                    WheelDateTimePicker(
                        startDateTime = startDateTime,
                        onSnappedDateTime = {
                            snappedDateTime = it
                        }
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(50), // Hình viên thuốc
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
                            onConfirm(snappedDateTime.truncatedTo(ChronoUnit.MINUTES))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(50), // Hình viên thuốc
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50), // Màu xanh lá cây
                            contentColor = Color.White
                        )
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

