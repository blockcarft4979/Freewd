package com.freewdcmkt.bck.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.freewdcmkt.bck.R
import com.freewdcmkt.bck.components.freewd.FreewdTopComponent
import com.freewdcmkt.bck.components.LoadErrorUiLayout
import com.freewdcmkt.bck.components.LoadingCard
import com.freewdcmkt.bck.viewmodel.RegisterUiState
import com.freewdcmkt.bck.viewmodel.RegisterViewmodel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterLayout(viewmodel: RegisterViewmodel = viewModel()) {
    val uiState by viewmodel.registerUiState.collectAsState()
    val countdown by viewmodel.countdown.collectAsState()

    Scaffold(topBar = { TopAppBar({ Text(stringResource(R.string.register_hint)) }) }) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            RegisterUiLayout(
                onSendCode = { viewmodel.sendCode(it) },
                onRegister = { account, password, code ->
                    viewmodel.register(account, password, code)
                },
                countdown = countdown,
            )

            when (uiState) {
                is RegisterUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        LoadingCard()
                    }
                }

                is RegisterUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f))
                    ) {
                        LoadErrorUiLayout(
                            onClick = { viewmodel.resetState() },
                            msg = (uiState as RegisterUiState.Error).msg,
                            buttonMsg = stringResource(R.string.retry_hint),
                        )
                    }
                }

                else -> { /* 什么也不显示 */
                }
            }
        }
    }
}

@Composable
private fun RegisterUiLayout(
    onSendCode: (String) -> Unit,
    onRegister: (String, String, String) -> Unit,
    countdown: Int
) {
    var account by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var authCode by rememberSaveable { mutableStateOf("") }
    Column(
        modifier = Modifier
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 15.dp)
    ) {
        FreewdTopComponent()
        OutlinedTextField(
            value = account,
            onValueChange = { account = it },
            label = { Text(stringResource(R.string.login_account_hint)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.login_password_hint)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text(stringResource(R.string.confirm_password_hint)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = authCode,
            onValueChange = { authCode = it },
            label = { Text(stringResource(R.string.auth_code)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = { onSendCode(account) },
            enabled = countdown == 0,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (countdown == 0) Text(stringResource(R.string.send_auth_code)) else Text(
                stringResource(R.string.wait_send_auth_code, countdown)
            )
        }
        Button(
            enabled = password == confirmPassword && password.length >= 8 && authCode.length == 6,
            onClick = {
                onRegister(
                    account,
                    password,
                    authCode
                )
            }, modifier = Modifier.fillMaxWidth()
        ) { Text(stringResource(R.string.register_hint)) }
    }


}
