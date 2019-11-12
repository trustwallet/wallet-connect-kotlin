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
    val autoSign: Boolean = false,
    val date: Date = Date()
)

class WCSessionStorage(
    private val sharedPreferences: SharedPreferences,
    builder: GsonBuilder = GsonBuilder()
) {
    private val gson = builder
        .serializeNulls()
        .create()

    val allSessions get(): Map<String, WCSessionStoreItem> {
        val json = sharedPreferences.getString(SESSIONS_KEY, null) ?: return mapOf()
        return gson.fromJson(json)
    }

    fun store(session: WCSession, peerId: String, peerMeta: WCPeerMeta, autoSign: Boolean = false, date: Date = Date()) {
        store(WCSessionStoreItem(session, peerId, peerMeta, autoSign, date))
    }

    fun store(item: WCSessionStoreItem) {
        val sessions = allSessions.toMutableMap()
        sessions[item.session.topic] = item
        store(sessions, item.session.topic)
    }

    fun load(topic: String): WCSessionStoreItem? {
        return allSessions[topic]
    }

    fun clear(topic: String) {
        val sessions = allSessions.toMutableMap()
        sessions.remove(topic)
        store(sessions)
    }

    val lastSession: WCSessionStoreItem? get() {
        val lastTopic = sharedPreferences.getString(LAST_SESSION_KEY, null) ?: return null
        return allSessions[lastTopic]
    }

    fun clearAll() {
        store(mapOf())
    }

    private fun store(items: Map<String, WCSessionStoreItem>, lastTopic: String? = null) {
        val json = gson.toJson(items)
        update {
            it.putString(SESSIONS_KEY, json)
            it.putString(LAST_SESSION_KEY, lastTopic)
        }
    }

    private inline fun update(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = sharedPreferences.edit()
        operation(editor)
        editor.apply()
    }

    companion object {
        private const val SESSIONS_KEY = "org.walletconnect.sessions"
        private const val LAST_SESSION_KEY = "org.walletconnect.last_session"
    }
}