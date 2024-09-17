package com.klavs.instagramclone.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.klavs.instagramclone.R
import com.klavs.instagramclone.util.FirebaseObject
import com.klavs.instagramclone.ui.theme.InstagramCloneTheme
import com.klavs.instagramclone.ui.theme.Pink80
import com.klavs.instagramclone.ui.theme.PurpleGrey80




@Composable
fun Greeting(navController: NavHostController) {
    val currentUser = FirebaseObject.auth.currentUser
    if (currentUser != null){
        navController.navigate("app_main")
        return
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
    ) {


        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
                .background(Pink80),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GreetingContent(navController)
        }

}
}

@Composable
fun GreetingContent(navController: NavHostController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFB3E5FC), Color(0xAFE1F5FE))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.padding(20.dp))
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "logo",
                modifier = Modifier
                    .size(120.dp)
            )
            Text(
                text = stringResource(id = R.string.app_name),
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                color = Color(0xFF00796B),
                modifier = Modifier
                    .padding(3.dp)
            )
            Spacer(modifier = Modifier.height(132.dp))
            OutlinedButton(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(56.dp)
                    .shadow(5.dp, CircleShape)
                    .background(Color(0xFF4CAF50), CircleShape),
                onClick = { navController.navigate("sign_in_screen") }
            ) {
                Text(
                    text = "Sign In",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "or",
                fontSize = 20.sp,
                fontFamily = FontFamily.SansSerif,
                color = Color.Gray,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            OutlinedButton(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(56.dp)
                    .shadow(5.dp, CircleShape)
                    .background(Color(0xFF03A9F4), CircleShape),
                onClick = { navController.navigate("register_screen") }
            ) {
                Text(
                    text = "Register",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    color = Color.White
                )

            }
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.fillMaxSize()) {
                Text(text = "by Furkan Kılavuz", fontSize = 16.sp, color = Color.DarkGray, fontFamily = FontFamily.SansSerif)
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFB3E5FC), Color(0xAFE1F5FE))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.padding(20.dp))
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "logo",
                modifier = Modifier
                    .size(120.dp)
            )
            Text(
                text = stringResource(id = R.string.app_name),
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                color = Color(0xFF00796B),
                modifier = Modifier
                    .padding(3.dp)
            )
            Spacer(modifier = Modifier.height(132.dp))
            OutlinedButton(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(56.dp)
                    .shadow(5.dp, CircleShape)
                    .background(Color(0xFF4CAF50), CircleShape),
                onClick = { /* Sign In action */ }
            ) {
                Text(
                    text = "Sign In",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "or",
                fontSize = 20.sp,
                fontFamily = FontFamily.SansSerif,
                color = Color.Gray,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            OutlinedButton(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(56.dp)
                    .shadow(5.dp, CircleShape)
                    .background(Color(0xFF03A9F4), CircleShape),
                onClick = { /* Register action */ }
            ) {
                Text(
                    text = "Register",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    color = Color.White
                )

            }
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.fillMaxWidth().fillMaxHeight(0.95f)) {
                Text(text = "by Furkan Kılavuz", fontSize = 16.sp, color = Color.DarkGray, fontFamily = FontFamily.SansSerif)
            }

        }
    }
}
