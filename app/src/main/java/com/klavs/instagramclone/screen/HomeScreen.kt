package com.klavs.instagramclone.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.pullrefresh.PullRefreshDefaults
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.klavs.instagramclone.model.PostModel
import com.klavs.instagramclone.util.AppConfig
import com.klavs.instagramclone.viewmodel.HomeScreenViewModel
import com.klavs.instagramclone.viewmodel.MainPageStatements
import com.klavs.instagramclone.viewmodel.PostScreenStatements
import com.klavs.instagramclone.R
import com.klavs.instagramclone.viewmodel.PostScreenViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    homeScreenViewModel: HomeScreenViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        homeScreenViewModel.GetPosts()
    }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = homeScreenViewModel.isLoading.value,
        onRefresh = {
            homeScreenViewModel.GetPosts()
        }
    )

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    Scaffold(
        topBar = {
            TopBarMainPage(scrollBehavior) {
                navController.navigate("chat_list_screen")
            }
        }, modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .fillMaxSize()
    ) { paddingValues ->
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ) {
            PullRefreshIndicator(
                refreshing = homeScreenViewModel.isLoading.value,
                state = pullRefreshState,
                modifier = Modifier.zIndex(2f)
            )
            if (!homeScreenViewModel.isLoading.value) {
                val data = homeScreenViewModel.refreshdata
                Content(
                    post_list = data,
                    navController = navController,
                    paddingValues = paddingValues
                )
            }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBarMainPage(
    scroolBehavior: TopAppBarScrollBehavior,
    ChatButtonOnClick: () -> Unit = {}
) {
    TopAppBar(
        title = {
            Spacer(modifier = Modifier.padding(5.dp))
            Text(
                stringResource(id = R.string.app_name),
                fontFamily = FontFamily.Default
            )
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            scrolledContainerColor = TopAppBarDefaults.centerAlignedTopAppBarColors().containerColor
        ),
        scrollBehavior = scroolBehavior,
        actions = {
            IconButton(onClick = ChatButtonOnClick) {
                Image(
                    painter = painterResource(id = R.drawable.message_icon),
                    contentScale = ContentScale.Crop,
                    contentDescription = "messages",
                    modifier = Modifier.size(39.dp)
                )
            }
        }
    )
}

@Composable
private fun Content(
    post_list: SnapshotStateList<PostModel>,
    viewModel: PostScreenViewModel = hiltViewModel(),
    navController: NavHostController,
    paddingValues: PaddingValues,
    HomeScreenViewModel: HomeScreenViewModel = hiltViewModel()
) {
    val postInSheet = remember {
        mutableStateOf<PostModel?>(null)
    }
    var showSheet by remember { mutableStateOf(false) }
    if (post_list.isEmpty()) {
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "No posts to show yet", fontSize = 36.sp)
        }

    }


    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        if (showSheet) {
            BottomSheet(postInSheet.value!!, navController = navController) {
                showSheet = false
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            post_list.forEach { post ->
                LaunchedEffect(Unit) {
                    viewModel.GetPost(post.post_id)
                }
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (viewModel.postStatementMap.toMap().containsKey(post.post_id)) {
                        when (val state =
                            viewModel.postStatementMap.toMap().get(post.post_id)!!.value) {
                            is PostScreenStatements.Error -> {
                                Text(text = "An error occured: " + state.errorMessage)
                            }

                            PostScreenStatements.Loading -> {

                            }

                            is PostScreenStatements.Success -> {
                                PostDesign(post = state.postData!!, navController, ShowSheet = {
                                    postInSheet.value = it
                                    showSheet = true
                                })
                            }
                        }
                    }

                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetinygPreview() {
    Text(
        stringResource(id = R.string.app_name),
        fontFamily = FontFamily.Default,
        fontSize = 36.sp
    )
}