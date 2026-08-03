package com.freewdcmkt.bck.components

import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.freewdcmkt.bck.R

@Composable
fun NotificationIcon(count: Int) {
    BadgedBox(
        badge = {
            if (count > 0) {
                Badge {
                    Text(
                        text = if (count < 9) count.toString() else "9+"
                    )
                }
            }
        }
    ) {
        Icon(
            painter = painterResource(R.drawable.baseline_notifications_none_24),
            contentDescription = stringResource(R.string.notification_hint),
        )
    }
}