package com.freewdcmkt.bck.layout.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.freewdcmkt.bck.R
import com.freewdcmkt.bck.api.userAvatarUrl
import com.freewdcmkt.bck.components.freewd.FreewdDialog
import com.freewdcmkt.bck.components.freewd.SettingCard
import com.freewdcmkt.bck.components.freewd.UserCard
import com.freewdcmkt.bck.data.screen.MeData
import com.freewdcmkt.bck.util.TokenManager
import com.freewdcmkt.bck.util.UserInfoManager
import com.freewdcmkt.bck.viewmodel.HomeViewmodel
import com.freewdcmkt.bck.viewmodel.MeUiState
import kotlinx.coroutines.launch

@Composable
fun Me(viewmodel: HomeViewmodel = viewModel()) {
    val uiState by viewmodel.uiState.collectAsState()
    val qq by viewmodel.userAccount.collectAsState()
    val username by viewmodel.username.collectAsState()
    val uid by viewmodel.uid.collectAsState()

    LaunchedEffect(Unit) { viewmodel.getUserInfo() }

    when (uiState) {
        is MeUiState.Finish -> MeUiLayout(qq, username, uid, (uiState as MeUiState.Finish).meData)
        else -> MeUiLayout(qq, username, uid, null)
    }

}

@Composable
private fun MeUiLayout(qq: String, username: String, uid: String, meData: MeData? = null) {
    val isShowDialog = rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    if (isShowDialog.value) {
        FreewdDialog(
            onDismiss = { isShowDialog.value = false },
            onConfirm = {
                TokenManager.clearToken()
                scope.launch { UserInfoManager.saveLogin(false) }
            },
            title = stringResource(R.string.logout_hint),
            msg = stringResource(R.string.logout_account_hint),
            hintMsg1 = stringResource(R.string.no_hint),
            hintMsg2 = stringResource(R.string.yes_hint)
        )
    }
    LazyColumn() {
        item {
            Column {
                Card(shape = RoundedCornerShape(16.dp)) {
                    UserCard(
                        userAvatarUrl(qq),
                        username = username,
                        uid = stringResource(R.string.uid_hint, uid)
                    )
                    Row(
                        modifier = Modifier.padding(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (meData != null) {
                            IconText(
                                modifier = Modifier.weight(1f),
                                icon = R.drawable.post,
                                description = stringResource(
                                    R.string.post_count_hint,
                                    meData.postCount.toString()
                                ),
                                text = stringResource(
                                    R.string.post_count_hint,
                                    meData.postCount.toString()
                                ),
                                onClick = {},
                            )
                            IconText(
                                modifier = Modifier.weight(1f),
                                icon = R.drawable.baseline_favorite_24,
                                description = stringResource(
                                    R.string.likes_count_hint,
                                    meData.totalLikes.toString()
                                ),
                                text = stringResource(
                                    R.string.likes_count_hint,
                                    meData.totalLikes.toString()
                                ),
                                onClick = {},
                            )
                        } else {
                            Text(stringResource(R.string.loading_hint))
                        }
                    }
                }

            }

        }
        item {
            Spacer(modifier = Modifier.height(4.dp))
            SettingCard(
                icon = R.drawable.fa6solidpen,
                name = stringResource(R.string.change_username_hint),
                description = stringResource(R.string.change_username_description_hint),
                onClick = {}
            )
            SettingCard(
                R.drawable.logout,
                stringResource(R.string.logout_hint),
                stringResource(R.string.logout_description_hint),
                isRed = true,
                onClick = { isShowDialog.value = true }
            )
        }
    }
}

@Composable
private fun IconText(
    modifier: Modifier = Modifier,
    icon: Int,
    description: String? = null,
    text: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(icon),
            tint = MaterialTheme.colorScheme.primary,
            contentDescription = description,
            modifier = Modifier
                .size(22.dp)
                .animateContentSize()
        )
        Text(text = text, fontSize = 14.sp)
    }

}