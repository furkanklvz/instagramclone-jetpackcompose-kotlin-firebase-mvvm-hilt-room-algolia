package com.klavs.instagramclone.screen

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.klavs.instagramclone.model.MessageModel
import com.klavs.instagramclone.model.UserModel
import com.klavs.instagramclone.ui.theme.MessageBGBlue
import com.klavs.instagramclone.ui.theme.MessageBGPurple
import com.klavs.instagramclone.util.FirebaseObject
import com.klavs.instagramclone.viewmodel.ChatScreenViewModel
import com.klavs.instagramclone.viewmodel.MessagingUserProfilePictureStatements
import com.klavs.instagramclone.viewmodel.MessagingUserStatements
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatScreen(
    chatID: String,
    firstNavController: NavHostController,
    viewModel: ChatScreenViewModel = hiltViewModel(),
    paddingValues: PaddingValues
) {

    LaunchedEffect(Unit) {
        chatID.split("_").forEach {
            if (it != FirebaseObject.auth.currentUser?.uid) {
                viewModel.RemoveMessageListener(chatID)
                viewModel.GetMessagingUser(it)
                viewModel.GetMessages(chatID)
                return@forEach
            }
        }
    }
    when (val state = viewModel.messagingUserState.value) {
        is MessagingUserStatements.Error -> firstNavController.popBackStack()
        MessagingUserStatements.Loading -> {
            Column(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is MessagingUserStatements.Success -> {
            Scaffold(topBar = {
                TopBar(
                    user = state.userData!!,
                    NavigateToUserProfilePage = { firstNavController.navigate("user_profile_page/${state.userData.username}") },
                    navigateBack = {
                        firstNavController.popBackStack()
                        viewModel.RemoveMessageListener(chatID)
                    }
                )
            }, modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding())
            ) {
                Content(paddingValues = it, chatID = chatID)
            }
        }
    }


}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    user: UserModel,
    viewModel: ChatScreenViewModel = hiltViewModel(),
    NavigateToUserProfilePage: () -> Unit = {},
    navigateBack: () -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.GetMessagingUserProfilePicture(user.uid)
    }

    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable {
                NavigateToUserProfilePage()
            }) {
                Spacer(modifier = Modifier.padding(5.dp))
                when (val imageState = viewModel.messagingUserProfilePictureState.value) {
                    is MessagingUserProfilePictureStatements.Error -> {
                            Image(
                                imageVector = Icons.Filled.Face,
                                contentDescription = "null",
                                modifier = Modifier
                                    .size(35.dp)
                                    .clip(CircleShape)
                                    .background(Color.LightGray, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                    }
                    MessagingUserProfilePictureStatements.Loading -> {
                        Image(
                            imageVector = Icons.Filled.Face,
                            contentDescription = "null",
                            modifier = Modifier
                                .size(35.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }

                    is MessagingUserProfilePictureStatements.Success -> {
                        val image = BitmapFactory.decodeByteArray(
                            imageState.profilePictureData,
                            0,
                            imageState.profilePictureData!!.size
                        ).asImageBitmap()
                        Image(
                            bitmap = image,
                            contentDescription = "profile picture",
                            modifier = Modifier
                                .size(35.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                Spacer(modifier = Modifier.padding(5.dp))
                Text(text = user.username)
            }

        },
        navigationIcon = {
            IconButton(onClick = navigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Localized description"
                )
            }
        }
    )

}


@Composable
private fun Content(
    paddingValues: PaddingValues,
    chatID: String,
    viewModel: ChatScreenViewModel = hiltViewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = paddingValues.calculateTopPadding()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.padding(5.dp))
        LazyColumn(
            Modifier
                .weight(1f),
            reverseLayout = true
        ) {
            items(viewModel.messageList) {
                MessageRow(it)
            }
        }
        Spacer(modifier = Modifier.padding(5.dp))
        val value = remember { mutableStateOf("") }
        OutlinedTextField(
            value = value.value,
            onValueChange = { value.value = it },
            shape = CircleShape,
            placeholder = { Text(text = "Message...") },
            singleLine = true,
            trailingIcon = {
                Icon(imageVector = Icons.Filled.Send,
                    contentDescription = "send",
                    Modifier.clickable {
                        viewModel.SendMessage(message = value.value, chatID = chatID)
                        value.value = ""
                    })
            },
            modifier = Modifier
                .imePadding()
                .border(1.dp, Color.Black, CircleShape)
                .fillMaxWidth(0.95f)
        )
    }
}

@Composable
private fun MessageRow(message: MessageModel, viewModel: ChatScreenViewModel = hiltViewModel()) {
    if (message.sender == FirebaseObject.auth.currentUser?.uid) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = message.message!!,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .background(MessageBGPurple, CircleShape)
                        .padding(6.dp),
                    fontSize = 21.sp
                )
                Spacer(modifier = Modifier.padding(2.dp))
                Text(text = GetHourFromDate(Date(message.time!!)), fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.padding(5.dp))
        }
    } else {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.padding(5.dp))
            when (val state = viewModel.messagingUserProfilePictureState.value) {
                is MessagingUserProfilePictureStatements.Error -> {
                    Icon(
                        imageVector = Icons.Filled.Face,
                        contentDescription = "pp",
                        modifier = Modifier.size(35.dp)
                    )
                }

                MessagingUserProfilePictureStatements.Loading -> {}
                is MessagingUserProfilePictureStatements.Success -> {
                    val imageBitmap = BitmapFactory.decodeByteArray(
                        state.profilePictureData,
                        0,
                        state.profilePictureData!!.size
                    ).asImageBitmap()
                    Image(
                        bitmap = imageBitmap,
                        contentDescription = "pp",
                        modifier = Modifier
                            .size(35.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.padding(5.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = message.message!!,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .background(MessageBGBlue, CircleShape)
                        .padding(6.dp),
                    fontSize = 21.sp
                )
                Spacer(modifier = Modifier.padding(2.dp))
                Text(text = GetHourFromDate(Date(message.time!!)), fontSize = 12.sp)
            }

        }
    }


}

fun GetHourFromDate(date: Date): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(date)
}


@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = "merhaba",
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .background(MessageBGBlue, CircleShape)
                        .padding(6.dp),
                    fontSize = 21.sp
                )
                Spacer(modifier = Modifier.padding(2.dp))
                Text(text = "12.25", fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.padding(5.dp))
        }
    }

}