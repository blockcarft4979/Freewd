package com.freewdcmkt.bck.layout.ui.community

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.freewdcmkt.bck.R
import com.freewdcmkt.bck.components.LoadErrorUiLayout
import com.freewdcmkt.bck.components.freewd.ImageCard
import com.freewdcmkt.bck.components.ui.LoadingCard
import com.freewdcmkt.bck.util.file.uriToFile
import com.freewdcmkt.bck.viewmodel.PostFeedUiState
import com.freewdcmkt.bck.viewmodel.PostFeedViewmodel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostFeedLayout(
    zone: Int,
    onUploaded: () -> Unit,
    onBack: () -> Unit,
    onToPreviewImg:(String)-> Unit,
    viewmodel: PostFeedViewmodel = viewModel()
) {
    val uiState by viewmodel.postFeedUiState.collectAsState()
    val imgUrl = rememberSaveable { mutableStateOf("") }
    val isUploadingImg = rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        when (uiState) {
            is PostFeedUiState.ImageUploaded -> isUploadingImg.value
            is PostFeedUiState.Error -> isUploadingImg.value = false
            else -> {}
        }
    }
    Scaffold(topBar = {
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
    }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 15.dp)
                .fillMaxSize()
        ) {
            when (uiState) {
                is PostFeedUiState.NoAction -> PostFeedUiLayout(
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
                    onClick = onToPreviewImg
                )

                is PostFeedUiState.Upload -> LoadingCard()
                is PostFeedUiState.Error -> LoadErrorUiLayout(
                    onClick = { viewmodel.resetState() },
                    msg = (uiState as PostFeedUiState.Error).msg,
                    buttonMsg = stringResource(R.string.yes_hint)
                )

                is PostFeedUiState.Success -> onUploaded()
                is PostFeedUiState.ImageUploaded -> {
                    viewmodel.resetState()
                    imgUrl.value = (uiState as PostFeedUiState.ImageUploaded).url
                }

            }
        }
    }
}

@Composable
fun PostFeedUiLayout(
    onPostFeed: (title: String?, message: String) -> Unit, onClick: (String) -> Unit,
    onUploadImg: (file: File) -> Unit, isUploadedImg: Boolean, imgUrl: String?
) {
    var title by rememberSaveable() { mutableStateOf("") }
    var message by rememberSaveable() { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    val isUploading = rememberSaveable() { mutableStateOf(isUploadedImg) }

    val context = LocalContext.current
    val imagePickerLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val file = uriToFile(uri, context)
                if (file != null) {
                    isUploading.value = true
                    onUploadImg(file)
                }
            }
        }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = title,
                onValueChange = { newTitle -> title = newTitle },
                label = { Text(stringResource(R.string.title_hint)) },
                maxLines = 1
            )
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                value = message,

                onValueChange = { newMessage -> message = newMessage },
                label = { Text(stringResource(R.string.content_hint)) }
            )
            if (!imgUrl.isNullOrEmpty()) Card(shape = RoundedCornerShape(16.dp)) {
                ImageCard(imgUrl, onClick = onClick)
            }
        }

        // 按钮固定在底部
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            onClick = { onPostFeed(title, message) },
            enabled = message.isNotEmpty() && message.isNotBlank()
        ) {
            Text(stringResource(R.string.post_new_feed_hint))
        }
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            enabled = !isUploading.value && imgUrl.isNullOrEmpty(),
            onClick = { imagePickerLauncher.launch("image/*") },
        ) {
            Text(stringResource(R.string.upload_image))
        }
    }

}