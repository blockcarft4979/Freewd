package com.freewdcmkt.bck.layout.ui.auth

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.freewdcmkt.bck.R
import com.freewdcmkt.bck.api.RequestApi
import com.freewdcmkt.bck.components.freewd.FreewdCheckBox
import com.freewdcmkt.bck.components.freewd.FreewdLoadingDialog
import com.freewdcmkt.bck.components.freewd.FreewdTopComponent
import com.freewdcmkt.bck.viewmodel.auth.LogInViewModel
import com.freewdcmkt.bck.viewmodel.auth.LoginUiState


@Composable
fun LoginLayout(
    onRegister: () -> Unit,
    onChangePassword: () -> Unit,
    onLogin: (account: String, password: String) -> Unit,
    onToUserAgreement: (String) -> Unit,
    viewModel: LogInViewModel = viewModel()
) {
    val uiState by viewModel.loginUiState.collectAsState()
    var isChecked by rememberSaveable() { mutableStateOf(false) }
    val unknownError = stringResource(R.string.unknown_error)
    val snackBarHostState = remember { SnackbarHostState() }
    var account by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val userIcon = rememberSaveable() { mutableStateOf("") }
    LaunchedEffect(uiState) {
        Log.d("LOGIN UI STATE",uiState.toString())
        (uiState as? LoginUiState.Error)?.let { error ->
            if (error.isNoNetWork) snackBarHostState.showSnackbar(unknownError) else error.msg?.let {
                snackBarHostState.showSnackbar(
                    it
                )
            }
        }
    }

    LaunchedEffect(account) {
        userIcon.value = account
    }

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.login_login_btn)) })
        },
        snackbarHost = { SnackbarHost(snackBarHostState) }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(start = 15.dp, end = 15.dp)
                .imePadding()
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            FreewdTopComponent(userIcon.value)
            OutlinedTextField(
                value = account,
                maxLines = 1,
                onValueChange = { input ->
                    val filteredInput = input.filter { it.isDigit() }
                    if (filteredInput.length <= 12) {
                        account = filteredInput
                    }
                },
                label = { Text(stringResource(R.string.login_account_hint)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = password,
                maxLines = 1,
                onValueChange = { password = it },
                label = { Text(stringResource(R.string.login_password_hint)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
            )

            Button(
                onClick = {
                    onLogin(account, password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                enabled = password.isNotEmpty() && account.isNotEmpty() && isChecked
            ) {
                Text(stringResource(R.string.login_login_btn))
            }
            FreewdCheckBox(
                checked = isChecked,
                text = stringResource(R.string.agree_agreement_part),
                onCheckBoxChanged = {
                    isChecked = it
                    if (it) onToUserAgreement(RequestApi.Document.USER_AGREEMENT)
                })
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.5f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(0.8f)),
                    onClick = onRegister,
                ) {
                    Text(
                        stringResource(R.string.register_hint),
                    )
                }
                TextButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.7f),
                    onClick = onChangePassword,
                ) {
                    Text(
                        stringResource(R.string.forget_password_hint), fontSize = 12.sp,
                    )
                }


            }

            when (uiState) {
                is LoginUiState.Loading -> FreewdLoadingDialog(stringResource(R.string.logging_in_hint))
                else -> {}
            }
        }
    }
}
