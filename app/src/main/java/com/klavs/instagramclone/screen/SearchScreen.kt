package com.klavs.instagramclone.screen

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.klavs.instagramclone.room.RecentSearchesModel
import com.klavs.instagramclone.util.FirebaseObject
import com.klavs.instagramclone.viewmodel.RecentSearchesStatements
import com.klavs.instagramclone.viewmodel.SearchScreenViewModel
import com.klavs.instagramclone.viewmodel.SearchStatements
import java.io.ByteArrayOutputStream

@Composable
fun SearchScreen(
    navController: NavHostController,
    viewModel: SearchScreenViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.GetAllRecentSearches()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        Spacer(modifier = Modifier.padding(20.dp))
        SearchBar {
            viewModel.SearchUsers(it)
        }
        Spacer(modifier = Modifier.padding(20.dp))
        when (val state = viewModel.searchState.value) {
            SearchStatements.Loading -> {
                CircularProgressIndicator()
            }

            is SearchStatements.Success -> {
                ListUsers(searchResults = state.data, navController = navController)
            }

            SearchStatements.Empty -> {
                Text(text = "No users found", fontSize = 30.sp)
            }

            is SearchStatements.Error -> {
                Text(text = "Error", fontSize = 30.sp)
            }

            SearchStatements.Idle -> {
                when (val recentSearchesState = viewModel.recentSearchesState.value) {
                    RecentSearchesStatements.Empty -> Text(text = "Search users", fontSize = 30.sp)
                    is RecentSearchesStatements.Error -> Text(text = "Error: "+recentSearchesState.message, fontSize = 30.sp)
                    RecentSearchesStatements.Loading -> CircularProgressIndicator()
                    is RecentSearchesStatements.Success -> {
                        ListUsers(
                            recentSearchResults = recentSearchesState.data,
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ListUsers(
    recentSearchResults: List<RecentSearchesModel>? = null,
    searchResults: MutableList<Map<String, Any>>? = null,
    navController: NavHostController,
    viewModel: SearchScreenViewModel = hiltViewModel()
) {
    if (!searchResults.isNullOrEmpty()) {
        LazyColumn {
            items(searchResults) { searchResult ->
                if (searchResult.get("user_id") == FirebaseObject.auth.currentUser!!.uid){
                    return@items
                }
                val bitmap: Bitmap = (searchResult.get("image") as ImageBitmap).asAndroidBitmap()

                val byteArrayOutputStream = ByteArrayOutputStream()

                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                val imageBtyeArray = byteArrayOutputStream.toByteArray()
                UserItemRow(
                    profilePicture = searchResult.get("image") as ImageBitmap,
                    username = searchResult.get("username") as String
                ) {
                    viewModel.InsertRecentSearch(
                        searchResult.get("user_id") as String,
                        searchResult.get("username") as String,
                        imageBtyeArray
                    )
                    println("veriler gonderildi: " + searchResult.get("username") as String)
                    navController.navigate("user_profile_page/${it}")
                }
                Spacer(modifier = Modifier.padding(10.dp))
            }
        }
    } else if (!recentSearchResults.isNullOrEmpty()) {
        Text(text = "Recent Searches", fontSize = 20.sp, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.padding(10.dp))
        LazyColumn {
            items(recentSearchResults) { recentSearchModel ->
                if (recentSearchModel.user_id==FirebaseObject.auth.currentUser!!.uid){
                    return@items
                }
                val pictureBitmap = BitmapFactory.decodeByteArray(
                    recentSearchModel.image,
                    0,
                    recentSearchModel.image.size
                )
                UserItemRow(
                    profilePicture = pictureBitmap.asImageBitmap(),
                    username = recentSearchModel.username
                ) {
                    viewModel.InsertRecentSearch(
                        recentSearchModel.user_id,
                        recentSearchModel.username,
                        recentSearchModel.image
                    )
                    println("veriler gonderildi: " + recentSearchModel.user_id)
                    navController.navigate("user_profile_page/${it}")
                }
                Spacer(modifier = Modifier.padding(10.dp))
            }
        }
    }

}

@Composable
fun UserItemRow(profilePicture: ImageBitmap, username: String, onClickListener: (String) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(5.dp)
            .clickable { onClickListener(username) }) {
        Image(
            bitmap = profilePicture,
            contentScale = ContentScale.Crop,
            contentDescription = "profile picture",
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(Color.LightGray, CircleShape)
        )
        Spacer(modifier = Modifier.padding(10.dp))
        Text(
            text = username,
            fontSize = 20.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(onSearch: (String) -> Unit = {}) {
    var text by remember {
        mutableStateOf("")
    }
    TextField(value = text,
        shape = CircleShape,
        modifier = Modifier.fillMaxWidth(0.8f),
        maxLines = 1,
        placeholder = { Text(text = "Search..")},
        colors = TextFieldDefaults.textFieldColors(
            unfocusedIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        trailingIcon = { Icon(imageVector = Icons.Filled.Search, contentDescription = "search") },
        onValueChange = {
            text = it
            onSearch(it)
        })
}

@Preview(showBackground = true)
@Composable
fun SearchScreenPreview() {

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        UserItemRow(profilePicture = ImageBitmap(1,1), username = "Furkan"){}
    }


}