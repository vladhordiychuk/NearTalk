package com.neartalk.utils

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest
import java.util.UUID

object SecurityUtils {
    private const val PREF_NAME = "NearTalk_Secure_Prefs"
    private const val KEY_MY_ID = "my_anonymous_id"

    fun getMyAnonymousId(context: Context): String {
        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        var id = prefs.getString(KEY_MY_ID, null)
        if (id == null) {
            id = UUID.randomUUID().toString()
            prefs.edit().putString(KEY_MY_ID, id).apply()
        }
        return id!!
    }

    fun generateHash(text: String, timestamp: Long, senderId: String): String {
        val input = "$text|$timestamp|$senderId"
        val bytes = input.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
}