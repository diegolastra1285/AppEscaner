package com.escaner.app.ui.review.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.escaner.app.R
import com.escaner.app.data.model.PaginaDocumento

@Composable
fun ThumbnailStrip(
    paginas: List<PaginaDocumento>,
    selectedIndex: Int,
    onSelectPage: (Int) -> Unit,
    onDeletePage: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        itemsIndexed(paginas) { index, pagina ->
            val isSelected = index == selectedIndex

            val thumbnailBitmap = remember(pagina.archivoCapturaOriginal.absolutePath) {
                try {
                    val options = BitmapFactory.Options().apply { inSampleSize = 4 }
                    BitmapFactory.decodeFile(pagina.archivoCapturaOriginal.absolutePath, options)
                } catch (e: Exception) {
                    null
                }
            }

            Box(
                modifier = Modifier
                    .padding(end = 12.dp)
                    .size(width = 68.dp, height = 96.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(
                        width = if (isSelected) 3.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .background(Color.DarkGray)
                    .clickable { onSelectPage(index) }
            ) {
                if (thumbnailBitmap != null) {
                    Image(
                        bitmap = thumbnailBitmap.asImageBitmap(),
                        contentDescription = "Página ${pagina.indice}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Page number badge
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(20.dp)
                        .background(Color.Black.copy(alpha = 0.7f), CircleShape)
                        .align(Alignment.BottomStart),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${pagina.indice}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }

                // Delete trash button
                IconButton(
                    onClick = { onDeletePage(index) },
                    modifier = Modifier
                        .padding(2.dp)
                        .size(24.dp)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        .align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.action_delete),
                        tint = Color(0xFFFF6B6B),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
