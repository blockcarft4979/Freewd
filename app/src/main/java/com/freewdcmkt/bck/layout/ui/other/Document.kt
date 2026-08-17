package com.freewdcmkt.bck.layout.ui.other

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.freewdcmkt.bck.R
import com.freewdcmkt.bck.components.freewd.ContentMarkdown
import com.freewdcmkt.bck.components.ui.FreewdHint
import com.freewdcmkt.bck.components.ui.LoadingCard
import com.freewdcmkt.bck.viewmodel.other.DocumentUiState
import com.freewdcmkt.bck.viewmodel.other.DocumentViewModel

@Composable
fun Document(onBack: () -> Unit, url: String, viewmodel: DocumentViewModel = viewModel()) {
    val uiState by viewmodel.uiState.collectAsState()
    val documentContent by viewmodel.documentContent.collectAsState()
    LaunchedEffect(url){
        viewmodel.fetchData(url)
    }
    Scaffold(topBar = {
        TopAppBar(
            title = { Text(stringResource(R.string.about_hint)) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_arrow_back_24),
                        contentDescription = stringResource(R.string.back_hint)
                    )
                }
            })
    }, modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 15.dp)
                .verticalScroll(rememberScrollState())
        ) {
            when (uiState) {
                is DocumentUiState.Loading -> LoadingCard()
                is DocumentUiState.Finish -> ContentMarkdown(documentContent)
                is DocumentUiState.LoadFailed -> FreewdHint(hint = stringResource(R.string.load_error_hint))
            }

        }
    }
}