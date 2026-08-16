package com.freewdcmkt.bck.components.freewd


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FreewdDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    title: String,
    msg: String,
    cancelHint: String? = null,
    confirmHint: String? = null
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp) // 内边距
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = msg,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 取消按钮
                    if (cancelHint != null) TextButton(onClick = onDismiss) {
                        Text(cancelHint)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    if (confirmHint != null) TextButton(
                        onClick =
                            onConfirm
                    ) {
                        Text(
                            text = confirmHint,
                            color = Color.Red
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FreewdLoadingDialog(text: String) {
    BasicAlertDialog(
        onDismissRequest = { }
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Row(
                modifier = Modifier.padding(24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LoadingIndicator()
                Text(text)
            }
        }
    }
}

@Composable
fun FreewdEditDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    title: String,
    maxLength: Int? = null,
    hintMsg1: String?,
    hintMsg2: String?
) {
    val text = rememberSaveable() { mutableStateOf("") }

    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = text.value,
                    onValueChange = {
                        if (maxLength == null) text.value = it else {
                            if (it.length <= maxLength) {
                                text.value = it
                            }
                        }
                    },
                    maxLines = 1,

                    )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {

                    if (hintMsg1 != null) TextButton(onClick = onDismiss) { Text(hintMsg1) }

                    Spacer(modifier = Modifier.width(8.dp))

                    if (hintMsg2 != null) TextButton(
                        onClick = { onConfirm(text.value) },
                        enabled = text.value.isNotBlank()
                    ) { Text(hintMsg2) }

                }
            }
        }
    }
}