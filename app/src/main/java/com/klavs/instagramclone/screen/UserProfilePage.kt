package com.klavs.instagramclone.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.klavs.instagramclone.model.UserModel
import com.klavs.instagramclone.util.FirebaseObject
import com.klavs.instagramclone.viewmodel.PhotosStatements
import com.klavs.instagramclone.viewmodel.ProfilePageStatements
import com.klavs.instagramclone.viewmodel.ProfilePictureStatements
import com.klavs.instagramclone.viewmodel.UserProfilePageViewModel

@Composable
fun UserProfilePage(
    navController: NavHostController,
    username: String?,
    firstNavController: NavHostController,
    viewModel: UserProfilePageViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        if (username != null) {
            viewModel.GetUserProfile(username)
        }
    }
    Scaffold(topBar = {
        TopBar(
            username,
            firstNavController,
            navController
        ) { navController.popBackStack() }
    }) {

        when (viewModel.statement.value) {
            ProfilePageStatements.Loading -> {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    CircularProgressIndicator()
                }
            }

            ProfilePageStatements.Error -> {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(text = "Error")
                }
            }

            ProfilePageStatements.Success -> {
                Content(
                    navController,
                    it,
                    viewModel.user,
                    firstNavController
                )
            }
        }

    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    username: String? = "",
    firstNavController: NavHostController? = null,
    navController: NavHostController,
    navigateBack: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                username ?: ""
            )
        },
        actions = {
            if (username == FirebaseObject.auth.currentUser?.displayName) {
                OverflowMenu(firstNavController, navController)
            }
        },
        navigationIcon = {
            if (username != FirebaseObject.auth.currentUser?.displayName) {
                IconButton(onClick = navigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Localized description"
                    )
                }
            }

        }
    )
}

@Composable
private fun OverflowMenu(
    firstNavController: NavHostController? = null,
    navController: NavHostController
) {
    var expanded = remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.wrapContentSize(Alignment.TopEnd)
    ) {
        IconButton(onClick = { expanded.value = true }) {
            Icon(
                imageVector = Icons.Default.MoreVert, // Üç nokta ikonu
                contentDescription = "More options"
            )
        }
        DropdownMenu(
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false }
        ) {
            if (FirebaseObject.auth.currentUser != null) {

            DropdownMenuItem(text = { Text(text = "Settings (inactive yet)") }, onClick = { })
                DropdownMenuItem(
                    text = { Text(text = "Edit Profile") },
                    onClick = { navController.navigate("edit_profile_2/${FirebaseObject.auth.currentUser!!.uid}") })
            }
            DropdownMenuItem(text = { Text(text = "Sign Out") }, onClick = {
                if (firstNavController != null) {
                    println("firstnavcontroller boş değil")
                    FirebaseObject.auth.signOut().also {
                        println("çıkış yapıldı")
                        firstNavController.navigate("main") {
                            popUpTo(0) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }

                }

            })
        }
    }
}


