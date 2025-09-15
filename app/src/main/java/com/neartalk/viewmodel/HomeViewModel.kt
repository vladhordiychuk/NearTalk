package com.neartalk.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.neartalk.models.Chat
import com.neartalk.models.ChatType

class HomeViewModel : ViewModel() {

    private val _allChats = listOf(
        Chat(1, "Alice", "Hey, how are you?", 1694449500, true, true, 0, true, true),
        Chat(2, "Bob", "Let's meet tomorrow", 1694446200, false, true, 0, true),
        Chat(3, "Charlie", "Cool project!", 1694360000, true, false, 102),
        Chat(4, "Oleg", "See you soon", 1694260000, true, false, 12),
        Chat(5, "Sasha", "Thanks", 1694170000, true, false, 0)
    )

    val chats = mutableStateOf<List<Chat>>(_allChats)

    val searchQuery = mutableStateOf("")

    val selectedTab = mutableStateOf(1)

    fun filterChats(query: String) {
        searchQuery.value = query
        chats.value = _allChats
            .filter { it.name.contains(query, ignoreCase = true) }
            .sortedWith(compareByDescending<Chat> { it.isPinned }.thenByDescending { it.time })
    }

    fun togglePin(chatId: Int) {
        val updated = chats.value.map {
            if (it.id == chatId) it.copy(isPinned = !it.isPinned) else it
        }.sortedWith(compareByDescending<Chat> { it.isPinned }.thenByDescending { it.time })
        chats.value = updated
    }
}