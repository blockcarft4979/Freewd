package com.freewdcmkt.bck.components.freewd

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
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
import coil.compose.rememberAsyncImagePainter
import com.freewdcmkt.bck.R
import com.freewdcmkt.bck.api.RequestApi
import com.freewdcmkt.bck.api.userAvatarUrl
import com.freewdcmkt.bck.ui.theme.FreewdTheme

@Composable
fun FreewdTopComponent(qq: String, onToUserAgreement: (String) -> Unit, onToPolicyPrivacy: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (qq.isEmpty()) {
            Icon(
                modifier = Modifier
                    .clip(CircleShape)
                    .size(40.dp)
                    .padding(5.dp),
                painter = painterResource(R.mipmap.ic_launcher_monochrome),
                contentDescription = null
            )
        } else {
            Image(
                modifier = Modifier
                    .clip(CircleShape)
                    .size(40.dp)
                    .padding(5.dp),
                painter = rememberAsyncImagePainter(userAvatarUrl(qq)),
                contentDescription = null
            )
        }
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
    Text(stringResource(R.string.agree_agreement_part), fontSize = 10.sp)
    Spacer(modifier = Modifier.height(8.dp))
    Row (){
        Text(
            stringResource(R.string.user_agreement),
            fontSize = 12.sp,
            color = FreewdTheme.themeColor,
            modifier = Modifier.clickable(onClick = { onToUserAgreement(RequestApi.Document.USER_AGREEMENT) })
        )
        Text(
            stringResource(R.string.policy_privacy),
            fontSize = 12.sp,
            color = FreewdTheme.themeColor,
            modifier = Modifier.clickable(onClick = { onToPolicyPrivacy(RequestApi.Document.PRIVACY_POLICY) })
        )
    }
}