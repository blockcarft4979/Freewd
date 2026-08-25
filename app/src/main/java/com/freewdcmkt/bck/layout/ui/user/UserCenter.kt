package com.freewdcmkt.bck.layout.ui.user

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.freewdcmkt.bck.R
import com.freewdcmkt.bck.api.userAvatarUrl
import com.freewdcmkt.bck.components.freewd.ExpCard
import com.freewdcmkt.bck.components.freewd.FreewdLoadingDialog
import com.freewdcmkt.bck.components.freewd.SettingCard
import com.freewdcmkt.bck.components.freewd.UidText
import com.freewdcmkt.bck.components.freewd.UserCard
import com.freewdcmkt.bck.components.ui.LoadingCard
import com.freewdcmkt.bck.data.common.UserInfoData
import com.freewdcmkt.bck.viewmodel.user.UserCenterUiState
import com.freewdcmkt.bck.viewmodel.user.UserCenterViewModel

@Composable
fun UserCenter(onBack: () -> Unit, viewModel: UserCenterViewModel = viewModel()) {

    val username by UserInfoData.username.collectAsState()
    val qq by UserInfoData.account.collectAsState()
    val uid by UserInfoData.uid.collectAsState()
    val exp by UserInfoData.exp.collectAsState()
    val checkInDays by UserInfoData.checkInDays.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val isChecked by viewModel.isChecked.collectAsState()
    val isShowCheckInDialog by viewModel.isShowChenInDialog.collectAsState()

    if (isShowCheckInDialog) FreewdLoadingDialog(stringResource(R.string.checking_hint))
    Scaffold(topBar = {
        TopAppBar(
            { Text(stringResource(R.string.user_center_hint)) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        painterResource(R.drawable.baseline_arrow_back_24),
                        contentDescription = stringResource(R.string.back_hint)
                    )
                }
            })
    }) { innerPadding ->
        when (uiState) {
            is UserCenterUiState.Loading -> LoadingCard()
            else -> {}
        }
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 15.dp)
        ) {
            UserCard(userAvatarUrl(qq), username, uid)
            Row() { UidText(stringResource(R.string.post_count_hint,)) }
            ExpCard(exp, checkInDays)
            SettingCard(
                icon = if (isChecked) R.drawable.calendarchecked else
                    R.drawable.calendaruncheck,
                name = stringResource(R.string.check_in_hint),
                onClick = {
                    if (isChecked) {
                        viewModel.checkIn()
                    }
                }
            )
        }
    }
}