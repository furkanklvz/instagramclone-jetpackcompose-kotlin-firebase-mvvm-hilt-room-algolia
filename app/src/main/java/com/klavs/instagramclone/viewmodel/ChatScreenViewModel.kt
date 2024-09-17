package com.klavs.instagramclone.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.klavs.instagramclone.model.MessageModel
import com.klavs.instagramclone.model.UserModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class MessagingUserStatements(
    val userData: UserModel? = null,
    val errorMessage: String? = null
) {
    class Success(userData: UserModel) : MessagingUserStatements(userData = userData)
    class Error(errorMessage: String) : MessagingUserStatements(errorMessage = errorMessage)
    object Loading : MessagingUserStatements()
}

sealed class MessagingUserProfilePictureStatements(
    val profilePictureData: ByteArray? = null,
    val errorMessage: String? = null
) {
    class Success(image: ByteArray) :
        MessagingUserProfilePictureStatements(profilePictureData = image)

    class Error(errorMessage: String) :
        MessagingUserProfilePictureStatements(errorMessage = errorMessage)

    object Loading : MessagingUserProfilePictureStatements()
}

@HiltViewModel
class ChatScreenViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val realtimeDB: FirebaseDatabase,
    private val db: FirebaseFirestore,
    private val storage: FirebaseStorage
) : ViewModel() {

    val messagingUserState: MutableState<MessagingUserStatements> =
        mutableStateOf(MessagingUserStatements.Loading)
    val messagingUserProfilePictureState: MutableState<MessagingUserProfilePictureStatements> =
        mutableStateOf(MessagingUserProfilePictureStatements.Loading)
    val messageList = mutableStateListOf<MessageModel>()
    private var messageListener: ChildEventListener? = null


    fun GetMessagingUser(uid: String) {
        messagingUserState.value = MessagingUserStatements.Loading
        viewModelScope.launch(Dispatchers.IO) {
            db.collection("users").document(uid).get().addOnSuccessListener {
                if (it.exists()) {
                    messagingUserState.value =
                        MessagingUserStatements.Success(it.toObject(UserModel::class.java)!!)
                }
            }.addOnFailureListener {
                messagingUserState.value =
                    MessagingUserStatements.Error(it.localizedMessage ?: "Error")
            }
        }
    }

    fun GetMessagingUserProfilePicture(uid: String) {
        messagingUserProfilePictureState.value = MessagingUserProfilePictureStatements.Loading
        val imageRef = storage.reference.child("profile_pictures").child(uid)
        viewModelScope.launch(Dispatchers.IO) {
            imageRef.getBytes(512 * 512).addOnSuccessListener { image ->
                messagingUserProfilePictureState.value =
                    MessagingUserProfilePictureStatements.Success(image)/////////////////////////////
            }.addOnFailureListener {
                messagingUserProfilePictureState.value =
                    MessagingUserProfilePictureStatements.Error(it.localizedMessage ?: "Error")
            }
        }
    }

    fun SendMessage(message: String, chatID: String) {
        val messageRef = realtimeDB.getReference("chats").child(chatID).push()
        val messageMap = hashMapOf(
            "message" to message,
            "sender" to auth.currentUser?.uid,
            "time" to Timestamp.now().toDate().time,
            "readed" to false,
            "messageID" to messageRef.key,
            "chatID" to chatID
        )
        viewModelScope.launch(Dispatchers.IO) {
            messageRef.setValue(messageMap).addOnSuccessListener {
            }.addOnFailureListener {
                println("mesaj yüklenirken hata oluştu: " + it.localizedMessage)
            }.addOnCanceledListener {
                println("mesaj iptal edildi")
            }
            if (auth.currentUser != null) {
                val userRef = db.collection("users").document(auth.currentUser!!.uid)
                userRef.update("chats", FieldValue.arrayUnion(chatID))
                val messagingUserID = chatID.split("_").find { it != auth.currentUser!!.uid }
                if (messagingUserID != null) {
                    val messagingUserRef = db.collection("users").document(messagingUserID)
                    messagingUserRef.update("chats", FieldValue.arrayUnion(chatID))
                }

            }
        }
    }

    fun GetMessages(chatID: String) {
        messageList.clear()
        val chatRef = realtimeDB.getReference("chats").child(chatID)
        viewModelScope.launch(Dispatchers.IO) {
            messageListener = object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val newMessage = snapshot.getValue(MessageModel::class.java)
                    if (newMessage != null) {
                        messageList.add(0, newMessage)
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

                override fun onChildRemoved(snapshot: DataSnapshot) {}

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

                override fun onCancelled(error: DatabaseError) {}

            }
            chatRef.addChildEventListener(messageListener!!)
        }
    }

    fun RemoveMessageListener(chatID: String) {
        val chatRef = realtimeDB.getReference("chats").child(chatID)
        if (messageListener != null) {
            chatRef.removeEventListener(messageListener!!)
        }
    }

}