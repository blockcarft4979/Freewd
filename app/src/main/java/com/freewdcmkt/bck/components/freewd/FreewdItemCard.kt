package com.freewdcmkt.bck.components.freewd

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun SettingCard(icon: Int, name: String, description: String) {
    Card(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(15.dp)
        ) {
            Icon(
                painter = painterResource(icon),
                null,
                modifier = Modifier
                    .size(40.dp)
                    .padding(10.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                TitleText(text = name)
                ContentText(description)
            }

        }

    }
}