@Composable
private fun Content(
    navController: NavHostController,
    paddingValues: PaddingValues,
    user: UserModel,
    firstNavController: NavHostController,
    viewModel: UserProfilePageViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.GetProfilePicture(user.uid)
    }
    Column(modifier = Modifier.padding(top = paddingValues.calculateTopPadding())) {
        Spacer(modifier = Modifier.padding(5.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                when (viewModel.pictureStatement.value) {
                    ProfilePictureStatements.Error -> {
                        Image(
                            imageVector = Icons.Filled.Warning,
                            contentScale = ContentScale.Crop,
                            contentDescription = "profile picture",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray, CircleShape)
                        )
                    }

                    ProfilePictureStatements.Loading -> {
                        Image(
                            imageVector = Icons.Filled.Person,
                            contentScale = ContentScale.Crop,
                            contentDescription = "profile picture",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray, CircleShape)
                        )
                    }

                    ProfilePictureStatements.Success -> {
                        Image(
                            bitmap = viewModel.profilePicture!!,
                            contentScale = ContentScale.Crop,
                            contentDescription = "profile picture",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray, CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.padding(3.dp))
                Text(text = user.name+" "+user.surname.get(0)+".", fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.padding(10.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Posts", fontSize = 16.sp)
                Spacer(modifier = Modifier.padding(3.dp))
                Text(text = user.posts?.size.toString(), fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.padding(10.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Followers", fontSize = 16.sp)
                Spacer(modifier = Modifier.padding(3.dp))
                Text(text = user.follower_list?.size.toString(), fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.padding(10.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Following", fontSize = 16.sp)
                Spacer(modifier = Modifier.padding(3.dp))
                Text(text = user.following_list?.size.toString(), fontSize = 16.sp)
            }
        }
        Spacer(modifier = Modifier.padding(20.dp))
        Text(
            text = user.biography,
            fontSize = 16.sp,
            modifier = Modifier.padding(start = 20.dp),
            maxLines = 3
        )
        Spacer(modifier = Modifier.padding(10.dp))
        if (user.uid != FirebaseObject.auth.currentUser?.uid) {
            val clicked = remember {
                mutableStateOf(false)
            }
            val isFollowed = remember {
                mutableStateOf(false)
            }
            if (!clicked.value) {
                isFollowed.value =
                    user.follower_list?.contains(FirebaseObject.auth.currentUser?.uid) == true
            }
            Row {
                Spacer(modifier = Modifier.padding(10.dp))
                FollowButton(isFollowed.value) { FollowStarted ->
                    clicked.value = true
                    if (FollowStarted) {
                        viewModel.Follow(user.uid)
                        isFollowed.value = true
                    } else {
                        viewModel.Unfollow(user.uid)
                        isFollowed.value = false
                    }
                }
                Spacer(modifier = Modifier.padding(10.dp))
                val chatID = listOf(user.uid, FirebaseObject.auth.currentUser?.uid).sortedBy { it }
                    .joinToString("_")
                MessageButton {
                    firstNavController.navigate("chat_screen/$chatID")
                }
            }

            Spacer(modifier = Modifier.padding(10.dp))
        }

        when (val state = viewModel.photosStatement.value) {
            PhotosStatements.Empty -> {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(text = "No posts yet", fontSize = 26.sp)
                }
            }

            is PhotosStatements.Error -> {
                Text(text = state.message!!, fontSize = 36.sp)
            }

            PhotosStatements.Loading -> {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    CircularProgressIndicator()
                }
            }

            is PhotosStatements.Success -> {
                Text(text = "Posts:", fontSize = 16.sp, modifier = Modifier.padding(start = 20.dp))
                val photoList = viewModel.photos.toMap().entries.toList()
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.padding(start = 10.dp),
                    contentPadding = PaddingValues(10.dp)
                ) {
                    items(photoList) {
                        Image(
                            bitmap = it.value,
                            contentDescription = "post",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(100.dp)
                                .padding(3.dp)
                                .clickable {
                                    navController.navigate("post_screen/${it.key}")
                                }
                        )
                    }
                }
            }

        }

    }
}

@Composable
fun FollowButton(
    isFollowed: Boolean,
    onClick: (FollowStarted: Boolean) -> Unit = {}
) {
    if (isFollowed) {
        OutlinedButton(
            onClick = { onClick(false) },
            shape = MaterialTheme.shapes.small,
            modifier = Modifier
                .background(Color.Gray, MaterialTheme.shapes.small)
                .size(width = 130.dp, height = 35.dp)
        ) {
            Text(text = "Followed", fontSize = 16.sp, color = Color.Green)
        }
    } else {
        OutlinedButton(
            onClick = { onClick(true) },
            shape = MaterialTheme.shapes.small,
            modifier = Modifier
                .background(Color.Gray, MaterialTheme.shapes.small)
                .size(width = 130.dp, height = 35.dp)
        ) {
            Text(text = "Follow", fontSize = 16.sp, color = Color.White)
        }
    }

}

@Composable
fun MessageButton(
    onClick: () -> Unit = {}
) {
    OutlinedButton(
        onClick = onClick,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier
            .background(Color.Gray, MaterialTheme.shapes.small)
            .size(width = 190.dp, height = 35.dp)
    ) {
        Text(text = "Send Message", fontSize = 16.sp, color = Color.White)
    }

}


@Preview(showBackground = true)
@Composable
private fun GreetingPreviewss() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {

    }

}