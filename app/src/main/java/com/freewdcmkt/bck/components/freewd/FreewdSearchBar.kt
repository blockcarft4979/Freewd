package com.freewdcmkt.bck.components.freewd

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.freewdcmkt.bck.R

@Composable
fun FreewdSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    onSearch: () -> Unit,
   // onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onClear: () -> Unit = { onValueChange("") }
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 返回
//        IconButton(onClick = onBack) {
//            Icon(
//                painter = painterResource(R.drawable.baseline_arrow_back_24),
//                contentDescription = null,
//                tint = MaterialTheme.colorScheme.onSurface
//            )
//        }

        // 输入框
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = MaterialTheme.typography.bodyLarge.fontSize
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            modifier = Modifier.weight(1f),
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text(
                        text = hint,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize
                    )
                }
                innerTextField()
            }
        )

        // 清除（有内容时才显示）
        if (value.isNotBlank()) {
            IconButton(onClick = onClear) {
                Icon(
                    painter = painterResource(R.drawable.baseline_close_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 搜索
        IconButton(onClick = onSearch) {
            Icon(
                painter = painterResource(R.drawable.baseline_search_24),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
//    HorizontalDivider(
//        color = MaterialTheme.colorScheme.outlineVariant,
//        thickness = 0.5.dp
//    )
}