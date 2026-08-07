package com.freewdcmkt.bck.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.freewdcmkt.bck.R
import com.freewdcmkt.bck.api.userAvatarUrl
import com.freewdcmkt.bck.components.freewd.DateText
import com.freewdcmkt.bck.components.freewd.UserIcon
import com.freewdcmkt.bck.components.freewd.UsernameText

@Composable
fun HomeTopZone(qq: String, username: String, uid: String, imageUrl: String?) {
    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth())
    {
        Column(modifier = Modifier.padding(all = 10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                UserIcon(userAvatarUrl(qq))

                Column(modifier = Modifier.padding(start = 8.dp)) {
                    UsernameText(username)
                    DateText(stringResource(R.string.uid_hint, uid))
                }
            }
        }
        if (imageUrl != null) {
            Image(
                painter = rememberAsyncImagePainter(imageUrl),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
@Preview(name = "Dark Mode", device = Devices.PIXEL_5)
fun UserProfile() {
    HomeTopZone("1000021", "BCK_", "10000", "null")
}