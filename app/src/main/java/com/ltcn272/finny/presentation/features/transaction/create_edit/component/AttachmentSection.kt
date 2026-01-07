package com.ltcn272.finny.presentation.features.transaction.create_edit.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ltcn272.finny.R

@Preview
@Composable
fun AttachmentSectionPreview() {
    AttachmentSection(
        imageUri = null,
        isUploading = false,
        onChooseImageClick = {},
        onDeleteImageClick = {}
    )
}

@Composable
fun AttachmentSection(
    imageUri: String?,
    isUploading: Boolean,
    onChooseImageClick: () -> Unit,
    onDeleteImageClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.Image, null, tint = Color.Gray)
                    Text(
                        stringResource(R.string.attachments_image),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Normal
                    )
                }
                if (imageUri.isNullOrBlank()) {
                    Surface(
                        onClick = onChooseImageClick,
                        shape = RoundedCornerShape(8.dp),
                        color = Color.LightGray.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = stringResource(R.string.choose_image),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                } else {
                    TextButton(onClick = onDeleteImageClick) {
                        Text(stringResource(R.string.delete), color = Color.Red)
                    }
                }
            }

            Text(
                text = stringResource(R.string.choose_and_upload_invoice),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )

            AnimatedVisibility(visible = !imageUri.isNullOrBlank() || isUploading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .heightIn(min = 150.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.LightGray.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isUploading) {
                        CircularProgressIndicator()
                    } else if (!imageUri.isNullOrBlank()) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = stringResource(R.string.choose_image),
                            modifier = Modifier.fillMaxWidth(),
                            contentScale = ContentScale.FillWidth
                        )
                    }
                }
            }
        }
    }
}
