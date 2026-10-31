package com.freewdcmkt.bck.components.freewd

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LoadingCard(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        LoadingIndicator()
    }
}

/**
 * 带下方文案的加载态。用于需要说明「在做什么」的场景（例如搜索中）。
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LoadingCard(hint: String) {
    Box(
        modifier = Modifier.Companion.fillMaxSize(),
        contentAlignment = Alignment.Companion.Center
    ) {
        Column(
            horizontalAlignment = Alignment.Companion.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            LoadingIndicator()
            Spacer(modifier = Modifier.Companion.height(12.dp))
            Text(hint)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ShowLoadingCardWithHint() {
    LoadingCard("搜索中…")
}