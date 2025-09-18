package com.neartalk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neartalk.data.repository.UserRepository
import com.neartalk.domain.model.Chat
import com.neartalk.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateOf
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _chats = mutableStateOf<List<Chat>>(emptyList())
    val chats = _chats

    val searchQuery = mutableStateOf("")
    val selectedTab = mutableStateOf(1)

    init {
        viewModelScope.launch {
            userRepository.getAllUsers().collect { users ->
                _chats.value = users.filter { it.id != 0 }
                    .map { user ->
                        Chat(
                            id = user.id,
                            name = user.name,
                            lastMessage = getLastMessageForUser(user.id),
                            time = getTimeForUser(user.id),
                            isPinned = isPinnedForUser(user.id),
                            isSentByMe = true,
                            unreadCount = getUnreadCountForUser(user.id),
                            isRead = user.id != 3,
                        )
                    }.sortedWith(compareByDescending<Chat> { it.isPinned }.thenByDescending { it.time })
            }
        }
    }

    private fun getLastMessageForUser(id: Int): String = when (id) {
        1 -> "Hey, how are you?"
        2 -> "Let's meet tomorrow"
        3 -> "Cool project!"
        4 -> "See you soon"
        5 -> "Thanks"
        else -> ""
    }

    private fun getTimeForUser(id: Int): Long = when (id) {
        1 -> 1694449500
        2 -> 1694446200
        3 -> 1694360000
        4 -> 1694260000
        5 -> 1694170000
        else -> System.currentTimeMillis() / 1000
    }

    private fun isPinnedForUser(id: Int): Boolean = id in listOf(1, 3)

    private fun getUnreadCountForUser(id: Int): Int = when (id) {
        3 -> 102
        4 -> 12
        else -> 0
    }

    fun filterChats(query: String) {
        searchQuery.value = query
        _chats.value = _chats.value.filter { it.name.contains(query, ignoreCase = true) }
            .sortedWith(compareByDescending<Chat> { it.isPinned }.thenByDescending { it.time })
    }

    fun togglePin(chatId: Int) {
        _chats.value = _chats.value.map {
            if (it.id == chatId) it.copy(isPinned = !it.isPinned) else it
        }.sortedWith(compareByDescending<Chat> { it.isPinned }.thenByDescending { it.time })
    }
}