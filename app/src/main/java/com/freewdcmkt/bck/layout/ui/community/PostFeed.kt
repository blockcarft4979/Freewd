package com.freewdcmkt.bck.layout.ui.community

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.freewdcmkt.bck.R
import com.freewdcmkt.bck.api.userAvatarUrl
import com.freewdcmkt.bck.components.freewd.FreewdIcon
import com.freewdcmkt.bck.components.freewd.FreewdLoadingDialog
import com.freewdcmkt.bck.components.freewd.FreewdSwitch
import com.freewdcmkt.bck.components.freewd.ImageCard
import com.freewdcmkt.bck.components.freewd.UserIcon
import com.freewdcmkt.bck.components.ui.PreviewImgUi
import com.freewdcmkt.bck.data.common.UserInfoData
import com.freewdcmkt.bck.util.file.uriToFile
import com.freewdcmkt.bck.viewmodel.community.PostFeedUiState
import com.freewdcmkt.bck.viewmodel.community.PostFeedViewmodel
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostFeedLayout(
    onUploaded: () -> Unit,
    onBack: () -> Unit,
    viewmodel: PostFeedViewmodel = viewModel()
) {
    var previewImgUrl by remember { mutableStateOf<String?>(null) }
    val uiState by viewmodel.postFeedUiState.collectAsState()
    val qq by UserInfoData.account.collectAsState()
    val unknownError = stringResource(R.string.unknown_error)
    val imgUrl = rememberSaveable { mutableStateOf("") }
    val enabledMarkdownHint = stringResource(R.string.enabled_markdown_hint)
    val disabledMarkdownHint = stringResource(R.string.disabled_markdown_hint)

    val snackBarHostState = remember { SnackbarHostState() }
    val isUploadingImg = rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var message by rememberSaveable { mutableStateOf("") }
    var isMarkdown by rememberSaveable { mutableStateOf(false) }
    var isAnonymous by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        (uiState as? PostFeedUiState.Error)?.let { error ->
            if (error.isNoNetwork) snackBarHostState.showSnackbar(unknownError) else error.msg?.let {
                snackBarHostState.showSnackbar(it)
            }
        }
        if (uiState is PostFeedUiState.Success) {
            viewmodel.resetUi()
            onUploaded()
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
        bottomBar = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp, vertical = 8.dp)
                    .imePadding()
            ) {
                FreewdSwitch(
                    stringResource(R.string.anonymous_post_hint),
                    checked = isAnonymous,
                    onCheckedChange = { isAnonymous = it },
                )
                Button(
                    onClick = {
                        viewmodel.postFeed(
                            message = message,
                            isAnonymous = isAnonymous,
                            isMarkdown = isMarkdown,
                            imgUrl = imgUrl.value
                        )
                    },
                    enabled = message.isNotBlank()
                ) {
                    Text(stringResource(R.string.post_new_feed_hint))
                }
            }
        },
        modifier = Modifier.imePadding()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 15.dp)
                .fillMaxSize()
        ) {
            PostFeedUiLayout(
                qq = qq,
                message = message,
                onMessageChange = { message = it },
                isAnonymous = isAnonymous,
                isMarkdown = isMarkdown,
                onUploadImg = { imgFile -> viewmodel.uploadImg(imgFile) },
                isUploadedImg = isUploadingImg.value,
                imgUrl = imgUrl.value,
                onToPreviewImg = { url -> previewImgUrl = url },
                onIsMarkdownChange = { newValue ->
                    isMarkdown = newValue
                    scope.launch {
                        snackBarHostState.currentSnackbarData?.dismiss()
                        snackBarHostState.showSnackbar(
                            if (newValue) enabledMarkdownHint else disabledMarkdownHint
                        )
                    }
                },
            )
            when (uiState) {
                is PostFeedUiState.ImageUploaded -> {
                    isUploadingImg.value = false
                    imgUrl.value = (uiState as PostFeedUiState.ImageUploaded).url
                }

                is PostFeedUiState.Upload -> FreewdLoadingDialog(stringResource(R.string.uploading_hint))
                else -> {
                    isUploadingImg.value = false
                }
            }
        }
        PreviewImgUi(url = previewImgUrl ?: "", onDismiss = { previewImgUrl = null })
    }
}

@Composable
fun PostFeedUiLayout(
    message: String,
    onMessageChange: (String) -> Unit,
    isAnonymous: Boolean,
    onToPreviewImg: (String) -> Unit,
    onUploadImg: (file: File) -> Unit,
    onIsMarkdownChange: (Boolean) -> Unit,
    isUploadedImg: Boolean,
    imgUrl: String?,
    qq: String,
    isMarkdown: Boolean,
) {
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
            AnimatedContent(
                targetState = isAnonymous,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(220)) + scaleIn(
                        initialScale = 0.8f,
                        animationSpec = tween(220)
                    )) togetherWith
                            (fadeOut(animationSpec = tween(160)) + scaleOut(
                                targetScale = 0.8f,
                                animationSpec = tween(160)
                            ))
                },
            ) { anonymous ->
                if (anonymous) FreewdIcon() else UserIcon(userAvatarUrl(qq))
            }

            Column {
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    value = message,
                    onValueChange = { input -> onMessageChange(input.take(3000)) },
                    label = { Text(stringResource(R.string.post_feed_content_hint)) }
                )
                if (!imgUrl.isNullOrEmpty()) {
                    Card(shape = RoundedCornerShape(16.dp)) {
                        ImageCard(imgUrl, onClick = onToPreviewImg)
                    }
                }
                Text("${message.length} / 3000", color = Color.Gray, fontSize = 12.sp)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(
                        enabled = !isUploadedImg && imgUrl.isNullOrEmpty(),
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier
                            .size(32.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.pictuer),
                            contentDescription = stringResource(R.string.add_picture_hint)
                        )
                    }
                    IconButton(
                        onClick = { onIsMarkdownChange(!isMarkdown) },
                        modifier = Modifier
                            .size(32.dp)
                    ) {
                        Icon(
                            painterResource(R.drawable.markdown),
                            contentDescription = if (isMarkdown) stringResource(R.string.enabled_markdown_hint) else stringResource(
                                R.string.disabled_markdown_hint
                            ),
                            tint = if (isMarkdown) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .background(
                                    color = if (isMarkdown) MaterialTheme.colorScheme.primaryContainer
                                    else Color.Transparent,
                                    shape = RoundedCornerShape(6.dp)
                                )
                        )
                    }
                }
            }
        }
    }
}