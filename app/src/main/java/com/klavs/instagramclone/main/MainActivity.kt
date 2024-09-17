package com.klavs.instagramclone.main

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.klavs.instagramclone.screen.ChatScreen
import com.klavs.instagramclone.screen.EditProfile2
import com.klavs.instagramclone.screen.EditProfileScreen
import com.klavs.instagramclone.screen.Greeting
import com.klavs.instagramclone.screen.PostScreen
import com.klavs.instagramclone.screen.RegisterScreen
import com.klavs.instagramclone.screen.SignInScreen
import com.klavs.instagramclone.screen.UserProfilePage
import com.klavs.instagramclone.ui.theme.InstagramCloneTheme
import com.klavs.instagramclone.util.AppConfig
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val window = (LocalContext.current as ComponentActivity).window
            val insetsController = WindowInsetsControllerCompat(window, window.decorView)

            SideEffect {
                window.statusBarColor = Color.Transparent.toArgb()
                insetsController.isAppearanceLightStatusBars = true
            }

            val firstNavController = rememberNavController()
            Scaffold {paddingValues->
                NavHost(
                    navController = firstNavController,
                    startDestination = "main"
                ) {
                    composable("main") {
                        Main(firstNavController)
                    }
                    composable("app_main") {
                        AppMain(firstNavController)
                    }
                    composable("chat_screen/{chatID}",
                        arguments = listOf(navArgument("chatID") {
                            type = NavType.StringType
                        })
                    ) {
                        ChatScreen(
                            chatID = it.arguments?.getString("chatID")!!,
                            firstNavController = firstNavController,
                            paddingValues= paddingValues
                        )
                    }
                    composable(
                        "user_profile_page/{username}",
                        arguments = listOf(navArgument("username") { type = NavType.StringType })
                    ) {
                        UserProfilePage(
                            navController = firstNavController,
                            username = it.arguments?.getString("username")!!,
                            firstNavController = firstNavController
                        )
                    }
                    composable(
                        "post_screen/{post_id}",
                        arguments = listOf(navArgument("post_id") { type = NavType.StringType })
                    ) {
                        PostScreen(
                            navController = firstNavController,
                            postID = it.arguments?.getString("post_id")!!
                        )
                    }
                    composable("edit_profile_2/{uid}", arguments = listOf(
                        navArgument("uid") { type = NavType.StringType }
                    )) {
                        EditProfile2(
                            uid = it.arguments?.getString("uid")!!,
                            navController = firstNavController
                        )
                    }
                }
            }

        }
    }
}


@Composable
fun Main(firstNavController: NavHostController) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "greeting_screen"
    ) {
        composable("app_main") { AppMain(firstNavController) }
        composable("greeting_screen") { Greeting(navController) }
        composable("sign_in_screen") { SignInScreen(firstNavController) }
        composable("register_screen") { RegisterScreen(navController) }
        composable("edit_profile_screen") { EditProfileScreen(navController) }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    InstagramCloneTheme {
        //Greeting()
    }
}