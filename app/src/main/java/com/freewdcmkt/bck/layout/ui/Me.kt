package com.freewdcmkt.bck.layout.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.freewdcmkt.bck.R
import com.freewdcmkt.bck.api.userAvatarUrl
import com.freewdcmkt.bck.components.LoadingCard
import com.freewdcmkt.bck.components.freewd.SettingCard
import com.freewdcmkt.bck.components.freewd.UserCard
import com.freewdcmkt.bck.data.screen.MeData
import com.freewdcmkt.bck.viewmodel.HomeViewmodel
import com.freewdcmkt.bck.viewmodel.MeUiState

@Composable
fun Me(viewmodel: HomeViewmodel = viewModel()) {
    val uiState by viewmodel.uiState.collectAsState()
    val qq by viewmodel.userAccount.collectAsState()
    val username by viewmodel.username.collectAsState()
    val uid by viewmodel.uid.collectAsState()

    LaunchedEffect(Unit) { viewmodel.getUserInfo() }
    when (uiState) {
        is MeUiState.Loading -> LoadingCard()
        is MeUiState.Finish -> MeUiLayout(qq,username,uid, (uiState as MeUiState.Finish).meData)
        is MeUiState.LoadError ->{}
    }

}

@Composable
private fun MeUiLayout(qq: String,username: String,uid: String,meData: MeData) {
    LazyColumn() {
        item {
            Column {
                Card(shape = RoundedCornerShape(16.dp)) {
                    UserCard(
                        userAvatarUrl(qq),
                        username = username,
                        uid = uid
                    )
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.post_count_hint,meData.postCount.toString()), modifier = Modifier.weight(1f))
                        Text(stringResource(R.string.likes_count_hint,meData.totalLikes.toString()), modifier = Modifier.weight(1f)) }
                }

            }

        }
        item {
            Spacer(modifier = Modifier.height(4.dp))
            SettingCard(
                icon = R.drawable.fa6solidpen,
                name = stringResource(R.string.change_username_hint),
                description = stringResource(R.string.change_username_description_hint)
            )
        }
    }
}