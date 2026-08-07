package com.freewdcmkt.bck.layout.ui

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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.freewdcmkt.bck.R
import com.freewdcmkt.bck.components.LoadingCard
import com.freewdcmkt.bck.components.freewd.FreewdTopComponent
import com.freewdcmkt.bck.data.screen.LoginScreenData
import com.freewdcmkt.bck.data.screen.RegisterScreenData
import com.freewdcmkt.bck.viewmodel.LogInViewModel
import com.freewdcmkt.bck.viewmodel.LoginUiState
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun LoginLayout(viewModel: LogInViewModel = viewModel()) {
    val context = LocalContext.current
    val uiState by viewModel.loginUiState.collectAsState()
    val navCollection = rememberNavController()
    val noAccountOrPasswordHint = stringResource(R.string.login_password_or_account_needed)
    val noNetworkHint = stringResource(R.string.no_internet_hint)
    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.Error) {
            if ((uiState as LoginUiState.Error).isNoNetWork) {
                scope.launch { snackBarHostState.showSnackbar(noNetworkHint) }
            } else (uiState as LoginUiState.Error).msg?.let { snackBarHostState.showSnackbar(it) }

        }
    }
    NavHost(navCollection, startDestination = LoginScreenData) {
        composable<LoginScreenData> {
            Scaffold(
                modifier = Modifier.imePadding(),
                topBar = {
                    TopAppBar(title = { Text(stringResource(R.string.app_name)) })
                },
                snackbarHost = { SnackbarHost(snackBarHostState) }
            ) { innerPadding ->
                Column(modifier = Modifier.padding(innerPadding)) {
                    when (uiState) {
                        is LoginUiState.Success -> {
                            viewModel.backToNoAction()
                        }

                        is LoginUiState.Error -> {
                            LoginLayout(onRegister = {}, onLogin = { account, password ->
                                run { viewModel.fetchData(password, account) }
                            })

                        }

                        is LoginUiState.NoAction -> {
                            LoginLayout(
                                onRegister = { navCollection.navigate(RegisterScreenData) },
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
                                })
                        }

                        is LoginUiState.Loading -> LoadingCard()
                    }
                }
            }
        }
        composable<RegisterScreenData> { RegisterLayout() }
    }

}

@Composable
fun LoginLayout(onRegister: () -> Unit, onLogin: (account: String, password: String) -> Unit) {
    var account by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            )

            Button(
                onClick = {
                    onLogin(account, password)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            ) {
                Text(stringResource(R.string.login_login_btn))
            }
            Button(
                onClick = onRegister,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            ) {
                Text(stringResource(R.string.login_register_btn))
            }
        }
    }

}
