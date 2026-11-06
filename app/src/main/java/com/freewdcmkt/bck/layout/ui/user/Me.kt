package com.freewdcmkt.bck.layout.ui.user

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.freewdcmkt.bck.R
import com.freewdcmkt.bck.components.freewd.FreewdEditDialog
import com.freewdcmkt.bck.components.freewd.FreewdLoadingDialog
import com.freewdcmkt.bck.components.freewd.FreewdModalBottomSheet
import com.freewdcmkt.bck.components.freewd.ItemCard
import com.freewdcmkt.bck.data.common.UserInfoData
import com.freewdcmkt.bck.util.TokenManager
import com.freewdcmkt.bck.util.UserInfoManager
import com.freewdcmkt.bck.viewmodel.user.MeViewModel
import kotlinx.coroutines.launch

@Composable
fun Me(
    viewmodel: MeViewModel = viewModel(),
    onToSettings: () -> Unit,
) {

    val isShowChenInDialog by viewmodel.isShowChenInDialog.collectAsState()
    val isChecked by UserInfoData.isChecked.collectAsState()
    val checkInDays by UserInfoData.checkInDays.collectAsState()
    val isShowLoadingDialog by viewmodel.isShowSubmittingDialog.collectAsState()

    if (isShowLoadingDialog) FreewdLoadingDialog(stringResource(R.string.submitting_hint))

    if (isShowChenInDialog) FreewdLoadingDialog(stringResource(R.string.checking_hint))
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                UserInfoData.refreshCheckStatus()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    MeUiLayout(
        onConfirmUsername = { viewmodel.submitUsername(it) },
        checkInDays = checkInDays,
        isChecked = isChecked,
        onCheckIn = { viewmodel.checkIn() },
        onConfirmBio = { viewmodel.updateBio(it) },
        onToSettings = onToSettings
    )

}

@Composable
private fun MeUiLayout(
    checkInDays: Int = 0,
    isChecked: Boolean,
    onConfirmUsername: (String) -> Unit,
    onConfirmBio: (String) -> Unit,
    onCheckIn: () -> Unit,
    onToSettings: () -> Unit
) {

    val isShowDialog = rememberSaveable { mutableStateOf(false) }
    val isShowEditDialog = rememberSaveable() { mutableStateOf(false) }
    val isShowEditBioDialog = rememberSaveable() { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val username by UserInfoData.username.collectAsState()
    val bio by UserInfoData.bio.collectAsState()

    if (isShowEditDialog.value) {
        FreewdEditDialog(
            text = username,
            onDismiss = { isShowEditDialog.value = false },
            onConfirm = {
                onConfirmUsername(it)
                isShowEditDialog.value = false
            },
            title = stringResource(R.string.change_username_hint),
            maxLength = 15,
            hintMsg1 = stringResource(R.string.cancel_hint),
            hintMsg2 = stringResource(R.string.yes_hint),
        )
    }
    if (isShowEditBioDialog.value) {
        FreewdEditDialog(
            text = bio,
            onDismiss = { isShowEditBioDialog.value = false },
            onConfirm = {
                onConfirmBio(it)
                isShowEditBioDialog.value = false
            },
            title = stringResource(R.string.user_edit_bio_hint),
            maxLength = 200,
            maxLines = 5,
            hintMsg1 = stringResource(R.string.cancel_hint),
            hintMsg2 = stringResource(R.string.yes_hint),
        )
    }
    if (isShowDialog.value) {
        FreewdModalBottomSheet(
            onDismiss = { isShowDialog.value = false },
            onConfirm = {
                TokenManager.clearToken()
                scope.launch {
                    UserInfoManager.clearAllData()
                }
            },
            title = stringResource(R.string.logout_hint),
            msg = stringResource(R.string.logout_account_hint),
            cancelHint = stringResource(R.string.no_hint),
            confirmHint = stringResource(R.string.yes_hint)
        )
    }

    Spacer(modifier = Modifier.height(8.dp))
    Column(modifier = Modifier.padding(horizontal = 15.dp)) {
        Card(shape = RoundedCornerShape(16.dp)) {

            ItemCard(
                icon = if (isChecked) R.drawable.calendarchecked else
                    R.drawable.calendaruncheck,
                name = if (isChecked) stringResource(
                    R.string.checked_hint,
                    checkInDays
                ) else stringResource(R.string.unchecked_hint),
                onClick = {
                    if (!isChecked) {
                        onCheckIn()
                    }
                },
                isShowArrow = false
            )
            ItemCard(
                icon = R.drawable.baseline_edit_24,
                name = stringResource(R.string.change_username_hint),
                onClick = { isShowEditDialog.value = true },
                isShowArrow = false
            )
            ItemCard(
                icon = R.drawable.baseline_edit_note_24,
                name = stringResource(R.string.user_edit_bio_hint),
                onClick = { isShowEditBioDialog.value = true },
                isShowArrow = false
            )
            ItemCard(
                onClick = onToSettings,
                icon = R.drawable.baseline_settings_24,
                name = stringResource(R.string.settings_hint)
            )

        }
        Spacer(modifier = Modifier.height(8.dp))

        ItemCard(
            icon = R.drawable.logout,
            name = stringResource(R.string.logout_hint),
            color = Color.Red,
            onClick = { isShowDialog.value = true }
        )
    }

}

