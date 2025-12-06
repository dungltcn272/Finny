package com.ltcn272.finny.presentation.features.transation.create_transaction.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ltcn272.finny.R
import com.ltcn272.finny.domain.model.TransactionType
import com.ltcn272.finny.presentation.common.ui.CircleIconButton

@Composable
fun CreateTransactionHeader(
    onBack: () -> Unit,
    selectedType: TransactionType,
    onTypeSelected: (TransactionType) -> Unit,
    onSaveClick: () -> Unit,
    isSaving: Boolean
) {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        CircleIconButton(onClick = onBack, icon = R.drawable.ic_close)

        TransactionTypeSelector(
            modifier = Modifier.align(Alignment.Center),
            selectedType = selectedType,
            onTypeSelected = onTypeSelected
        )

        Surface(
            onClick = onSaveClick,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .height(32.dp),
            // Tối ưu: Disable khi đang lưu
            enabled = !isSaving,
            shape = CircleShape,
            color = Color.Black,
            contentColor = Color.White
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 15.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isSaving) "Saving..." else "Save",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}