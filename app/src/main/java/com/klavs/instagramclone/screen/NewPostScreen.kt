package com.klavs.instagramclone.screen

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.klavs.instagramclone.model.ScreenModel
import com.klavs.instagramclone.viewmodel.NewPostViewModel
import com.klavs.instagramclone.viewmodel.UploadingPostStatements
import androidx.hilt.navigation.compose.hiltViewModel
import java.io.ByteArrayOutputStream

fun ResizeImage(context: Context, imageUri: Uri, maxSize: Int): ByteArray {
    val inputStream = context.contentResolver.openInputStream(imageUri)
    val originalBitmap = BitmapFactory.decodeStream(inputStream)

    // EXIF verisini okuma
    val exif = ExifInterface(context.contentResolver.openInputStream(imageUri)!!)
    val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)

    // Bitmap'in boyutlarını ayarlama
    var width = originalBitmap.width
    var height = originalBitmap.height
    val bitmapOrani: Double = width.toDouble() / height.toDouble()

    if (bitmapOrani > 1) {
        width = maxSize
        height = (width / bitmapOrani).toInt()
    } else {
        height = maxSize
        width = (height * bitmapOrani).toInt()
    }

    // Bitmap'i yeniden boyutlandırma
    val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, true)

    // Resmin EXIF verisine göre döndürülmesi
    val correctedBitmap = when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(resizedBitmap, 90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(resizedBitmap, 180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(resizedBitmap, 270f)
        else -> resizedBitmap
    }

    // ByteArray'e dönüştürme
    val outputStream = ByteArrayOutputStream()
    correctedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
    return outputStream.toByteArray()
}

// Bitmap'i döndüren fonksiyon
fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
    val matrix = Matrix()
    matrix.postRotate(angle)
    return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
}


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun NewPostScreen(navController: NavHostController,
                  viewModel: NewPostViewModel = hiltViewModel()) {

    var imageSelected by remember {
        mutableStateOf(false)
    }
    var imageOfPost by remember {
        mutableStateOf<Uri?>(null)
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                imageOfPost = it
                imageSelected = true
            }
        }
    )
    var isMediaGranted by remember {
        mutableStateOf(false)
    }

    val mediaPermissionState =
        rememberPermissionState(Manifest.permission.READ_MEDIA_IMAGES)
    val requstPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            galleryLauncher.launch("image/*")
            isMediaGranted = true
        } else {
            isMediaGranted = false
        }

    }
    LaunchedEffect(mediaPermissionState) {
        println("izin istendi")
        requstPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
    }

    if (isMediaGranted) {

    } else {
        Text(text = "Access Denied", fontSize = 20.sp)
    }
    if (imageSelected) {
        when (val state = viewModel.statement.value) {
            is UploadingPostStatements.Error -> {
                val message = state.message
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(text = "An error ocured: " + message)
                    Spacer(modifier = Modifier.padding(10.dp))
                    OutlinedButton(onClick = {
                        navController.navigate(ScreenModel.Home.route) {
                            popUpTo(ScreenModel.Home.route) {
                                inclusive = true
                            }
                        }
                    }) {
                        Text(text = "Go Home")
                    }
                }
            }

            UploadingPostStatements.Idle -> {
                val imageBtyeArray = ResizeImage(LocalContext.current, imageOfPost!!, 1024)
                SharePost(imageBtyeArray, navController)}
            UploadingPostStatements.Loading -> {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(text = "Sharing...")
                    Spacer(modifier = Modifier.padding(10.dp))
                    CircularProgressIndicator()
                }
            }

            UploadingPostStatements.Success -> {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(imageVector = Icons.Filled.CheckCircle, contentDescription = "Success")
                }
            }
        }

    }
}

@Composable
fun SharePost(imageOfPost: ByteArray?, navController: NavHostController, viewModel: NewPostViewModel = hiltViewModel()) {

    var description by remember {
        mutableStateOf("")
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = rememberAsyncImagePainter(imageOfPost),
            contentDescription = "sharing image",
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(300.dp)
        )
        Spacer(modifier = Modifier.padding(20.dp))
        OutlinedTextField(
            value = description, onValueChange = {
                if (it.length <= 100) {
                    description = it
                }
            },
            placeholder = { Text(text = "Write a caption...") },
            maxLines = 3,
            modifier = Modifier.fillMaxWidth(0.9f)
        )

        Spacer(modifier = Modifier.padding(20.dp))
        OutlinedButton(onClick = {
            viewModel.UploadPost(
                navController,
                imageOfPost!!,
                description
            )
        }) {
            Text(text = "Share")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NewPostScreenPreview() {
    // SharePost(imageOfPost = Uri.EMPTY)
}

