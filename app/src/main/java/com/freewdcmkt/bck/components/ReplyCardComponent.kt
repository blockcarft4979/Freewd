package com.freewdcmkt.bck.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.freewdcmkt.bck.R

@Composable
fun ReplyInputBar(
    onSend: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf("") }
    val isSendEnabled = text.isNotBlank()

    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .weight(1f),
                maxLines = 5,
                placeholder = { Text(stringResource(R.string.reply_hint)) },
                //shape = RoundedCornerShape(16.dp) // 圆角输入框
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (isSendEnabled) {
                        onSend(text)
                        text = "" // 清空输入框
                    }
                },
                enabled = isSendEnabled,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Icon(
                    painterResource(R.drawable.baseline_send_24),
                    stringResource(R.string.send_hint),
                    modifier= Modifier.size(28.dp)
                )
                Text(stringResource(R.string.send_hint))
            }
        }
    }
}