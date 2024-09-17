package com.klavs.instagramclone.screen

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.klavs.instagramclone.model.UserModel
import com.klavs.instagramclone.viewmodel.EditProfile2ViewModel
import com.klavs.instagramclone.viewmodel.ProfileEditState
import com.klavs.instagramclone.viewmodel.ProfileUpdateState

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun EditProfile2(uid: String, navController: NavHostController) {
    val viewModel: EditProfile2ViewModel = hiltViewModel()

    Scaffold(topBar = { TopBar { navController.popBackStack() } }) { paddingValues ->
        when (val state = viewModel.profileEditState.value) {
            is ProfileEditState.Error -> {
                navController.popBackStack()
                Toast.makeText(
                    navController.context,
                    "Error, please try again later",
                    Toast.LENGTH_SHORT
                ).show()
            }

            ProfileEditState.Loading -> {
                Column(
                    Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            }

            is ProfileEditState.Success -> {
                Content(userModel = state.data!!, paddingValues, navController = navController)
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(navigateBack: () -> Unit = {}) {
    CenterAlignedTopAppBar(title = { Text(text = "Edit Profile") },
        navigationIcon = {
            IconButton(onClick = { navigateBack() }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        })
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun Content(userModel: UserModel, paddingValues: PaddingValues,viewModel: EditProfile2ViewModel= hiltViewModel(),navController: NavHostController) {
    val imageUri = remember {
        mutableStateOf<Uri?>(null)
    }
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                imageUri.value = it
                viewModel.UpdateProfilePicture(ResizeImage(navController.context,it,512))
            }
        }
    )
    val isMediaGranted = remember {
        mutableStateOf(true)
    }


    val mediaPermissionState =
        rememberPermissionState(Manifest.permission.READ_MEDIA_IMAGES)
    if (mediaPermissionState.status.isGranted){
        isMediaGranted.value = true
    }else{
        isMediaGranted.value = false
    }

    val requstPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        isMediaGranted.value = isGranted
        if (isGranted){
            galleryLauncher.launch("image/*")
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(top = paddingValues.calculateTopPadding())) {
        Spacer(modifier = Modifier.padding(10.dp))
        Column(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (viewModel.isPictureDownloaded.value){
                if (viewModel.profilePicture.value!=null){
                    val imageByteArray = viewModel.profilePicture.value
                    val imageBitmap = BitmapFactory.decodeByteArray(imageByteArray,0,imageByteArray!!.size).asImageBitmap()
                    Image(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(
                                Color.Gray,
                                shape = CircleShape
                            )
                            .clickable {
                                if (isMediaGranted.value) {
                                    galleryLauncher.launch("image/*")
                                } else {
                                    requstPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                                }
                            },
                        bitmap = imageBitmap,
                        contentDescription = "",
                        contentScale = ContentScale.Crop
                    )
                }else{
                    Image(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(
                                Color.Gray,
                                shape = CircleShape
                            ),
                        imageVector = Icons.Filled.Face,
                        contentDescription = "",
                        contentScale = ContentScale.Crop
                    )
                }
            }else{
                Image(modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Color.Gray,
                        shape = CircleShape
                    ),
                    imageVector = Icons.Filled.Face,
                    contentDescription = "",
                    contentScale = ContentScale.Crop

                )
            }

            Spacer(modifier = Modifier.padding(4.dp))
            Text(text = "Change Profile Picture", fontSize = 11.sp)

        }
        Spacer(modifier = Modifier.padding(20.dp))
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            val username = remember { mutableStateOf(userModel.username) }
            val usernameIsFocused = remember { mutableStateOf(false) }
            TextField(
                value = username.value, onValueChange = { username.value = it },
                label = { Text(text = "Username", fontSize = 12.sp, color = Color.DarkGray) },
                singleLine = true,
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth()
                    .onFocusChanged {
                        usernameIsFocused.value = it.isFocused
                    },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                textStyle = TextStyle(fontSize = 20.sp),
                trailingIcon = {
                    if (usernameIsFocused.value){
                        when(val updateState = viewModel.profileUpdateState.value){
                            is ProfileUpdateState.Error -> {
                                IconButton(onClick = { viewModel.UpdateUsername(username.value) }) {
                                    Icon(imageVector = Icons.Filled.Refresh, contentDescription = "Save", tint = Color.Red)
                                }
                                println("Error: "+ updateState.message)
                            }
                            ProfileUpdateState.Idle -> {
                                IconButton(onClick = { viewModel.UpdateUsername(username.value) }) {
                                    Icon(imageVector = Icons.Filled.Check, contentDescription = "Save", tint = Color.Blue)
                                }
                            }
                            ProfileUpdateState.Loading -> CircularProgressIndicator()
                        }
                    }
                }
            )
            val biography = remember { mutableStateOf(userModel.biography) }
            val biographyIsFocused = remember { mutableStateOf(false) }
            TextField(
                value = biography.value, onValueChange = { biography.value = it },
                label = { Text(text = "Biography", fontSize = 12.sp, color = Color.DarkGray) },
                singleLine = true,
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth()
                    .onFocusChanged {
                        biographyIsFocused.value = it.isFocused
                    },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                textStyle = TextStyle(fontSize = 20.sp),
                trailingIcon = {
                    if (biographyIsFocused.value){
                        when(val updateState = viewModel.profileUpdateState.value){
                            is ProfileUpdateState.Error -> {
                                IconButton(onClick = { viewModel.UpdateBiography(biography.value) }) {
                                    Icon(imageVector = Icons.Filled.Refresh, contentDescription = "Save", tint = Color.Red)
                                }
                                println("Error: "+ updateState.message)
                            }
                            ProfileUpdateState.Idle -> {
                                IconButton(onClick = { viewModel.UpdateBiography(biography.value) }) {
                                    Icon(imageVector = Icons.Filled.Check, contentDescription = "Save", tint = Color.Blue)
                                }
                            }
                            ProfileUpdateState.Loading -> CircularProgressIndicator()
                        }

                    }
                }
            )
        }

    }
}

@Preview(showBackground = true)
@Composable
fun EditProfile2Preview() {

}