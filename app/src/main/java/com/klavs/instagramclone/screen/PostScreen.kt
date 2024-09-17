package com.klavs.instagramclone.screen

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.firebase.Timestamp
import com.klavs.instagramclone.model.PostModel
import com.klavs.instagramclone.viewmodel.ImageStatements
import com.klavs.instagramclone.viewmodel.PostScreenStatements
import com.klavs.instagramclone.viewmodel.PostScreenViewModel
import com.klavs.instagramclone.R
import com.klavs.instagramclone.model.CommentModel
import com.klavs.instagramclone.util.FirebaseObject
import java.util.concurrent.TimeUnit

@Composable
fun PostScreen(
    navController: NavHostController,
    postID: String,
    viewModel: PostScreenViewModel = hiltViewModel()
) {
    val post = remember {
        mutableStateOf<PostModel?>(null)
    }
    var showSheet by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        viewModel.GetPost(postID)
    }
    Scaffold(topBar = {
        TopBar {
            navController.popBackStack()
        }
    }) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {

            if (showSheet && post != null) {
                BottomSheet(post.value!!, navController = navController) {
                    showSheet = false
                }
            }
            Column(modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = it.calculateBottomPadding())) {
                if (viewModel.postStatementMap.toMap().containsKey(postID)) {
                    when (val state = viewModel.postStatementMap.toMap().get(postID)!!.value) {
                        is PostScreenStatements.Error -> {
                            Text(text = "An error occured: " + state.errorMessage)
                        }

                        PostScreenStatements.Loading -> {

                        }

                        is PostScreenStatements.Success -> {
                            PostDesign(post = state.postData!!, navController, ShowSheet = {
                                post.value = it
                                showSheet = true
                            })
                        }
                    }
                }

            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(navigateBack: () -> Unit) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                "Post",
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
fun PostDesign(post: PostModel, navController: NavHostController, ShowSheet: (PostModel) -> Unit) {
    Spacer(modifier = Modifier.padding(10.dp))
    PostOwner(post = post, navController)
    Spacer(modifier = Modifier.padding(10.dp))
    PostImage(post = post)
    Spacer(modifier = Modifier.padding(3.dp))
    ReactionBar(post = post, ShowSheet = ShowSheet)
    Spacer(modifier = Modifier.padding(3.dp))
    Description(post = post)
}

@Composable
fun Description(post: PostModel) {
    if (post.description.isNotEmpty()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.padding(15.dp))
            Text(
                text = post.post_owner_object?.let { it.username } ?: ("error" + ": "),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.padding(5.dp))
            Text(text = post.description, fontSize = 18.sp)
        }
    }
}

@Composable
fun PostOwner(
    post: PostModel,
    navController: NavHostController,
    viewModel: PostScreenViewModel = hiltViewModel()
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        Spacer(modifier = Modifier.padding(8.dp))
        when (viewModel.profilePictureMap.toMap().containsKey(post.post_owner)) {
            true -> {
                val btyeArray = viewModel.profilePictureMap.toMap().get(post.post_owner)!!
                val bitmap = BitmapFactory.decodeByteArray(btyeArray, 0, btyeArray.size)
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentScale = ContentScale.Crop,
                    contentDescription = "profile picture",
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray, CircleShape)
                        .clickable(
                            indication = null, // Tıklama efektini kapatır
                            interactionSource = remember { MutableInteractionSource() },
                            role = Role.Image
                        ) {
                            if (post.post_owner != FirebaseObject.auth.currentUser?.uid) {
                                navController.navigate("user_profile_page/${post.post_owner_object?.username}")
                            }
                        }
                )
            }

            false -> {
                Image(
                    imageVector = Icons.Filled.Face,
                    contentScale = ContentScale.Crop,
                    contentDescription = "profile picture",
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray, CircleShape)
                        .clickable(
                            indication = null, // Tıklama efektini kapatır
                            interactionSource = remember { MutableInteractionSource() },
                        ) {
                            if (post.post_owner != FirebaseObject.auth.currentUser?.uid) {
                                navController.navigate("user_profile_page/${post.post_owner_object?.username}")
                            }
                        }
                )
            }
        }
        Spacer(modifier = Modifier.padding(8.dp))
        Text(text = post.post_owner_object?.let { it.username } ?: "error", fontSize = 18.sp,
            modifier = Modifier.clickable(indication = null, // Tıklama efektini kapatır
                interactionSource = remember { MutableInteractionSource() },) {
                if (post.post_owner != FirebaseObject.auth.currentUser?.uid){
                    navController.navigate("user_profile_page/${post.post_owner_object?.username}")
                }
            })
        Spacer(modifier = Modifier.padding(8.dp))
        Text(text = ConvertTime(post.date),fontSize = 12.sp, color = Color.DarkGray)
    }
}

