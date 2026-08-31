package com.freewdcmkt.bck.components.freewd

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.freewdcmkt.bck.R

@Composable
fun SettingCard(
    icon: Int,
    name: String,
    description: String? = null,
    isRed: Boolean? = false,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onClick)
                .padding(15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = if (isRed == true) Color.Red else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .size(40.dp)
                    .padding(10.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                TitleText(text = name)
                if (description != null) ContentText(description)
            }

        }

    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ImageCard(url: String, onClick: (String) -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = { onClick(url) })
            .aspectRatio(16f / 9f)
    ) {

        SubcomposeAsyncImage(
            model = url,
            contentDescription = stringResource(R.string.image_hint),

            modifier = Modifier
                .fillMaxWidth(),
            contentScale = ContentScale.Crop,
            loading = {
                Box(modifier = Modifier.fillMaxSize()) {
                    LoadingIndicator(
                        modifier = Modifier
                            .size(48.dp)
                            .align(Alignment.Center)
                    )
                }
            })
    }
}

@Composable
fun ExpCard(exp: Int) {
    val maxExp = when {
        exp in 0..<200 -> 200f
        exp < 1500 -> 1500f
        exp < 4500 -> 4500f
        exp < 10800 -> 10800f
        exp < 28000 -> 28800f
        else -> 28800f
    }
    val level = when {
        exp in 0..<200 -> "Lv 1"
        exp < 1500 -> "Lv 2"
        exp < 4500 -> "Lv 3"
        exp < 10800 -> "Lv 4"
        exp < 28000 -> "Lv 5"
        else -> "Lv 6"
    }

    Card(shape = RoundedCornerShape(16.dp)) {
        Column(
            modifier = Modifier
                .padding(15.dp)
                .fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                LinearWavyProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(0.5f),
                    progress = { exp / maxExp },
                    amplitude = { 1.4f }
                )
                LevelText("${(exp / maxExp * 100).toInt()}%")
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LevelText(level)
                Text(
                    "$exp/${maxExp.toInt()}",
                    fontSize = 12.sp
                )
            }
        }
    }
}


@Composable
@Preview(showBackground = true)
private fun Show() {
    val f: Float = 1000 / 2800f
    // SettingCard(R.drawable.fa6solidpen, "gdfiugf", "sdbhiusdu", onClick = {})
    ImageCard("") {}
}
