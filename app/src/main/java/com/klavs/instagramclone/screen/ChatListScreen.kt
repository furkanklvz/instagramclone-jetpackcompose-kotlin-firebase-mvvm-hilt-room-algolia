package com.klavs.instagramclone.screen

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.firebase.Timestamp
import com.klavs.instagramclone.model.ChatItemModel
import com.klavs.instagramclone.viewmodel.ChatListStatement
import com.klavs.instagramclone.viewmodel.ChatListViewModel
import java.util.Date
import java.util.concurrent.TimeUnit


@Composable
fun ChatList(navController: NavHostController,firstNavController: NavHostController, viewModel: ChatListViewModel = hiltViewModel()) {
    LaunchedEffect(Unit) {
        viewModel.GetList()
    }
    Scaffold(topBar = { ChatListScreenTopBar { navController.popBackStack() } }) { paddingValues ->
        when (val state = viewModel.chatListState.value) {
            ChatListStatement.Empty -> {
                Column(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "No Chats", fontSize = 36.sp)
                }
            }

            is ChatListStatement.Error -> {
                println("hata: " + state.message!!)
            }

            ChatListStatement.Loading -> {
                println("yükleniyor")
            }

            is ChatListStatement.Success -> {
                Content(chatList = state.data!!, paddingValues = paddingValues){
                    firstNavController.navigate("chat_screen/${it}")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreenTopBar(navigateBack: () -> Unit = {}) {
    TopAppBar(
        title = {
            Text(
                text = "Chats",
                fontSize = 28.sp
            )
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
private fun Content(chatList: List<ChatItemModel>, paddingValues: PaddingValues,NavigateToChatScreen: (String) -> Unit = {}) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(PaddingValues(top = paddingValues.calculateTopPadding())),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(chatList) {
            Spacer(modifier = Modifier.padding(6.dp))
            ChatListItem(it,NavigateToChatScreen=NavigateToChatScreen)
        }
    }
}

@Composable
fun ChatListItem(chatItemModel: ChatItemModel, viewModel: ChatListViewModel = hiltViewModel(),NavigateToChatScreen: (String) -> Unit = {}) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(0.95f)
            .clickable { NavigateToChatScreen(chatItemModel.chatID!!) },

    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (viewModel.chatItemPictures.containsKey(chatItemModel.messagingUserID)) {
                val imageByteArray = viewModel.chatItemPictures[chatItemModel.messagingUserID]!!
                val imageBitmap =
                    BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.size)
                        .asImageBitmap()
                Image(
                    bitmap = imageBitmap,
                    contentDescription = "profile picture",
                    modifier = Modifier
                        .size(49.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray, CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    imageVector = Icons.Filled.Face,
                    contentDescription = "profile picture",
                    modifier = Modifier
                        .size(49.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray, CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.padding(5.dp))
            Column {
                Text(
                    text = chatItemModel.messagingUsername!!,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.padding(2.dp))
                if (chatItemModel.lastMessageSender != chatItemModel.messagingUserID) {
                    Text(
                        text = "You: " + chatItemModel.lastMessage!!,
                        fontSize = 14.sp,
                        maxLines = 1
                    )
                } else {
                    Text(text = chatItemModel.lastMessage!!, fontSize = 14.sp, maxLines = 1)
                }

            }
        }
        Text(
            text = ConvertTime(chatItemModel.lastMessageDate!!),
            color = Color.Gray,
            fontSize = 12.sp
        )

    }
}

private fun ConvertTime(date: Date): String {
    val now = System.currentTimeMillis()
    val time = date.time

    val diff = now - time
    return when {
        TimeUnit.MILLISECONDS.toMinutes(diff) < 1 -> "Now"
        TimeUnit.MILLISECONDS.toMinutes(diff) < 60 -> "${TimeUnit.MILLISECONDS.toMinutes(diff)} minutes ago"
        TimeUnit.MILLISECONDS.toHours(diff) < 24 -> "${TimeUnit.MILLISECONDS.toHours(diff)} hours ago"
        TimeUnit.MILLISECONDS.toDays(diff) < 7 -> "${TimeUnit.MILLISECONDS.toDays(diff)} days ago"
        TimeUnit.MILLISECONDS.toDays(diff) < 365 -> "${TimeUnit.MILLISECONDS.toDays(diff) / 7} weeks ago"
        else -> "${TimeUnit.MILLISECONDS.toDays(diff) / 365} years ago"

    }
}


@Preview
@Composable
fun ChatListPreview() {
    Column(
        Modifier
            .fillMaxSize()
            .padding(0.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "1")
            Text(text = "2")
        }

    }
}