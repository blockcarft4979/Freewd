package com.freewdcmkt.bck.layout.ui.auth

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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.freewdcmkt.bck.R
import com.freewdcmkt.bck.components.freewd.FreewdLoadingDialog
import com.freewdcmkt.bck.components.freewd.FreewdTopComponent
import com.freewdcmkt.bck.data.screen.AboutScreenData
import com.freewdcmkt.bck.data.screen.LoginScreenData
import com.freewdcmkt.bck.data.screen.RegisterScreenData
import com.freewdcmkt.bck.layout.ui.other.Document
import com.freewdcmkt.bck.viewmodel.auth.LogInViewModel
import com.freewdcmkt.bck.viewmodel.auth.LoginUiState
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun LoginLayout(viewModel: LogInViewModel = viewModel()) {

    val uiState by viewModel.loginUiState.collectAsState()
    val navCollection = rememberNavController()
    val navToRegister = { navCollection.navigate(RegisterScreenData) }
    val noAccountOrPasswordHint = stringResource(R.string.login_password_or_account_needed)
    val unknownError = stringResource(R.string.unknown_error)
    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        (uiState as? LoginUiState.Error)?.let { error ->
            if (error.isNoNetWork) {
                snackBarHostState.showSnackbar(unknownError)
            } else {
                error.msg?.let { snackBarHostState.showSnackbar(it) }
            }
        }
    }

    NavHost(navCollection, startDestination = LoginScreenData) {
        composable<LoginScreenData> {
            Scaffold(
                modifier = Modifier.imePadding(),
                topBar = {
                    TopAppBar(title = { Text(stringResource(R.string.login_login_btn)) })
                },
                snackbarHost = { SnackbarHost(snackBarHostState) }
            ) { innerPadding ->
                Column(modifier = Modifier.padding(innerPadding)) {
                    LoginLayout(
                        onRegister = navToRegister,
                        onLogin = { account, password ->
                            if (account.isNotEmpty() && password.isNotEmpty()) run {
                                viewModel.fetchData(
                                    password,
                                    account
                                )
                            }
                            else {
                                scope.launch {
                                    snackBarHostState.showSnackbar(
                                        noAccountOrPasswordHint
                                    )
                                }
                            }
                        },
                        onToUserAgreement = { navCollection.navigate(AboutScreenData(it)) },
                        onToPolicyPrivacy = { navCollection.navigate(AboutScreenData(it)) }
                    )
                    when (uiState) {
                        is LoginUiState.Loading -> FreewdLoadingDialog(stringResource(R.string.logging_in_hint))
                        else -> {}
                    }
                }
            }
        }
        composable<RegisterScreenData> {
            RegisterLayout(
                onToUserAgreement = { navCollection.navigate(AboutScreenData(it)) },
                onToPolicyPrivacy = { navCollection.navigate(AboutScreenData(it)) }
            )
        }
        composable<AboutScreenData> { backStack ->
            val arg = backStack.toRoute<AboutScreenData>()
            Document(
                onBack = { navCollection.popBackStack() },
                arg.url
            )
        }
    }

}

@Composable
fun LoginLayout(
    onRegister: () -> Unit,
    onLogin: (account: String, password: String) -> Unit,
    onToUserAgreement: (String) -> Unit,
    onToPolicyPrivacy: (String) -> Unit
) {
    var account by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val userIcon = rememberSaveable() { mutableStateOf("") }
    LaunchedEffect(account) {
        userIcon.value = account
    }
    Surface(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .padding(start = 15.dp, end = 15.dp)
                .imePadding()
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            FreewdTopComponent(
                userIcon.value,
                onToUserAgreement = onToUserAgreement,
                onToPolicyPrivacy = onToPolicyPrivacy
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
                modifier = Modifier
                    .fillMaxWidth()
            )

            Button(
                onClick = {
                    onLogin(account, password)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                enabled = password.isNotEmpty() && account.isNotEmpty()
            ) {
                Text(stringResource(R.string.login_login_btn))
            }
            TextButton(onClick = onRegister, modifier = Modifier.fillMaxWidth()) {
                Text(
                    stringResource(R.string.login_register_btn),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

        }
    }

}
