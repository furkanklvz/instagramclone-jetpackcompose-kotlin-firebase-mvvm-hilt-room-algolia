package com.klavs.instagramclone.main

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.klavs.instagramclone.model.ScreenModel
import com.klavs.instagramclone.screen.ChatList
import com.klavs.instagramclone.screen.EditProfile2
import com.klavs.instagramclone.screen.EditProfileScreen
import com.klavs.instagramclone.screen.HomeScreen
import com.klavs.instagramclone.screen.NewPostScreen
import com.klavs.instagramclone.screen.PostScreen
import com.klavs.instagramclone.screen.SearchScreen
import com.klavs.instagramclone.screen.UserProfilePage
import com.klavs.instagramclone.util.FirebaseObject


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AppMain(
    firstNavController: NavHostController
) {
    val bottomNavigationItems = listOf(
        ScreenModel.Home,
        ScreenModel.Search,
        ScreenModel.NewPost,
        ScreenModel.Profile
    )
    val navControllerMain = rememberNavController()

    Scaffold(bottomBar = {
        NavigationBar {
            val navBackStackEntry by navControllerMain.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            bottomNavigationItems.forEach { screen ->
                BottomNavigationItem(
                    label = { Text(text = screen.label) },
                    selected = currentRoute == screen.route,
                    onClick = {
                        navControllerMain.navigate(screen.route) {
                            popUpTo(navControllerMain.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }

                    },
                    icon = {
                        if (currentRoute == screen.route) {
                            Icon(
                                imageVector = screen.selectedIcon,
                                contentDescription = screen.label
                            )
                        } else {
                            Icon(
                                imageVector = screen.unselectedIcon,
                                contentDescription = screen.label
                            )
                        }
                    })
            }

        }
    }) { paddingValues ->
            NavHost(
                navController = navControllerMain,
                startDestination = ScreenModel.Home.route,
                modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding())
            ) {
                composable(ScreenModel.Home.route) { HomeScreen(navControllerMain) }
                composable(ScreenModel.Search.route) { SearchScreen(navControllerMain) }
                composable(ScreenModel.NewPost.route) { NewPostScreen(navControllerMain) }
                composable(ScreenModel.Profile.route) {
                    UserProfilePage(
                        navController = navControllerMain,
                        firstNavController = firstNavController,
                        username = FirebaseObject.auth.currentUser?.displayName
                    )
                }
                composable("main") { Main(firstNavController) }
                composable("edit_profile_screen") { EditProfileScreen(firstNavController) }

                composable(
                    "user_profile_page/{username}",
                    arguments = listOf(navArgument("username") { type = NavType.StringType })
                ) {
                    UserProfilePage(
                        navControllerMain,
                        it.arguments?.getString("username")!!,
                        firstNavController
                    )
                }


                composable(
                    "post_screen/{post_id}",
                    arguments = listOf(navArgument("post_id") { type = NavType.StringType })
                ) {
                    PostScreen(
                        navController = navControllerMain,
                        postID = it.arguments?.getString("post_id")!!
                    )
                }
                composable("chat_list_screen") {
                    ChatList(
                        navController = navControllerMain,
                        firstNavController = firstNavController
                    )
                }
                composable("edit_profile_2/{uid}", arguments =listOf(
                    navArgument("uid"){type= NavType.StringType}
                )){
                    EditProfile2(
                        uid = it.arguments?.getString("uid")!!,
                        navController = navControllerMain
                    )
                }
            }



    }
}