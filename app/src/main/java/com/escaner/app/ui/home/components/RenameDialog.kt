package com.escaner.app.ui.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.escaner.app.R
import com.escaner.app.data.model.DocumentoPDF

@Composable
fun RenameDialog(
    documento: DocumentoPDF,
    nombresExistentes: List<String>,
    onConfirmRename: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var textValue by remember {
        mutableStateOf(documento.nombre.removeSuffix(".pdf"))
    }

    val trimmed = textValue.trim()
    val nombreCompleto = "$trimmed.pdf"

    val errorResId: Int? = when {
        trimmed.isEmpty() -> R.string.dialog_rename_error_empty
        !DocumentoPDF.validarNombreBase(trimmed) -> R.string.dialog_rename_error_invalid_chars
        nombresExistentes.any { it.equals(nombreCompleto, ignoreCase = true) && !it.equals(documento.nombre, ignoreCase = true) } -> R.string.dialog_rename_error_exists
        else -> null
    }

    val isValid = errorResId == null && trimmed != documento.nombre.removeSuffix(".pdf")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.dialog_rename_title))
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = textValue,
                    onValueChange = { textValue = it },
                    label = { Text(stringResource(R.string.dialog_rename_hint)) },
                    singleLine = true,
                    isError = errorResId != null,
                    supportingText = {
                        if (errorResId != null) {
                            Text(
                                text = stringResource(errorResId),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirmRename(trimmed) },
                enabled = isValid
            ) {
                Text(text = stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.action_cancel))
            }
        }
    )
}
