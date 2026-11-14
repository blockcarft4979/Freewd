package com.freewdcmkt.bck.layout.ui.user

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.freewdcmkt.bck.R
import com.freewdcmkt.bck.api.userAvatarUrl
import com.freewdcmkt.bck.components.freewd.ExpCard
import com.freewdcmkt.bck.components.freewd.FeedCard
import com.freewdcmkt.bck.components.freewd.FreewdEditDialog
import com.freewdcmkt.bck.components.freewd.FreewdLoadingDialog
import com.freewdcmkt.bck.components.freewd.FreewdModalBottomSheet
import com.freewdcmkt.bck.components.freewd.ItemCard
import com.freewdcmkt.bck.components.freewd.LoadingCard
import com.freewdcmkt.bck.components.freewd.UserCard
import com.freewdcmkt.bck.components.freewd.UserProfileHeader
import com.freewdcmkt.bck.components.ui.PreviewImgUi
import com.freewdcmkt.bck.data.common.UserInfoData
import com.freewdcmkt.bck.data.screen.UserProfileData
import com.freewdcmkt.bck.util.TokenManager
import com.freewdcmkt.bck.util.UserInfoManager
import com.freewdcmkt.bck.viewmodel.user.MeViewModel
import com.freewdcmkt.bck.viewmodel.user.UserProfileUiState
import com.freewdcmkt.bck.viewmodel.user.UserProfileViewmodel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Me(
    viewmodel: MeViewModel = viewModel(),
    profileViewmodel: UserProfileViewmodel = viewModel(),
    onToSettings: () -> Unit,
    onToFeedDetail: (id: Int) -> Unit,
) {
    // ---- 本地数据：断网也能显示 ----
    val localUid by UserInfoData.uid.collectAsState()
    val localUsername by UserInfoData.username.collectAsState()
    val localBio by UserInfoData.bio.collectAsState()
    val localExp by UserInfoData.exp.collectAsState()
    val localQq by UserInfoData.account.collectAsState()

    // ---- 签到 / 对话框 ----
    val isShowChenInDialog by viewmodel.isShowChenInDialog.collectAsState()
    val isChecked by UserInfoData.isChecked.collectAsState()
    val checkInDays by UserInfoData.checkInDays.collectAsState()
    val isShowLoadingDialog by viewmodel.isShowSubmittingDialog.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) UserInfoData.refreshCheckStatus()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // ---- 帖子列表 ----
    val networkProfile by profileViewmodel.profile.collectAsState()
    val profileVm: UserProfileViewmodel = viewModel()
    val posts by profileVm.posts.collectAsState()
    val isLoadingPosts by profileVm.isLoadingPosts.collectAsState()
    val hasMore by profileVm.hasMore.collectAsState()
    val isLoadingMore by profileVm.isLoadingMore.collectAsState()
    val uiState by profileVm.uiState.collectAsState()

    val listState = rememberLazyListState()
    var imgUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(localUid) {
        if (localUid > 0) profileVm.load(localUid)
    }
    LaunchedEffect(listState, hasMore) {
        snapshotFlow {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = listState.layoutInfo.totalItemsCount
            total > 0 && lastVisible >= total - 1
        }.distinctUntilChanged().collect { atEnd ->
            if (atEnd && hasMore) profileVm.loadMore()
        }
    }

    // 本地拼的 profile：永远非空，网络怎么着都不影响 header
    val localProfile = remember(localUid, localUsername, localBio, localQq, localExp) {
        UserProfileData(
            uid = localUid,
            username = localUsername,
            qq = localQq,
            bio = localBio,
            xp = localExp,
        )
    }
    val displayProfile = networkProfile ?: localProfile

    val density = LocalDensity.current
    val avatarThresholdPx = with(density) { 74.dp.toPx() }
    val showTopBarAvatar by remember(displayProfile) {
        derivedStateOf {
            val firstItem = listState.layoutInfo.visibleItemsInfo.firstOrNull()
            if (firstItem == null) {
                false
            } else if (firstItem.index == 0) {
                -firstItem.offset > avatarThresholdPx
            } else {
                true
            }
        }
    }

    if (isShowLoadingDialog) FreewdLoadingDialog(stringResource(R.string.submitting_hint))
    if (isShowChenInDialog) FreewdLoadingDialog(stringResource(R.string.checking_hint))

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    AnimatedContent(
                        targetState = showTopBarAvatar,
                        transitionSpec = {
                            fadeIn(tween(250)) togetherWith fadeOut(tween(250))
                        }
                    ) { showAvatar ->
                        if (showAvatar) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                UserCard(
                                    userAvatarUrl(displayProfile.qq),
                                    username = displayProfile.username,
                                    uid = displayProfile.uid.toString()
                                )
                            }
                        } else {
                            Text(
                                stringResource(R.string.user_center_hint),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            PullToRefreshBox(
                isRefreshing = uiState is UserProfileUiState.Loading,
                onRefresh = {
                    if (localUid > 0) profileVm.load(localUid, true)
                    UserInfoData.refreshCheckStatus()
                }
            ) {
                LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                    item {
                        UserProfileHeader(displayProfile)
                        ExpCard(exp = localExp)
                    }
                    item {
                        MeActionCards(
                            checkInDays = checkInDays,
                            isChecked = isChecked,
                            onCheckIn = { viewmodel.checkIn() },
                            onConfirmUsername = { viewmodel.submitUsername(it) },
                            onConfirmBio = { viewmodel.updateBio(it) },
                            onToSettings = onToSettings,
                        )
                    }
                    item {
                        Text(
                            text = stringResource(R.string.user_my_posts_hint),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(
                                start = 20.dp, end = 20.dp, top = 14.dp, bottom = 2.dp
                            )
                        )
                    }
                    if (isLoadingPosts) {
                        item { LoadingCard(Modifier.padding(16.dp).height(64.dp)) }
                    } else if (posts.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (uiState is UserProfileUiState.Error)
                                        stringResource(R.string.unknown_error)
                                    else stringResource(R.string.user_no_post_hint),
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(items = posts, key = { it.id }) { feed ->
                            FeedCard(
                                feed = feed,
                                onClick = onToFeedDetail,
                                onToPreviewImg = { imgUrl = it },
                                onLike = { profileVm.toggleLike(it) }
                            )
                        }
                        if (isLoadingMore) {
                            item { LoadingCard(Modifier.padding(16.dp).height(64.dp)) }
                        }
                    }
                }
            }
            //PreviewImgUi(url = imgUrl ?: "", onDismiss = { imgUrl = null })
        }
    }
}