@Composable
fun PostImage(post: PostModel, viewModel: PostScreenViewModel = hiltViewModel()) {
    if (viewModel.postImageStatementMap.toMap().containsKey(post.post_id)) {
        when (val state = viewModel.postImageStatementMap.toMap().get(post.post_id)!!.value) {
            is ImageStatements.Error -> {
                Column {
                    Text(text = "Error:" + state.errorMessage)
                }
            }

            ImageStatements.Loading -> {
                val configuration = LocalConfiguration.current
                val screenWidthDp = configuration.screenWidthDp.dp
                val screenHeightDp = configuration.screenHeightDp.dp
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = screenWidthDp * 0.9f)
                        .heightIn(screenHeightDp * 0.6f)
                ) {
                    CircularProgressIndicator()
                }
            }
            is ImageStatements.Success -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val configuration = LocalConfiguration.current
                    val screenWidthDp = configuration.screenWidthDp.dp
                    val screenHeightDp = configuration.screenHeightDp.dp
                    Image(
                        bitmap = state.image!!,
                        contentScale = ContentScale.Crop,
                        contentDescription = "post image",
                        modifier = Modifier
                            .widthIn(max = screenWidthDp * 0.9f)
                            .heightIn(screenHeightDp * 0.6f)
                    )
                }

            }
        }
    }

}


@Composable
private fun ReactionBar(
    viewModel: PostScreenViewModel = hiltViewModel(),
    post: PostModel,
    ShowSheet: (PostModel) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.padding(20.dp))
        if (viewModel.isPostLikedMap.toMap().containsKey(post.post_id)) {
            when (viewModel.isPostLikedMap.toMap().get(post.post_id)!!.value) {
                true -> Image(imageVector = Icons.Filled.Favorite,
                    contentDescription = "like",
                    modifier = Modifier
                        .size(30.dp)
                        .clickable(indication = null,
                            interactionSource = remember { MutableInteractionSource() }) {
                            viewModel.LikeThePost(post.post_id, false)
                        })

                false -> Image(imageVector = Icons.Outlined.FavoriteBorder,
                    contentDescription = "like",
                    modifier = Modifier
                        .size(30.dp)
                        .clickable(indication = null,
                            interactionSource = remember { MutableInteractionSource() }) {
                            viewModel.LikeThePost(post.post_id, true)
                        })
            }
        }

        Spacer(modifier = Modifier.padding(3.dp))
        Text(text = post.like_list.size.toString(), fontSize = 18.sp)
        Spacer(modifier = Modifier.padding(10.dp))
        Image(
            painter = painterResource(id = R.drawable.comment_icon),
            contentDescription = "",
            modifier = Modifier
                .size(30.dp)
                .clickable(indication = null,
                    interactionSource = remember { MutableInteractionSource() }) {
                    ShowSheet(post)
                }
        )
        Spacer(modifier = Modifier.padding(3.dp))
        val comment_size = viewModel.commentListMap.toMap().get(post.post_id)?.size ?: 0
        Text(text = comment_size.toString(), fontSize = 18.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheet(
    post: PostModel,
    viewModel: PostScreenViewModel = hiltViewModel(),
    navController: NavHostController,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val configuration = LocalConfiguration.current
    val screenHeightDp = configuration.screenHeightDp.dp
    ModalBottomSheet(
        onDismissRequest = { onDismiss() },
        sheetState = sheetState,
        modifier = Modifier
            .zIndex(2f)
            .height(screenHeightDp * 0.9f)
    ) {
        val value = remember { mutableStateOf("") }
        Box(
            modifier = Modifier.fillMaxHeight()
        ) {

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 42.dp), horizontalAlignment = Alignment.Start
            ) {
                if (viewModel.commentListMap.toMap().containsKey(post.post_id)) {
                    items(viewModel.commentListMap.toMap().get(post.post_id)!!) {
                        CommentRow(it, navController = navController)
                        Spacer(modifier = Modifier.padding(10.dp))
                    }
                }

            }
            Spacer(modifier = Modifier.padding(5.dp))
            TextField(value = value.value, onValueChange = {
                value.value = it
            },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(start = 5.dp)
                    .zIndex(2f),
                placeholder = { Text(text = "Type a comment") },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Send,
                        contentDescription = "send",
                        modifier = Modifier.clickable {
                            viewModel.Comment(post.post_id, value.value)
                            value.value = ""
                        })
                })
        }

    }
}

