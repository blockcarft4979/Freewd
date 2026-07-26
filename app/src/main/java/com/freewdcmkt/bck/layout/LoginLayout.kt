package com.freewdcmkt.bck.layout

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.freewdcmkt.bck.R
import com.freewdcmkt.bck.components.LoadingCard
import com.freewdcmkt.bck.viewmodel.LogInViewModel
import com.freewdcmkt.bck.viewmodel.LoginUiState

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun LoginLayout(viewModel: LogInViewModel = viewModel()) {
    val context = LocalContext.current
    val uiState by viewModel.loginUiState.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.app_name)) })
        },
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            when (uiState) {
                is LoginUiState.Success -> {
                    Toast.makeText(
                        context,
                        stringResource(R.string.login_success),
                        Toast.LENGTH_SHORT
                    )
                }

                is LoginUiState.Error -> {
                    LoginLayout(onRegister = {}, onLogin = { account, password ->
                        run { viewModel.fetchData(password, account) }
                    })
                    //Log.d("LOGIN LAYOUT",(uiState as LoginUiState.Error).msg)
                    Toast.makeText(context, (uiState as LoginUiState.Error).msg, Toast.LENGTH_SHORT)
                        .show()
                }

                is LoginUiState.NoAction -> {
                    LoginLayout(onRegister = {}, onLogin = { account, password ->
                        if (account.isNotEmpty() && password.isNotEmpty()) run {
                            viewModel.fetchData(
                                password,
                                account
                            )
                        }
                        else {
                           // val message = stringResource(R.string.login_password_or_account_needed)
                            Toast.makeText(
                                context,
                                (R.string.login_password_or_account_needed),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
                }

                is LoginUiState.Loading -> LoadingCard()
            }
        }

    }
}

@Composable
fun LoginLayout(onRegister: () -> Unit, onLogin: (account: String, password: String) -> Unit) {
    var account  by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    Surface(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .padding(start = 15.dp, end = 15.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier
                        .size(40.dp)
                        .padding(5.dp),
                    painter = painterResource(R.mipmap.ic_launcher_monochrome),
                    contentDescription = null
                )
                Text(
                    modifier = Modifier.padding(start = 4.dp),
                    text = stringResource(R.string.login_welcome_title),
                    //modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
            Text(
                text = stringResource(R.string.login_hint),
                fontWeight = FontWeight.Thin,
                fontSize = 15.sp
            )
            OutlinedTextField(
                value = account,
                onValueChange = { account = it },
                label = { Text(stringResource(R.string.login_account_hint)) },
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
                onClick = {
                    onRegister()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            ) {
                Text(stringResource(R.string.login_register_btn))
            }
        }
    }
}
