package com.freewdcmkt.bck.components.freewd

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FreewdCheckBox(checked: Boolean, text: String, onCheckBoxChanged: (Boolean) -> Unit) {
    // var checked by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.clickable(
            onClick = { onCheckBoxChanged(!checked) }, indication = null,
            interactionSource = remember { MutableInteractionSource() },
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckBoxChanged
        )
        Text(text, fontSize = 12.sp)
    }

}