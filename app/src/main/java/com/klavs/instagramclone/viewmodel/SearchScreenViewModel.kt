package com.klavs.instagramclone.viewmodel

import android.graphics.BitmapFactory
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.algolia.search.client.ClientSearch
import com.algolia.search.model.APIKey
import com.algolia.search.model.ApplicationID
import com.algolia.search.model.IndexName
import com.algolia.search.model.search.Query
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.klavs.instagramclone.room.RecentSearchesModel
import com.klavs.instagramclone.util.RecentSearchesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject

sealed class RecentSearchesStatements(val message: String? = "", val data: List<RecentSearchesModel>? = null){
    object Loading: RecentSearchesStatements()
    class Success(data: List<RecentSearchesModel>): RecentSearchesStatements(data = data)
    object Empty: RecentSearchesStatements()
    class Error(message: String): RecentSearchesStatements(message = message)
}
sealed class SearchStatements(val data: MutableList<Map<String,Any>>? = null, val message: String? = "") {
    object Loading : SearchStatements()
    class Success(list: MutableList<Map<String, Any>>): SearchStatements(data = list)
    object Empty : SearchStatements()
    class Error(message: String) : SearchStatements(message = message)
    object Idle : SearchStatements()
}


@HiltViewModel
class SearchScreenViewModel @Inject constructor(
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth,
    private val repository: RecentSearchesRepository
): ViewModel() {


    val client =
        ClientSearch(ApplicationID(""), APIKey(""))
    val index = client.initIndex(IndexName("my_algolia_index"))
    var searchState: MutableState<SearchStatements> = mutableStateOf(SearchStatements.Idle)
    val recentSearchesState: MutableState<RecentSearchesStatements> = mutableStateOf(RecentSearchesStatements.Loading)
    val searchResults = mutableListOf<Map<String,Any>>(emptyMap())


    fun GetAllRecentSearches() {
        recentSearchesState.value = RecentSearchesStatements.Loading
        viewModelScope.launch(Dispatchers.IO) {

            try {
                val recentSearches = repository.getRecentSearchesByUser(auth.currentUser!!.uid)
                if (recentSearches.isNotEmpty()) {
                    recentSearchesState.value = RecentSearchesStatements.Success(recentSearches)
                } else {
                    recentSearchesState.value = RecentSearchesStatements.Empty
                }
            }catch (e: Exception){
                recentSearchesState.value = RecentSearchesStatements.Error(e.localizedMessage)
            }
        }
    }
    fun InsertRecentSearch(user_id: String,username: String, image: ByteArray) {
        viewModelScope.launch(Dispatchers.IO) {
            val recentSearch = RecentSearchesModel(
                user_id = user_id,
                username = username,
                search_date = System.currentTimeMillis().toString(),
                searched_by = auth.currentUser!!.uid,
                image = image
            )
            try {
                repository.insertRecentSearch(recentSearch)
            }catch (e: Exception){
            }
        }
    }

    fun SearchUsers(query: String) {
        searchResults.clear()
        if (query == "") {
            searchState.value = SearchStatements.Idle
            return
        }
        searchState.value = SearchStatements.Loading
        viewModelScope.launch(Dispatchers.IO) {
            val result = index.search(Query(query))

            if (result.hits.isEmpty()) {
                searchState.value = SearchStatements.Empty

                return@launch
            }
            result.hits.forEach { hit ->


                val uid = hit.json.get("uid")?.jsonPrimitive?.contentOrNull
                val username = hit.json.get("username")?.jsonPrimitive?.contentOrNull
                val storageRef = storage.reference

                if (uid != null && username != null) {

                    val imageRef = storageRef.child("profile_pictures").child(uid)
                    val QUARTER_MEGABYTE: Long = 512 * 512

                    imageRef.getBytes(QUARTER_MEGABYTE).addOnSuccessListener { imageByteArray ->

                        val bitmap =
                            BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.size)

                        if (!searchResults.any { it["user_id"] == uid }){
                            val searchData = mapOf(
                                "user_id" to uid,
                                "username" to username,
                                "image" to bitmap.asImageBitmap()
                            )
                            searchResults.add(searchData)
                        }

                            println(searchResults.size.toString()+" , "+result.hits.size.toString())
                            searchState.value = SearchStatements.Success(searchResults)

                    }.addOnFailureListener {
                        searchState.value = SearchStatements.Error(it.localizedMessage)
                        println("Bir hata oluştu:" + it.localizedMessage)
                    }
                }

            }
        }
    }
}