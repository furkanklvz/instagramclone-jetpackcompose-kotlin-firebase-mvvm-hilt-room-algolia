package com.klavs.instagramclone.screen

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.OutlinedButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.klavs.instagramclone.util.FirebaseObject

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SignInScreen(navController: NavHostController) {
    Scaffold(modifier = Modifier.fillMaxSize()) {
        SignInForm(navController)
    }
}

@Composable
fun SignInForm(navController: NavHostController) {
    var email by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }
    Column(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFB3E5FC), Color(0xAFE1F5FE))
                )
            )
    ) {

    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.6f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(
            text = "Sign In",
            fontSize = 50.sp
        )
        SignInTextField(
            label = "E-Mail",
            value = email,
            Icons.Filled.Email
        ) {
            email = it
        }
        SignInTextField(
            label = "Password",
            value = password,
            Icons.Filled.Lock
        ) {
            password = it
        }
        SignInButton(email.trim(), password.trim(), navController)
    }
}

@Composable
fun SignInButton(email: String, password: String, navController: NavHostController) {
    val auth = FirebaseObject.auth
    OutlinedButton(
        shape = CircleShape,
        onClick = {
            if (!email.isEmpty() && !password.isEmpty()) {
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful) {
                        navController.navigate("app_main")

                    } else {
                        if (it.exception != null) {
                            Toast.makeText(
                                navController.context,
                                it.exception!!.localizedMessage,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } else {
                Toast.makeText(navController.context, "Please fill all fields", Toast.LENGTH_SHORT)
                    .show()
            }
        }) {
        Text(
            text = "Sign In",
            fontSize = 30.sp
        )
    }
}

@Composable
fun SignInTextField(
    label: String,
    value: String,
    icon: ImageVector,
    onValueChange: (String) -> Unit
) {
    var visualTransformation = VisualTransformation.None
    if (label == "Password") {
        visualTransformation = PasswordVisualTransformation()
    }
    TextField(
        value = value,
        onValueChange = onValueChange,
        visualTransformation = visualTransformation,
        singleLine = true,
        label = { Text(text = label) },
        leadingIcon = { Icon(imageVector = icon, contentDescription = label) },
        modifier = Modifier.fillMaxWidth(0.9f)
    )
}

@Preview(showBackground = true)
@Composable
fun SignInScreenPreview() {
    SignInScreen(rememberNavController())
}




