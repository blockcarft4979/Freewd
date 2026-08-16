package com.freewdcmkt.bck.layout.ui.user

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
import com.freewdcmkt.bck.components.freewd.FreewdEditDialog
import com.freewdcmkt.bck.components.freewd.FreewdLoadingDialog
import com.freewdcmkt.bck.components.freewd.SettingCard
import com.freewdcmkt.bck.components.freewd.UserCard
import com.freewdcmkt.bck.util.TokenManager
import com.freewdcmkt.bck.util.UserInfoManager
import com.freewdcmkt.bck.viewmodel.HomeViewmodel
import com.freewdcmkt.bck.viewmodel.MeUiState
import kotlinx.coroutines.launch

@Composable
fun Me(viewmodel: HomeViewmodel = viewModel(),onToUserCenter: () -> Unit) {
    val uiState by viewmodel.uiState.collectAsState()
    val qq by viewmodel.userAccount.collectAsState()
    val username by viewmodel.username.collectAsState()
    val uid by viewmodel.uid.collectAsState()
    val isShowLoadingDialog = rememberSaveable() { mutableStateOf(false) }

    if (isShowLoadingDialog.value) {
        FreewdLoadingDialog( stringResource(R.string.submitting_hint))
    }

    MeUiLayout(
        qq, username, uid,
        onConfirmUsername = {
            viewmodel.submitUsername(it)
            isShowLoadingDialog.value = true
        }, onToUserCenter = onToUserCenter
    )
    when (uiState) {

        is MeUiState.SubmitFinish -> isShowLoadingDialog.value = false
        else -> {
            isShowLoadingDialog.value = false
        }
    }

}

@Composable
private fun MeUiLayout(
    qq: String,
    username: String,
    uid: String,
    onConfirmUsername: (String) -> Unit,
    onToUserCenter:()-> Unit
) {
    val isShowDialog = rememberSaveable { mutableStateOf(false) }
    val isShowEditDialog = rememberSaveable() { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    if (isShowEditDialog.value) {
        FreewdEditDialog(
            onDismiss = { isShowEditDialog.value = false },
            onConfirm = {
                onConfirmUsername(it)
                isShowEditDialog.value = false
            },
            title = stringResource(R.string.change_username_hint),
            maxLength = 15,
            hintMsg1 = stringResource(R.string.cancel_hint),
            hintMsg2 = stringResource(R.string.yes_hint)
        )
    }

    if (isShowDialog.value) {
        FreewdDialog(
            onDismiss = { isShowDialog.value = false },
            onConfirm = {
                TokenManager.clearToken()
                scope.launch { UserInfoManager.saveLogin(false) }
            },
            title = stringResource(R.string.logout_hint),
            msg = stringResource(R.string.logout_account_hint),
            cancelHint = stringResource(R.string.no_hint),
            confirmHint = stringResource(R.string.yes_hint)
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
                }

            }

        }
        item {
            Spacer(modifier = Modifier.height(4.dp))
            SettingCard(
                R.drawable.personal_center,
                stringResource(R.string.user_center_hint),
                stringResource(R.string.user_center_description_hint),
                onClick = onToUserCenter
            )
            SettingCard(
                icon = R.drawable.fa6solidpen,
                name = stringResource(R.string.change_username_hint),
                description = stringResource(R.string.change_username_description_hint),
                onClick = { isShowEditDialog.value = true }
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