package com.neartalk.domain.model

data class User(
    val id: String,
    val name: String,
    val username: String,
    val phone: String,
    val email: String,
    val birthday: String,
    val status: String,
    val isPinned: Boolean = false,
)