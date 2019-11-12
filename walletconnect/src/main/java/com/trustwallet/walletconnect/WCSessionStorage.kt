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

    fun store(session: WCSession, peerId: String, peerMeta: WCPeerMeta, autoSign: Boolean = false, date: Date = Date()) {
        store(WCSessionStoreItem(session, peerId, peerMeta, autoSign, date))
    }

    fun store(item: WCSessionStoreItem) {
        val topic = item.session.topic

        val sessions = allSessions.toMutableMap()
        sessions[topic] = item

        val topics = allTopics.toMutableList()
        topics.add(topic)

        store(sessions, topics)
    }

    fun load(topic: String): WCSessionStoreItem? {
        return allSessions[topic]
    }

    fun clear(topic: String) {
        val sessions = allSessions.toMutableMap()
        sessions.remove(topic)

        val topics = allTopics.toMutableList()
        topics.remove(topic)

        store(sessions, topics)
    }

    fun clearAll() {
        store(mapOf(), listOf())
    }

    val lastSession: WCSessionStoreItem? get() {
        val topic = allTopics.lastOrNull() ?: return null
        return allSessions[topic]
    }

    val allSessions get(): Map<String, WCSessionStoreItem> {
        val json = sharedPreferences.getString(SESSIONS_KEY, null) ?: return mapOf()
        return gson.fromJson(json)
    }

    private val allTopics: List<String> get() {
        val json = sharedPreferences.getString(TOPICS_KEY, null) ?: return listOf()
        return gson.fromJson(json)
    }

    private fun store(items: Map<String, WCSessionStoreItem>, topics: List<String>) {
        update {
            it.putString(SESSIONS_KEY, gson.toJson(items))
            it.putString(TOPICS_KEY, gson.toJson(topics))
        }
    }

    private inline fun update(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = sharedPreferences.edit()
        operation(editor)
        editor.apply()
    }

    companion object {
        private const val SESSIONS_KEY = "org.walletconnect.sessions"
        private const val TOPICS_KEY = "org.walletconnect.topics"
    }
}