/** 签到 / 改昵称 / 改 bio / 设置 / 登出，原样抽出来 */
@Composable
private fun MeActionCards(
    checkInDays: Int,
    isChecked: Boolean,
    onCheckIn: () -> Unit,
    onConfirmUsername: (String) -> Unit,
    onConfirmBio: (String) -> Unit,
    onToSettings: () -> Unit,
) {
    val isShowDialog = rememberSaveable { mutableStateOf(false) }
    val isShowEditDialog = rememberSaveable { mutableStateOf(false) }
    val isShowEditBioDialog = rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val username by UserInfoData.username.collectAsState()
    val bio by UserInfoData.bio.collectAsState()

    if (isShowEditDialog.value) {
        FreewdEditDialog(
            text = username,
            onDismiss = { isShowEditDialog.value = false },
            onConfirm = { onConfirmUsername(it); isShowEditDialog.value = false },
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
            onConfirm = { onConfirmBio(it); isShowEditBioDialog.value = false },
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
                scope.launch {
                    TokenManager.clearToken()
                    UserInfoManager.clearAllData()
                }
                isShowDialog.value = false
            },
            title = stringResource(R.string.logout_hint),
            msg = stringResource(R.string.logout_account_hint),
            cancelHint = stringResource(R.string.no_hint),
            confirmHint = stringResource(R.string.yes_hint)
        )
    }

    Spacer(Modifier.height(8.dp))
    Column(modifier = Modifier.padding(horizontal = 15.dp)) {
        Card(shape = RoundedCornerShape(16.dp)) {
            ItemCard(
                icon = if (isChecked) R.drawable.calendarchecked else R.drawable.calendaruncheck,
                name = if (isChecked) stringResource(R.string.checked_hint, checkInDays)
                else stringResource(R.string.unchecked_hint),
                onClick = { if (!isChecked) onCheckIn() },
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
        Spacer(Modifier.height(8.dp))
        ItemCard(
            icon = R.drawable.logout,
            name = stringResource(R.string.logout_hint),
            color = Color.Red,
            onClick = { isShowDialog.value = true }
        )
    }
}