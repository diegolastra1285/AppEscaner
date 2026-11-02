package com.escaner.app.ui.review.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.escaner.app.R

@Composable
fun ConfirmDiscardBatchDialog(
    pageCount: Int,
    onConfirmDiscard: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(text = stringResource(R.string.dialog_discard_batch_title))
        },
        text = {
            Text(text = stringResource(R.string.dialog_discard_batch_message, pageCount))
        },
        confirmButton = {
            TextButton(
                onClick = onConfirmDiscard,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(text = stringResource(R.string.action_discard))
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(text = stringResource(R.string.action_continue_scanning))
            }
        }
    )
}
