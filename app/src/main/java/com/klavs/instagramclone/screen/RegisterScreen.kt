package com.klavs.instagramclone.screen

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.google.firebase.auth.userProfileChangeRequest
import com.klavs.instagramclone.util.FirebaseObject


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RegisterScreen(navController: NavHostController) {
    Scaffold {
        RegisterForm(navController)
    }
}

@Composable
fun RegisterForm(navController: NavHostController?) {
    val goToStageTwo = remember {
        mutableStateOf(false)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFB3E5FC), Color(0xAFE1F5FE))
                )
            )
    ) {

        var name by remember {
            mutableStateOf("")
        }
        var surname by remember {
            mutableStateOf("")
        }
        var e_mail by remember {
            mutableStateOf("")
        }
        var username by remember {
            mutableStateOf("")
        }
        var phone_number by remember {
            mutableStateOf("")
        }
        var password by remember {
            mutableStateOf("")
        }
        var password_again by remember {
            mutableStateOf("")
        }
        val registerInputMap: Map<String, Any> = mapOf(
            "name" to name,
            "surname" to surname,
            "username" to username,
            "e_mail" to e_mail,
            "phone_number" to phone_number,
            "password" to password,
            "password_again" to password_again
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
                .fillMaxHeight(0.6f)) {
            Text(
                text = "REGISTER",
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp
            )
            Row(
                modifier = Modifier.fillMaxWidth(0.9f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (goToStageTwo.value){
                    OutlinedButton(onClick = { goToStageTwo.value = false }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "turnBack")
                    }
                }
                RegisterButton(navController, registerInputMap,goToStageTwo.value){
                    goToStageTwo.value = true
                }
            }
            if (!goToStageTwo.value){
                RegisterTextField(
                    label = "Name",
                    value = name,
                    Icons.Filled.Person,
                    KeyboardType.Text
                ) {
                    name = it
                }
                RegisterTextField(
                    label = "Surname",
                    value = surname, Icons.Filled.Person,
                    KeyboardType.Text
                ) {
                    surname = it
                }
                RegisterTextField(
                    label = "Username",
                    value = username, Icons.Filled.Person,
                    KeyboardType.Text
                ) {
                    username = it
                }
                RegisterTextField(
                    label = "E-mail",
                    value = e_mail, Icons.Filled.Email,
                    KeyboardType.Email
                ) {
                    e_mail = it
                }
                RegisterTextField(
                    label = "Phone Number",
                    value = phone_number, Icons.Filled.Phone,
                    KeyboardType.Phone
                ) {
                    phone_number = it
                }
            }else{
                RegisterTextField(
                    label = "Password",
                    value = password, Icons.Filled.Lock,
                    KeyboardType.Password
                ) {
                    password = it
                }
                RegisterTextField(
                    label = "Password (Again)",
                    value = password_again, Icons.Filled.Lock,
                    KeyboardType.Password
                ) {
                    password_again = it
                }
            }
        }





    }
}

@Composable
fun RegisterTextField(
    label: String,
    value: String,
    icon: ImageVector,
    keyboardType: KeyboardType,
    onValueChange: (String) -> Unit
) {
    var visualTransformation: VisualTransformation = VisualTransformation.None
    if (keyboardType == KeyboardType.Password) {
        visualTransformation = PasswordVisualTransformation()
    }
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label) },
        singleLine = true,
        leadingIcon = { Icon(imageVector = icon, contentDescription = null) },
        modifier = Modifier.fillMaxWidth(0.9f),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = visualTransformation
    )
}

@Composable
fun RegisterButton(navController: NavHostController?, inputs: Map<String, Any>,stageTwo:Boolean,goToStageTwo: () -> Unit = {}) {
    var isLoading by remember {
        mutableStateOf(false)
    }
    OutlinedButton( onClick = {
        if (!stageTwo){
            goToStageTwo()
        }else{
            RegisterSubmit(navController!!,
                inputs,
                startLoading = { isLoading = true },
                stopLoading = { isLoading = false })
        }

    }) {
        when (isLoading) {
            false -> {
                Text(
                    text = "CONTINUE",
                    fontSize = 30.sp
                )
                Spacer(modifier = Modifier.padding(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "",
                    modifier = Modifier.size(36.dp)
                )
            }

            true -> {
                CircularProgressIndicator()
            }
        }

    }
}

fun RegisterSubmit(
    navController: NavHostController,
    inputs: Map<String, Any>,
    startLoading: () -> Unit,
    stopLoading: () -> Unit
) {
    startLoading()
    val auth = FirebaseObject.auth
    val db = FirebaseObject.db
    if (!inputs.containsValue("")) {
        if (inputs["password"] == inputs["password_again"]) {
            auth.createUserWithEmailAndPassword(
                inputs["e_mail"].toString().trim(),
                inputs["password"].toString().trim()
            )
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        val profileUpdate = userProfileChangeRequest {
                            displayName = inputs["username"].toString()
                        }
                        it.result.user!!.updateProfile(profileUpdate)
                        val uid = it.result.user!!.uid
                        val userDatas = hashMapOf(
                            "uid" to uid.trim(),
                            "name" to inputs["name"].toString().trim(),
                            "surname" to inputs["surname"].toString().trim(),
                            "username" to inputs["username"].toString().trim(),
                            "e_mail" to inputs["e_mail"].toString().trim(),
                            "password" to inputs["password"].toString().trim(),
                        )
                        db.collection("users").document(uid).set(userDatas).addOnSuccessListener {
                            println("Success: " + uid)
                            stopLoading()
                            navController.navigate("edit_profile_screen") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }


                    } else {
                        if (it.exception != null) {
                            stopLoading()
                            Toast.makeText(
                                navController.context,
                                it.exception!!.localizedMessage,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

            println(inputs)
        } else {
            stopLoading()
            Toast.makeText(navController.context, "Passwords do not match", Toast.LENGTH_SHORT)
                .show()
        }

    } else {
        stopLoading()
        Toast.makeText(navController.context, "Please fill all fields", Toast.LENGTH_SHORT).show()
    }

}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RegisterScreenPreview() {
}