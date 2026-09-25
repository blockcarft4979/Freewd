package com.freewdcmkt.bck.layout.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.freewdcmkt.bck.R
import com.freewdcmkt.bck.api.RequestApi
import com.freewdcmkt.bck.components.freewd.FreewdCheckBox
import com.freewdcmkt.bck.components.freewd.FreewdLoadingDialog
import com.freewdcmkt.bck.components.freewd.FreewdTopComponent
import com.freewdcmkt.bck.viewmodel.auth.RegisterUiState
import com.freewdcmkt.bck.viewmodel.auth.RegisterViewmodel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterLayout(
    onToUserAgreement: (String) -> Unit,
    viewmodel: RegisterViewmodel = viewModel()
) {

    val uiState by viewmodel.registerUiState.collectAsState()
    val countdown by viewmodel.countdown.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }

    val unknownError = stringResource(R.string.unknown_error)

    LaunchedEffect(uiState) {
        (uiState as? RegisterUiState.Error)?.let { error ->
            if (error.isNoNetWork) {
                snackBarHostState.showSnackbar(unknownError)
            } else {
                error.msg?.let { snackBarHostState.showSnackbar(it) }
            }
        }
    }

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = { TopAppBar({ Text(stringResource(R.string.register_hint)) }) },
        snackbarHost = { SnackbarHost(snackBarHostState) }) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            RegisterUiLayout(
                onSendCode = { viewmodel.sendCode(it) },
                onRegister = { account, password, code ->
                    viewmodel.register(account, password, code)
                },
                countdown = countdown,
                onToUserAgreement = onToUserAgreement,
            )
            when (uiState) {
                is RegisterUiState.Loading -> FreewdLoadingDialog(stringResource(R.string.wait_hint))
                else -> {}
            }
        }
    }
}

@Composable
private fun RegisterUiLayout(
    onSendCode: (String) -> Unit,
    onRegister: (String, String, String) -> Unit,
    onToUserAgreement: (String) -> Unit,
    countdown: Int
) {
    var isChecked by rememberSaveable() { mutableStateOf(false) }
    var account by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var authCode by rememberSaveable { mutableStateOf("") }
    val userIcon = rememberSaveable() { mutableStateOf("") }
    LaunchedEffect(account) {
        userIcon.value = account
    }
    Column(
        modifier = Modifier
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 15.dp)
    ) {
        FreewdTopComponent(
            userIcon.value,
        )
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
            maxLines = 1,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text(stringResource(R.string.confirm_password_hint)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
            maxLines = 1,
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            OutlinedTextField(
                value = authCode,
                onValueChange = { input ->
                    authCode = input.filter { it.isDigit() }.take(6)
                },
                label = { Text(stringResource(R.string.auth_code)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            Button(
                onClick = { onSendCode(account) },
                enabled = countdown == 0 && account != "",
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (countdown == 0) Text(stringResource(R.string.send_auth_code)) else Text(
                    stringResource(R.string.wait_send_auth_code, countdown)
                )
            }
        }
        FreewdCheckBox(
            checked = isChecked, stringResource(R.string.agree_agreement_part),
            onCheckBoxChanged = {
                isChecked = it
                if (it) onToUserAgreement(RequestApi.Document.USER_AGREEMENT)
            })

        Button(
            enabled = password == confirmPassword && password.length >= 8 && authCode.length == 6 && isChecked,
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
