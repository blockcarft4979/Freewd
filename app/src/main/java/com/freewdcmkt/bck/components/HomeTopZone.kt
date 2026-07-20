package com.freewdcmkt.bck.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.freewdcmkt.bck.R
import com.freewdcmkt.bck.api.userAvatarUrl

@Composable
fun HomeTopZone(qq: String, username: String, uid: String, imageUrl: String?) {
    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth())
    {
        Column(modifier = Modifier.padding(all = 10.dp)) {
            Row {
                Image(
                    // painter = painterResource(R.drawable.ic_launcher_background),
                    painter = rememberAsyncImagePainter(userAvatarUrl(qq = qq)),
                    null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(55.dp)
                        .clip(CircleShape)

                )
                //Spacer(modifier = Modifier.height(4.dp))
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    Text(username, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(stringResource(R.string.uid_hint, uid), fontSize = 12.sp)
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
@Preview(name = "Dark Mode")
fun UserProfile() {
    HomeTopZone("1000021", "BCK_", "10000", "null")
}