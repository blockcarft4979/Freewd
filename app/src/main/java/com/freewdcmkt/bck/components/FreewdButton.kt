package com.freewdcmkt.bck.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun IconTextButton(
    modifier: Modifier = Modifier,
    icon: Int,
    description: String? = null,
    text: String,
    onClick: () -> Unit,

) {
    Row(
        modifier = modifier.clickable(true, onClick = onClick).padding(5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(icon),
            tint = MaterialTheme.colorScheme.primary,
            contentDescription = description,
            modifier = Modifier.size(22.dp)
        )
        Text(text = text, fontSize = 14.sp)
    }

}