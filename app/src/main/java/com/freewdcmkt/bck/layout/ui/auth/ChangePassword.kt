package com.freewdcmkt.bck.layout.ui.auth

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import com.freewdcmkt.bck.components.freewd.FreewdLoadingDialog
import com.freewdcmkt.bck.util.TokenManager
import com.freewdcmkt.bck.viewmodel.auth.ChangePasswordUiState
import com.freewdcmkt.bck.viewmodel.auth.ChangePasswordViewModel

@Composable
fun ChangePasswordHost(
    viewModel: ChangePasswordViewModel = viewModel(),
    onChangedPassword: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val countdown by viewModel.countdown.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }
    val unknownError = stringResource(R.string.unknown_error)
    LaunchedEffect(uiState) {
        (uiState as? ChangePasswordUiState.Error)?.let { error ->
            if (error.isNoNetWork) snackBarHostState.showSnackbar(unknownError) else snackBarHostState.showSnackbar(
                error.errorMsg ?: unknownError
            )
        }
    }
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.change_password_hint)) }) },
        snackbarHost = { SnackbarHost(snackBarHostState) }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 15.dp)
                .fillMaxSize()
                .verticalScroll(
                    rememberScrollState()
                )
        ) {
            ChangePasswordUi(
                onSendCode = { viewModel.sendCode(it) },
                { qq, password, code -> viewModel.submitPassword(qq, password, code) },
                countdown = countdown
            )
            when (uiState) {
                is ChangePasswordUiState.Loading -> FreewdLoadingDialog(stringResource(R.string.wait_hint))
                is ChangePasswordUiState.OnChangedPassword -> onChangedPassword
                else -> {}
            }
        }
    }
}

@Composable
fun ChangePasswordUi(
    onSendCode: (qq: String) -> Unit,
    onSubmitPassword: (qq: String, password: String, code: String) -> Unit,
    countdown: Int
) {
    var qq by rememberSaveable() { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirmedPassword by rememberSaveable() { mutableStateOf("") }
    var code by rememberSaveable() { mutableStateOf("") }
    Column(modifier = Modifier.fillMaxWidth()) {
        Log.d("CHANGE PASSWORD", TokenManager.getToken() ?: "NULL!!")

        if (TokenManager.getToken().isNullOrEmpty()) OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = qq,
            onValueChange = { qq = it },
            maxLines = 1,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            label = { Text(stringResource(R.string.login_account_hint)) }
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            value = newPassword,
            onValueChange = { newPassword = it },
            label = { Text(stringResource(R.string.new_password_hint)) })

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            value = confirmedPassword,
            onValueChange = { confirmedPassword = it },
            label = { Text(stringResource(R.string.confirm_password_hint)) })

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            OutlinedTextField(
                value = code,
                onValueChange = { input ->
                    code = input.filter { it.isDigit() }.take(6)
                },
                label = { Text(stringResource(R.string.auth_code)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
            Button(
                onClick = { onSendCode(qq) },
                enabled = newPassword == confirmedPassword && newPassword.length >= 8 && countdown == 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Text(
                    stringResource(R.string.auth_code)
                )
            }
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onSubmitPassword(qq, newPassword, code) },
            enabled = code.length == 6 && newPassword == confirmedPassword
        ) {
            Text(
                if (countdown == 0) stringResource(R.string.submit_password_hint) else stringResource(R.string.wait_send_auth_code,countdown)
            )
        }
    }

}