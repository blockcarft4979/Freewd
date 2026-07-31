package com.freewdcmkt.bck.components


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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
    hintMsg1: String,
    hintMsg2: String
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss // 点击外部/返回键关闭
    ) {
        // 手动构建 Material 风格的卡片布局
        Card(
            shape = RoundedCornerShape(28.dp), // 标准对话框圆角
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp) // 左右留边距
        ) {
            Column(
                modifier = Modifier.padding(24.dp) // 内边距
            ) {
                // ---- 标题 ----
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                // ---- 内容描述 ----
                Text(
                    text = msg,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                // ---- 按钮区域（右对齐） ----
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 取消按钮
                    TextButton(onClick = onDismiss) {
                        Text(hintMsg1)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // 确认删除按钮（红色警示）
                    TextButton(
                        onClick =
                            onConfirm
                    ) {
                        Text(
                            text = hintMsg2,
                            color = Color.Red
                        )
                    }
                }
            }
        }
    }
}