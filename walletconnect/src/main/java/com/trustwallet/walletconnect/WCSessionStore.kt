package com.trustwallet.walletconnect

import android.content.SharedPreferences
import com.github.salomonbrys.kotson.*
import com.google.gson.GsonBuilder
import com.trustwallet.walletconnect.models.WCPeerMeta
import com.trustwallet.walletconnect.models.session.WCSession
import java.util.*

data class WCSessionStoreItem(
    val session: WCSession,
    val peerId: String,
    val peerMeta: WCPeerMeta,
    val isAutoSign: Boolean = false,
    val date: Date = Date()
)

class WCSessionStore(
    private val sharedPreferences: SharedPreferences,
    builder: GsonBuilder = GsonBuilder()
) {
    private val gson = builder
        .serializeNulls()
        .create()

    private fun store(item: WCSessionStoreItem?) {
        if (item != null) {
            sharedPreferences.edit().putString(SESSION_KEY, gson.toJson(item)).apply()
        } else {
            sharedPreferences.edit().remove(SESSION_KEY).apply()
        }
    }

    private fun load(): WCSessionStoreItem? {
        val json = sharedPreferences.getString(SESSION_KEY, null) ?: return null
        return gson.fromJson(json)
    }

    var session: WCSessionStoreItem?
        set(item) = store(item)
        get() = load()

    companion object {
        private const val SESSION_KEY = "org.walletconnect.session"
    }
}