package com.freewdcmkt.bck.components.freewd

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freewdcmkt.bck.R
import com.freewdcmkt.bck.api.userAvatarUrl
import com.freewdcmkt.bck.data.screen.UserProfileData

@Composable
fun UserCard(imageUrl: String, username: String, uid: String? = null, onClick: () -> Unit = {}) {
    Column {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserIcon(imageUrl, onClick = onClick)
            Column() {
                UsernameText(username)
                if (uid != null) UidText(stringResource(R.string.uid_hint, uid))
            }
        }
    }
}
@Composable
fun UserProfileHeader(data: UserProfileData) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 15.dp, vertical = 10.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                UserIcon(url = userAvatarUrl(data.qq))

                Spacer(modifier = Modifier.size(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    UsernameText(data.username)

                    UidText(stringResource(R.string.uid_hint, data.uid.toString()))
                    data.joinedAt?.let { joined ->
                        Text(
                            text = stringResource(
                                R.string.user_joined_hint,
                                joined.substringBefore(' ')
                            ),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            ContentText(data.bio.ifBlank { stringResource(R.string.user_no_bio_hint) })
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProfileStat(
                    value = data.postCount.toString(),
                    label = stringResource(R.string.user_stat_post)
                )
                ProfileStat(
                    value = data.totalLikes.toString(),
                    label = stringResource(R.string.user_stat_like)
                )
                ProfileStat(
                    value = data.xp.toString(),
                    label = stringResource(R.string.user_stat_xp)
                )
            }
        }
    }
}

@Composable
private fun ProfileStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
