package com.klavs.instagramclone.screen


import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.OutlinedButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.klavs.instagramclone.ui.theme.TextFieldBackground
import com.klavs.instagramclone.viewmodel.EditProfileViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun EditProfileScreen(
    navController: NavHostController,
    viewmodel: EditProfileViewModel = hiltViewModel()
) {
    var imageUri by remember {
        mutableStateOf<Uri?>(null)
    }
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                imageUri = it
            }
        }
    )
    var isMediaGranted by remember {
        mutableStateOf(false)
    }

    val mediaPermissionState =
        rememberPermissionState(Manifest.permission.READ_MEDIA_IMAGES)
    if (mediaPermissionState.status.isGranted){
        isMediaGranted = true
    }
    val requstPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        isMediaGranted = isGranted

    }
    LaunchedEffect(Unit) {
        println("izin istendi")
        requstPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFB3E5FC), Color(0xAFE1F5FE))
                )
            )
    ) {
        Spacer(modifier = Modifier.padding(25.dp))
        Text(
            text = "Add a profile picture",
            fontSize = 30.sp,
            fontFamily = FontFamily.Serif
        )
        Spacer(modifier = Modifier.padding(10.dp))
        Image(painter = imageUri?.let { rememberAsyncImagePainter(it) }
            ?: rememberVectorPainter(Icons.Filled.Person),
            contentScale = ContentScale.Crop,
            contentDescription = "add",
            modifier = Modifier
                .background(Color.LightGray, CircleShape)
                .size(250.dp)
                .clip(CircleShape)
                .clickable {
                    if (isMediaGranted) {
                        galleryLauncher.launch("image/*")
                    }else{
                        mediaPermissionState.launchPermissionRequest()
                    }
                }
        )
        Spacer(modifier = Modifier.padding(25.dp))
        Text(
            text = "Biography",
            fontSize = 30.sp
        )
        Spacer(modifier = Modifier.padding(10.dp))
        var text by remember {
            mutableStateOf("")
        }
        val maxChar = 130
        BiographyTextField(text) {
            if (it.length <= maxChar) {
                text = it
            }
        }
        Spacer(modifier = Modifier.padding(10.dp))
        when (viewmodel.isLoading.value) {
            true -> {
                CircularProgressIndicator()
            }

            false -> {
                if (viewmodel.isUploaded.value) {
                    navController.navigate("app_main")
                }
                var imageByteArray: ByteArray? = null
                if (imageUri!=null){
                    imageByteArray = ResizeImage(LocalContext.current, imageUri!!, 512)
                }

                    SubmitButton(onClickListener = {
                        viewmodel.SubmitAction(
                            biography = text,
                            profilePictureUri = imageByteArray
                        )
                    })



            }
        }

    }
}


@Composable
fun SubmitButton(onClickListener: () -> Unit) {
    OutlinedButton(
        onClick = onClickListener,
        contentPadding = PaddingValues(
            start = 20.dp,
            top = 10.dp,
            bottom = 10.dp,
            end = 20.dp
        ),
        shape = MaterialTheme.shapes.extraLarge,
        border = BorderStroke(1.dp, Color.Black)
    ) {
        Text(
            text = "Finish",
            fontSize = 30.sp
        )
    }
}

@Composable
fun BiographyTextField(text: String, onValueChange: (String) -> Unit) {
    TextField(
        value = text,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = "Write something about yourself..",
                color = Color.Gray
            )
        },
        minLines = 2,
        maxLines = 4,
        modifier = Modifier
            .border(1.dp, Color.Black)
            .width(300.dp)
            .widthIn(300.dp, 300.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = TextFieldBackground
        )
    )
}


@Preview(showBackground = true)
@Composable
fun defaultPreview() {
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        SubmitButton({})
    }

}

