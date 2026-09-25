package com.freewdcmkt.bck.components.freewd

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import com.freewdcmkt.bck.R
import com.freewdcmkt.bck.api.userAvatarUrl

@Composable
fun FreewdTopComponent(
    qq: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
//        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (qq.isEmpty()) {
            Icon(
                modifier = Modifier
                    .size(50.dp)
                    .padding(5.dp),
                painter = painterResource(R.mipmap.ic_launcher_monochrome),
                contentDescription = null
            )
        } else {
            Image(
                modifier = Modifier
                    .clip(CircleShape)
                    .size(50.dp),
                painter = rememberAsyncImagePainter(userAvatarUrl(qq)),
                contentDescription = null
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.login_welcome_title),
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
    }
}