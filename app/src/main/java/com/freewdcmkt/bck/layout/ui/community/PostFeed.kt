package com.freewdcmkt.bck.layout.ui.community

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.freewdcmkt.bck.R
import com.freewdcmkt.bck.api.userAvatarUrl
import com.freewdcmkt.bck.components.freewd.FreewdLoadingDialog
import com.freewdcmkt.bck.components.freewd.ImageCard
import com.freewdcmkt.bck.components.freewd.UserIcon
import com.freewdcmkt.bck.components.freewd.UsernameText
import com.freewdcmkt.bck.data.common.UserInfoData
import com.freewdcmkt.bck.util.file.uriToFile
import com.freewdcmkt.bck.viewmodel.community.PostFeedUiState
import com.freewdcmkt.bck.viewmodel.community.PostFeedViewmodel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostFeedLayout(
    zone: Int,
    onUploaded: () -> Unit,
    onBack: () -> Unit,
    onToPreviewImg: (String) -> Unit,
    viewmodel: PostFeedViewmodel = viewModel()
) {
    val uiState by viewmodel.postFeedUiState.collectAsState()
    val qq by UserInfoData.account.collectAsState()
    val noNetworkHint = stringResource(R.string.no_internet_hint)
    val imgUrl = rememberSaveable { mutableStateOf("") }
    val snackBarHostState = remember { SnackbarHostState() }
    val isUploadingImg = rememberSaveable { mutableStateOf(false) }
    val isShowDialog = rememberSaveable() { mutableStateOf(false) }
    if (isShowDialog.value) {
        FreewdLoadingDialog(stringResource(R.string.uploading_hint))
    }
    LaunchedEffect(uiState) {
        when (uiState) {
            is PostFeedUiState.Error -> {
                isShowDialog.value = false
                isUploadingImg.value = false
                imgUrl.value = ""
                if ((uiState as PostFeedUiState.Error).isNoNetwork) snackBarHostState.showSnackbar(
                    noNetworkHint,
                    duration = SnackbarDuration.Short
                ) else {
                    (uiState as PostFeedUiState.Error).msg?.let {
                        snackBarHostState.showSnackbar(
                            it,
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            }

            is PostFeedUiState.Upload -> isShowDialog.value = true

            is PostFeedUiState.Success -> onUploaded()

            is PostFeedUiState.ImageUploaded -> {
                isUploadingImg.value = false
                isShowDialog.value = false
                imgUrl.value = (uiState as PostFeedUiState.ImageUploaded).url
            }

            else -> {
                isUploadingImg.value = false
            }
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_post_hint)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painterResource(R.drawable.baseline_arrow_back_24),
                            stringResource(R.string.back_hint)
                        )
                    }
                })
        },
        snackbarHost = { SnackbarHost(snackBarHostState) },
        modifier = Modifier.imePadding()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 15.dp)
                .fillMaxSize()
        ) {
            PostFeedUiLayout(
                onPostFeed = { title, message ->
                    viewmodel.postFeed(
                        zone = zone,
                        title = title,
                        message = message,
                        imgUrl = imgUrl.value
                    )
                },
                onUploadImg = { imgFile -> viewmodel.uploadImg(imgFile) },
                isUploadedImg = isUploadingImg.value,
                imgUrl = imgUrl.value,
                onToPreviewImg = onToPreviewImg,
                qq = qq
            )
        }
    }
}

@Composable
fun PostFeedUiLayout(
    onPostFeed: (title: String?, message: String) -> Unit,
    onToPreviewImg: (String) -> Unit,
    onUploadImg: (file: File) -> Unit,
    isUploadedImg: Boolean,
    imgUrl: String?,
    qq: String,
) {
    var title by rememberSaveable { mutableStateOf("") }
    var message by rememberSaveable { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    val context = LocalContext.current
    val imagePickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let {
                val file = uriToFile(uri, context)
                if (file != null) {
                    onUploadImg(file)
                }
            }
        }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            UserIcon(userAvatarUrl(qq))

            Column() {
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.title_hint)) },
                    maxLines = 1
                )
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    value = message,
                    onValueChange = { message = it },
                    label = { Text(stringResource(R.string.content_hint)) }
                )
                if (!imgUrl.isNullOrEmpty()) {
                    Card(
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        ImageCard(
                            imgUrl,
                            onClick = onToPreviewImg
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(
                        enabled = !isUploadedImg && imgUrl.isNullOrEmpty(),
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier
                            .size(32.dp)
                            .padding(4.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.pictuer),
                            contentDescription = stringResource(R.string.add_picture_hint)
                        )
                    }
                }
            }

        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                modifier = Modifier
                    .padding(top = 8.dp),
                onClick = {
                    onPostFeed(title, message)
                },
                enabled = message.isNotEmpty() && message.isNotBlank()
            ) {
                Text(stringResource(R.string.post_new_feed_hint))
            }
        }


    }
}