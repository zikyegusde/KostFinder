package com.example.kostfinder.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.kostfinder.AuthViewModel
import com.example.kostfinder.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController, authViewModel: AuthViewModel = viewModel()) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.awan),
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.3f))
        )

        IconBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo2),
                    contentDescription = "Logo KostFinder",
                    modifier = Modifier
                        .size(100.dp)
                        .padding(bottom = 16.dp)
                )
                Text(
                    text = "Selamat Datang Kembali!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Silakan login untuk melanjutkan",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(32.dp))


                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email atau Username") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = "Email Icon")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    // ## PERUBAHAN: Membuat bentuk menjadi bulat ##
                    shape = RoundedCornerShape(50),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFF4FC3F7),
                        focusedLabelColor = Color(0xFF4FC3F7),
                        cursorColor = Color(0xFF4FC3F7),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.7f),
                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                        unfocusedLeadingIconColor = Color.White.copy(alpha = 0.7f),
                        focusedLeadingIconColor = Color(0xFF4FC3F7)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = "Password Icon")
                    },
                    trailingIcon = {
                        val image = if (passwordVisible)
                            Icons.Filled.Visibility
                        else Icons.Filled.VisibilityOff

                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, "Toggle Password Visibility")
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    // ## PERUBAHAN: Membuat bentuk menjadi bulat ##
                    shape = RoundedCornerShape(50),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFF4FC3F7),
                        focusedLabelColor = Color(0xFF4FC3F7),
                        cursorColor = Color(0xFF4FC3F7),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.7f),
                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                        unfocusedLeadingIconColor = Color.White.copy(alpha = 0.7f),
                        unfocusedTrailingIconColor = Color.White.copy(alpha = 0.7f),
                        focusedLeadingIconColor = Color(0xFF4FC3F7),
                        focusedTrailingIconColor = Color(0xFF4FC3F7)
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (isLoading) {
                    CircularProgressIndicator(color = Color.White)
                } else {
                    GradientButton(
                        text = "Masuk",
                        onClick = {
                            if (email.isBlank() || password.isBlank()) {
                                errorMessage = "Email dan password wajib diisi"
                                return@GradientButton
                            }
                            isLoading = true
                            Firebase.auth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val uid = Firebase.auth.currentUser?.uid
                                        if (uid != null) {
                                            authViewModel.fetchUserRole(uid) { role ->
                                                isLoading = false
                                                val destination = if (role == "admin") "admin" else "home"
                                                navController.navigate(destination) {
                                                    popUpTo(0) { inclusive = true }
                                                }
                                            }
                                        }
                                    } else {
                                        isLoading = false
                                        errorMessage = "Login gagal: ${task.exception?.message}"
                                    }
                                }
                        }
                    )
                }

                if (errorMessage.isNotBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
                }

                Spacer(modifier = Modifier.height(16.dp))
                ClickableText(
                    text = AnnotatedString("Belum punya akun? Daftar di sini"),
                    onClick = { navController.navigate("register") },
                    style = TextStyle(
                        color = Color.White,
                        textDecoration = TextDecoration.Underline
                    )
                )
            }
        }
    }
}


@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    gradient: Brush = Brush.horizontalGradient(
        colors = listOf(Color(0xFF03A9F4), Color(0xFF4FC3F7), Color.White)
    )
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isPressed) 0.98f else 1f, label = "scale")

    Surface(
        shape = RoundedCornerShape(50),
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .pointerInput(Unit) {
                this.awaitPointerEventScope {
                    while (true) {
                        awaitFirstDown()
                        isPressed = true
                        waitForUpOrCancellation()
                        isPressed = false
                    }
                }
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        tonalElevation = 4.dp
    ) {
        Box(
            modifier = Modifier
                .background(gradient)
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}


@Composable
fun IconBackground() {
    val icons = listOf(Icons.Default.Key, Icons.Default.Bed, Icons.Default.LocationOn, Icons.Default.Shield)
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    val screenHeightDp = configuration.screenHeightDp.dp

    Box(modifier = Modifier.fillMaxSize()) {
        repeat(25) { index ->
            val iconData = remember(key1 = index) {
                IconData(
                    icon = icons.random(),
                    x = (Random.nextFloat() * screenWidthDp.value).dp,
                    y = (Random.nextFloat() * screenHeightDp.value).dp,
                    size = Dp(Random.nextInt(25, 55).toFloat()),
                    rotation = Random.nextFloat() * 360f
                )
            }
            BackgroundIcon(iconData)
        }
    }
}

@Composable
fun BackgroundIcon(data: IconData) {
    var currentRotation by remember { mutableStateOf(data.rotation) }

    LaunchedEffect(Unit) {
        while (true) {
            currentRotation += 0.1f
            delay(100)
        }
    }

    Icon(
        imageVector = data.icon,
        contentDescription = null,
        tint = Color.White.copy(alpha = 0.12f),
        modifier = Modifier
            .size(data.size)
            .offset(x = data.x, y = data.y)
            .rotate(currentRotation)
            .alpha(0.5f)
    )
}

data class IconData(
    val icon: ImageVector,
    val x: Dp,
    val y: Dp,
    val size: Dp,
    val rotation: Float
)