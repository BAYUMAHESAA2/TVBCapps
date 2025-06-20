package com.tvbc.tvbcapps.ui.theme.screen

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.tvbc.tvbcapps.R
import com.tvbc.tvbcapps.model.AuthState
import com.tvbc.tvbcapps.model.AuthViewModel
import com.tvbc.tvbcapps.navigation.Screen
import com.tvbc.tvbcapps.ui.theme.TVBCappsTheme
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavHostController) {
    Scaffold { innerPadding ->
        LoginScreenContent(
            modifier = Modifier.padding(innerPadding),
            navController
        )
    }
}

@Composable
fun LoginScreenContent(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showForgetPasswordDialog by remember { mutableStateOf(false) }
    var resetEmailSent by remember { mutableStateOf(false) }

    val loginState by viewModel.loginState.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    val resetPasswordState by viewModel.resetPasswordState.collectAsState()
    val scope = rememberCoroutineScope()

    // Observasi state login
    LaunchedEffect(loginState) {
        when (loginState) {
            is AuthState.Loading -> isLoading = true
            is AuthState.Success -> {
                isLoading = false

                // Navigasi berdasarkan role dari Firestore
                if (userRole == "admin") {
                    navController.navigate(Screen.AdminHomeScreen.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                } else {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
                viewModel.resetStates()
            }
            is AuthState.Error -> {
                isLoading = false
                errorMessage = (loginState as AuthState.Error).message
            }
            else -> {}
        }
    }

    // Observasi state reset password
    LaunchedEffect(resetPasswordState) {
        when (resetPasswordState) {
            is AuthState.Success -> {
                resetEmailSent = true
                showForgetPasswordDialog = false
                viewModel.resetPasswordState()
            }
            is AuthState.Error -> {
                errorMessage = (resetPasswordState as AuthState.Error).message
                viewModel.resetPasswordState()
            }
            else -> {}
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 35.dp)
            .padding(start = 16.dp, end = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Image(
            painter = painterResource(R.drawable.logologin),
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp),
            contentDescription = "Gambar Login"
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Enter your email") },
            singleLine = true,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Filled.Email,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(25.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            trailingIcon = {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Error message
        errorMessage?.let {
            Text(
                text = it,
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Success message untuk reset password
        if (resetEmailSent) {
            Text(
                text = "Email reset password telah dikirim! Silakan cek email Anda.",
                color = Color.Green,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text = "Forget password?",
                color = Color(0xFF660000),
                modifier = Modifier.clickable {
                    showForgetPasswordDialog = true
                }
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Row (
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ){
            Text("New member? ")
            Text(
                text = "Register now",
                color = Color(0xFF660000),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .size(width = if (index == 2) 24.dp else 17.dp, height = 8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (index == 2) Color(0xFF000000) else Color(0xFF660000))
                )
                Spacer(modifier = Modifier.width(3.dp))
            }

            Spacer(modifier = Modifier.weight(1f))

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(60.dp),
                    color = Color(0xFF660000)
                )
            } else {
                Button(
                    onClick = {
                        scope.launch {
                            errorMessage = null
                            resetEmailSent = false
                            if (email.isNotBlank() && password.isNotBlank()) {
                                viewModel.loginUser(email, password)
                            } else {
                                errorMessage = "Email dan password tidak boleh kosong"
                            }
                        }
                    },
                    enabled = email.isNotEmpty() && password.isNotEmpty(),
                    shape = CircleShape,
                    modifier = Modifier.size(60.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF660000))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
        }
    }

    // Dialog untuk forget password
    if (showForgetPasswordDialog) {
        ForgetPasswordDialog(
            onDismiss = { showForgetPasswordDialog = false },
            onSendResetEmail = { resetEmail ->
                viewModel.sendPasswordResetEmail(resetEmail)
            }
        )
    }
}

@Composable
fun ForgetPasswordDialog(
    onDismiss: () -> Unit,
    onSendResetEmail: (String) -> Unit
) {
    var resetEmail by remember { mutableStateOf("") }
    var isValidEmail by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Reset Password",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column {
                Text(
                    text = "Masukkan email Anda untuk menerima link reset password",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = resetEmail,
                    onValueChange = {
                        resetEmail = it
                        isValidEmail = android.util.Patterns.EMAIL_ADDRESS.matcher(it).matches()
                    },
                    label = { Text("Email") },
                    singleLine = true,
                    isError = !isValidEmail && resetEmail.isNotEmpty(),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Email,
                            contentDescription = null
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (!isValidEmail && resetEmail.isNotEmpty()) {
                    Text(
                        text = "Format email tidak valid",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (resetEmail.isNotBlank() && isValidEmail) {
                        onSendResetEmail(resetEmail)
                    }
                },
                enabled = resetEmail.isNotBlank() && isValidEmail
            ) {
                Text("Kirim", color = Color(0xFF660000))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun LoginScreenPreview() {
    TVBCappsTheme {
        LoginScreen(rememberNavController())
    }
}