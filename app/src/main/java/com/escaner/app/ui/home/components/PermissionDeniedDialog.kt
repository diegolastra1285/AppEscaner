package com.escaner.app.ui.home.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.escaner.app.R

@Composable
fun PermissionDeniedDialog(
    onDismiss: () -> Unit,
    onGrantPermission: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.dialog_permission_camera_title))
        },
        text = {
            Text(
                text = stringResource(R.string.dialog_permission_camera_message),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(onClick = onGrantPermission) {
                Text(text = stringResource(R.string.action_open_settings))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.action_cancel))
            }
        }
    )
}