@Composable
fun CommentRow(commentModel: CommentModel,navController: NavHostController, viewModel: PostScreenViewModel = hiltViewModel()) {
    viewModel.GetCommentProfilePicture(commentModel.user_id)
    Row(modifier = Modifier.fillMaxWidth(0.95f), horizontalArrangement = Arrangement.SpaceBetween) {
        Row {
            Spacer(modifier = Modifier.padding(5.dp))
            if (viewModel.comment_profile_pictures.containsKey(commentModel.user_id)) {
                val imageBitmap = BitmapFactory.decodeByteArray(
                    viewModel.comment_profile_pictures[commentModel.user_id]!!,
                    0,
                    viewModel.comment_profile_pictures[commentModel.user_id]!!.size
                ).asImageBitmap()
                Image(
                    bitmap = imageBitmap, contentDescription = "profile_picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray, CircleShape)
                        .clickable {
                            navController.navigate("user_profile_page/${commentModel.username}")
                        }
                )
            } else {
                Image(
                    imageVector = Icons.Filled.Face, contentDescription = "profile_picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(50.dp)
                        .background(Color.LightGray, CircleShape)
                        .clickable {
                            navController.navigate("user_profile_page/${commentModel.username}")
                        }
                )
            }

            Spacer(modifier = Modifier.padding(5.dp))
            Column {
                Row {
                    Text(
                        text = commentModel.username.toString(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.clickable {
                            navController.navigate("user_profile_page/${commentModel.username}")
                        }
                    )
                    Spacer(modifier = Modifier.padding(5.dp))
                    Text(
                        text = ConvertTime(commentModel.date),
                        fontSize = 11.sp,
                        color = Color.DarkGray,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
                Spacer(modifier = Modifier.padding(2.dp))
                Text(text = commentModel.comment, fontSize = 18.sp)
            }
        }
        val favouriteIcon = remember {
            mutableStateOf(Icons.Filled.FavoriteBorder)
        }
        val actionIsChanged = remember { mutableStateOf(false) }

        val like_list = commentModel.like_list
        if (!actionIsChanged.value) {
            if (like_list.contains(FirebaseObject.auth.currentUser!!.uid)) {
                favouriteIcon.value = Icons.Filled.Favorite
            } else {
                favouriteIcon.value = Icons.Filled.FavoriteBorder
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(imageVector = favouriteIcon.value, contentDescription = "favorite",
                modifier = Modifier
                    .size(30.dp)
                    .align(Alignment.CenterVertically)
                    .clickable {
                        actionIsChanged.value = true
                        if (favouriteIcon.value == Icons.Filled.Favorite) {
                            viewModel.LikeTheComment(
                                commentModel.comment_id,
                                commentModel.post_id,
                                false
                            )
                            favouriteIcon.value = Icons.Filled.FavoriteBorder
                        } else {
                            viewModel.LikeTheComment(
                                commentModel.comment_id,
                                commentModel.post_id,
                                true
                            )
                            favouriteIcon.value = Icons.Filled.Favorite
                        }

                    })
            Spacer(modifier = Modifier.padding(3.dp))
            Text(text = like_list.size.toString(), fontSize = 18.sp)

        }


    }
}


private fun ConvertTime(timestamp: Timestamp): String {
    val now = System.currentTimeMillis()
    val time = timestamp.toDate().time

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

@Preview(showBackground = true)
@Composable
fun PostScreenPreview() {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {

    }

}

