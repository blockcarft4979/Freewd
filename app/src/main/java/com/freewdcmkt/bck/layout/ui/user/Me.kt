package com.freewdcmkt.bck.layout.ui.user

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
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
import com.freewdcmkt.bck.components.freewd.ExpCard
import com.freewdcmkt.bck.components.freewd.FreewdEditDialog
import com.freewdcmkt.bck.components.freewd.FreewdLoadingDialog
import com.freewdcmkt.bck.components.freewd.FreewdModalBottomSheet
import com.freewdcmkt.bck.components.freewd.SettingCard
import com.freewdcmkt.bck.data.common.UserInfoData
import com.freewdcmkt.bck.util.TokenManager
import com.freewdcmkt.bck.util.UserInfoManager
import com.freewdcmkt.bck.viewmodel.user.MeViewModel
import kotlinx.coroutines.launch

@Composable
fun Me(viewmodel: MeViewModel = viewModel(), onToUserCenter: () -> Unit) {

    val isShowChenInDialog by viewmodel.isShowChenInDialog.collectAsState()
    val isChecked by UserInfoData.isChecked.collectAsState()
    val exp by UserInfoData.exp.collectAsState()
    val checkInDays by UserInfoData.checkInDays.collectAsState()
    val isShowLoadingDialog by viewmodel.isShowSubmittingDialog.collectAsState()

    if (isShowLoadingDialog) FreewdLoadingDialog(stringResource(R.string.submitting_hint))

    if (isShowChenInDialog) FreewdLoadingDialog(stringResource(R.string.checking_hint))
    MeUiLayout(
        onConfirmUsername = { viewmodel.submitUsername(it) },
        onToUserCenter = onToUserCenter,
        exp = exp, checkInDays = checkInDays,
        isChecked = isChecked,
        onCheckIn = { viewmodel.checkIn() }
    )

}

@Composable
private fun MeUiLayout(
    exp: Int = 0,
    checkInDays: Int = 0,
    isChecked: Boolean,
    onConfirmUsername: (String) -> Unit,
    onCheckIn: () -> Unit,
    onToUserCenter: () -> Unit
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
        FreewdModalBottomSheet(
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
            ExpCard(
                exp = exp
            )
        }
        item {
            Spacer(modifier = Modifier.height(8.dp))

//            SettingCard(
//                R.drawable.personal_center,
//                stringResource(R.string.user_center_hint),
//                stringResource(R.string.user_center_description_hint),
//                onClick = onToUserCenter
//            )
            SettingCard(
                icon = if (isChecked) R.drawable.calendarchecked else
                    R.drawable.calendaruncheck,
                name = stringResource(R.string.check_in_hint),
                description = if (isChecked) stringResource(
                    R.string.checked_hint,
                    checkInDays
                ) else stringResource(R.string.unchenked_hint),
                onClick = {
                    if (!isChecked) {
                        onCheckIn()
                    }
                }
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

sealed class MeUiState() {
    object NoAction : MeUiState()
    object SubmittingUsername : MeUiState()
    object SubmitFinish : MeUiState()

    class LoadError(val msg: String? = null, val isNoNetWork: Boolean = false) : MeUiState()
}