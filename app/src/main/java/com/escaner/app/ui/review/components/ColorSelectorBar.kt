package com.escaner.app.ui.review.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.escaner.app.R
import com.escaner.app.data.model.ModoColor

@Composable
fun ColorSelectorBar(
    selectedMode: ModoColor,
    onSelectMode: (ModoColor) -> Unit,
    modifier: Modifier = Modifier
) {
    val modes = listOf(
        ModoColor.BLANCO_Y_NEGRO to stringResource(R.string.color_mode_bw),
        ModoColor.COLOR to stringResource(R.string.color_mode_color),
        ModoColor.ESCALA_GRISES to stringResource(R.string.color_mode_grayscale)
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        modes.forEach { (mode, label) ->
            val isSelected = mode == selectedMode

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                    )
                    .clickable { onSelectMode(mode) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    maxLines = 1
                )
            }
        }
    }
}
