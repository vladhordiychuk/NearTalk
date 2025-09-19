package com.neartalk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neartalk.data.repository.ChatRepository
import com.neartalk.domain.model.Chat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateOf
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _chats = mutableStateOf<List<Chat>>(emptyList())
    val chats = _chats

    val searchQuery = mutableStateOf("")
    val selectedTab = mutableStateOf(1)

    init {
        viewModelScope.launch {
            chatRepository.getAllChats().collect { chatsList ->
                _chats.value = chatsList
                    .filter { it.name.contains(searchQuery.value, ignoreCase = true) }
                    .sortedWith(compareByDescending<Chat> { it.isPinned }.thenByDescending { it.time })
            }
        }
    }

    fun filterChats(query: String) {
        searchQuery.value = query
    }

    fun deleteChat(chat: Chat) {
        viewModelScope.launch {
            _chats.value = _chats.value.filter { it.id != chat.id }
            chatRepository.deleteChat(chat.id)
        }
    }

    fun togglePinChat(chat: Chat) {
        viewModelScope.launch {
            val newPinnedStatus = !chat.isPinned
            _chats.value = _chats.value.map {
                if (it.id == chat.id) it.copy(isPinned = newPinnedStatus) else it
            }.sortedWith(compareByDescending<Chat> { it.isPinned }.thenByDescending { it.time })
            chatRepository.updatePinStatus(chat.id, newPinnedStatus)
        }
    }

    fun toggleMuteChat(chat: Chat) {
        viewModelScope.launch {
            val newMutedStatus = !chat.isMuted
            _chats.value = _chats.value.map {
                if (it.id == chat.id) it.copy(isMuted = newMutedStatus) else it
            }.sortedWith(compareByDescending<Chat> { it.isPinned }.thenByDescending { it.time })
            chatRepository.updateMuteStatus(chat.id, newMutedStatus)
        }
    }
}