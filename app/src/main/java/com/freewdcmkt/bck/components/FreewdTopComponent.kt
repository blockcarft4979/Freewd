package com.freewdcmkt.bck.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freewdcmkt.bck.R

@Composable
fun FreewdTopComponent(){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier
                .size(40.dp)
                .padding(5.dp),
            painter = painterResource(R.mipmap.ic_launcher_monochrome),
            contentDescription = null
        )
        Text(
            modifier = Modifier.padding(start = 4.dp),
            text = stringResource(R.string.login_welcome_title),
            //modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
    }
    Text(
        text = stringResource(R.string.login_hint),
        fontWeight = FontWeight.Thin,
        fontSize = 15.sp
    )
}