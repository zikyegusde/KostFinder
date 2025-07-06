package com.example.kostfinder.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.kostfinder.KostViewModel
import com.example.kostfinder.R
import com.example.kostfinder.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    currentName: String,
    currentEmail: String,
    currentImageUrl: String?,
    onSaveClick: (String, String) -> Unit,
    onCancelClick: () -> Unit,
    userViewModel: UserViewModel = viewModel(),
    kostViewModel: KostViewModel = viewModel()
) {
    var name by remember { mutableStateOf(currentName) }
    var email by remember { mutableStateOf(currentEmail) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val isLoading by kostViewModel.isLoading.collectAsState()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> imageUri = uri }
    )

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Edit Profil") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(24.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.Center) {
                AsyncImage(
                    model = imageUri ?: currentImageUrl ?: R.drawable.ic_placeholder,
                    contentDescription = "Foto Profil",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .clickable {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentScale = ContentScale.Crop
                )
            }
            TextButton(onClick = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }) {
                Text("Ganti Foto Profil")
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nama") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                enabled = false
            )
            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        if (imageUri != null) {
                            kostViewModel.uploadImageToCloudinary(imageUri!!) { result ->
                                result.onSuccess { newImageUrl ->
                                    userViewModel.updateUserProfilePicture(newImageUrl, context) { success ->
                                        if (success) {
                                            onSaveClick(name, email)
                                        }
                                    }
                                }
                                result.onFailure {
                                    Toast.makeText(context, "Gagal upload foto: ${it.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        } else {
                            onSaveClick(name, email)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Simpan")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(onClick = onCancelClick, modifier = Modifier.fillMaxWidth()) {
                Text("Batal")
            }
        }
    }
}