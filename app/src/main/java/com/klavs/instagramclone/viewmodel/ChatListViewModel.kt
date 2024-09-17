package com.klavs.instagramclone.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.dataObjects
import com.google.firebase.firestore.snapshots
import com.google.firebase.storage.FirebaseStorage
import com.klavs.instagramclone.model.ChatItemModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject


sealed class ChatListStatement(val data: List<ChatItemModel>? = null, val message: String? = null) {
    object Loading : ChatListStatement()
    class Success(data: List<ChatItemModel>) : ChatListStatement(data = data)
    class Error(message: String) : ChatListStatement(message = message)
    object Empty : ChatListStatement()
}

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val realtimeDB: FirebaseDatabase
) : ViewModel() {

    val mutableStateChatList = mutableStateListOf<ChatItemModel>()
    val chatListState: MutableState<ChatListStatement> = mutableStateOf(ChatListStatement.Loading)
    val chatItemPictures = mutableStateMapOf<String,ByteArray>()


    fun GetList() {
        mutableStateChatList.clear()
        chatListState.value = ChatListStatement.Loading
        if (auth.currentUser != null) {
            val chatListRef =
                db.collection("users").document(auth.currentUser!!.uid)
            viewModelScope.launch(Dispatchers.IO) {
                chatListRef.get().addOnSuccessListener {
                    if (it.exists()) {
                        val chatList = it.get("chats") as? List<String>?: emptyList()
                        if (chatList.isEmpty()){
                            chatListState.value = ChatListStatement.Empty
                            return@addOnSuccessListener
                        }
                        chatList.forEach {
                            val chat_id = it
                            val messaging_user_id =
                                chat_id.split("_").find { it != auth.currentUser!!.uid }
                            if (messaging_user_id!=null){
                                GetChatItemPicture(messaging_user_id)
                                val messagingUserRef =
                                    db.collection("users").document(messaging_user_id)
                                messagingUserRef.get()
                                    .addOnSuccessListener {
                                        val messaging_user_name = it.get("username").toString()
                                        val chatRef =
                                            realtimeDB.getReference("chats").child(chat_id)
                                        chatRef.orderByChild("time").limitToLast(1).get()
                                            .addOnSuccessListener {
                                                if (it.exists()){
                                                    if (it !=null){
                                                        val last_message = it.children.first()
                                                            .child("message").value.toString()
                                                        val last_message_date: Date =
                                                            Date(it.children.first().child("time").value as Long)

                                                        val last_message_sender = it.children.first()
                                                            .child("sender").value.toString()
                                                        mutableStateChatList.add(
                                                            ChatItemModel(
                                                                chat_id,
                                                                messaging_user_id,
                                                                messaging_user_name,
                                                                last_message,
                                                                last_message_date,
                                                                last_message_sender
                                                            )
                                                        )
                                                        if (chatList.size == mutableStateChatList.size) {
                                                            mutableStateChatList.sortByDescending { chatItemModel ->
                                                                chatItemModel.lastMessageDate
                                                            }
                                                            chatListState.value =
                                                                ChatListStatement.Success(mutableStateChatList)
                                                            println("chatlist yüklendi: "+mutableStateChatList.size)
                                                        }else{
                                                            println("chatlist yükleniyortem: "+mutableStateChatList.size)
                                                        }
                                                    }
                                                }


                                            }.addOnFailureListener {
                                                chatListState.value = ChatListStatement.Error("ucuncude: "+it.message.toString())
                                            }
                                    }.addOnFailureListener {
                                        chatListState.value = ChatListStatement.Error("ikincide: "+it.message.toString())
                                    }
                            }

                        }
                    }
                }.addOnFailureListener {
                    chatListState.value = ChatListStatement.Error("birincide: "+it.message.toString())
                }
            }

        }
    }

    fun GetChatItemPicture(uid:String){
        val imageRef = storage.reference.child("profile_pictures").child(uid)
        viewModelScope.launch(Dispatchers.IO) { imageRef.getBytes(512*512).addOnSuccessListener { image->
            chatItemPictures[uid] = image
        }.addOnFailureListener {
            println("resim bulunamadı")
        } }

    }
}