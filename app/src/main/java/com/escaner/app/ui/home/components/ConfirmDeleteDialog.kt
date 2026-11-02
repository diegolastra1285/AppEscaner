package com.escaner.app.ui.home.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.escaner.app.R
import com.escaner.app.data.model.DocumentoPDF

@Composable
fun ConfirmDeleteDialog(
    documento: DocumentoPDF,
    onConfirmDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.dialog_delete_title))
        },
        text = {
            Text(text = stringResource(R.string.dialog_delete_message, documento.nombre))
        },
        confirmButton = {
            TextButton(
                onClick = onConfirmDelete,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(text = stringResource(R.string.action_delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.action_cancel))
            }
        }
    )
}
