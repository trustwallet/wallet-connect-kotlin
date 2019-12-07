package com.trustwallet.walletconnect

import com.google.gson.GsonBuilder
import com.trustwallet.walletconnect.models.WCPeerMeta
import com.trustwallet.walletconnect.models.session.WCSession
import okhttp3.OkHttpClient
import java.util.*

class MultiWCClientManager(
    private val httpClient: OkHttpClient,
    private val builder: GsonBuilder = GsonBuilder()
) {

    private val sessionsMap: HashMap<String, WCSession> = hashMapOf()
    private val clientsMap: HashMap<WCSession, WCClient> = hashMapOf()
    private val clientCallbackListenersMap: HashMap<WCSession, WCClientCallBacksListener> =
        hashMapOf()

    fun connect(
        session: WCSession,
        listener: WCClientCallBacksListener,
        peerMeta: WCPeerMeta,
        peerId: String = UUID.randomUUID().toString(),
        remotePeerId: String? = null
    ): String {
        val wcClient = WCClient(httpClient)
        wcClient.connect(session, peerMeta, peerId, remotePeerId)
        wcClient.onFailure = listener.onFailure
        wcClient.onDisconnect = listener.onDisconnect
        wcClient.onSessionRequest = listener.onSessionRequest
        wcClient.onEthSign = listener.onEthSign
        wcClient.onEthSignTransaction = listener.onEthSignTransaction
        wcClient.onEthSendTransaction = listener.onEthSendTransaction
        wcClient.onCustomRequest = listener.onCustomRequest
        wcClient.onBnbTrade = listener.onBnbTrade
        wcClient.onBnbCancel = listener.onBnbCancel
        wcClient.onBnbTransfer = listener.onBnbTransfer
        wcClient.onBnbTxConfirm = listener.onBnbTxConfirm
        wcClient.onGetAccounts = listener.onGetAccounts
        wcClient.onSignTransaction = listener.onSignTransaction
        sessionsMap[session.topic] = session
        clientsMap[session] = wcClient
        clientCallbackListenersMap[session] = listener
        return session.topic
    }

    fun disconnect(topic: String): Boolean {
        var isDisconnected = false
        with(sessionsMap[topic]) {
            isDisconnected = clientsMap[this]?.disconnect() ?: false
            if (isDisconnected) {
                clientsMap.remove(this)
                clientCallbackListenersMap.remove(this)
                sessionsMap.remove(topic)
            }
        }
        return isDisconnected
    }

    fun disconnectAll(): Boolean {
        sessionsMap.entries.forEach { sessionsMapData ->
            disconnect(sessionsMapData.key)
        }
        return sessionsMap.size == 0
    }

    fun approveSession(topic: String, accounts: List<String>, chainId: Int): Boolean? {
        return getClient(topic)?.approveSession(accounts, chainId)
    }

    fun updateSession(
        topic: String,
        accounts: List<String>? = null,
        chainId: Int? = null,
        approved: Boolean = true
    ): Boolean? {
        return getClient(topic)?.updateSession(accounts, chainId, approved)
    }

    fun rejectSession(topic: String, message: String = "Session rejected"): Boolean? {
        return getClient(topic)?.rejectSession(message)
    }

    fun killSession(topic: String): Boolean? {
        return getClient(topic)?.killSession()
    }

    fun <T> approveRequest(topic: String, id: Long, result: T): Boolean? {
        return getClient(topic)?.approveRequest(id, result)
    }

    fun rejectRequest(topic: String, id: Long, message: String = "Reject by the user"): Boolean? {
        return getClient(topic)?.rejectRequest(id, message)
    }

    fun getClient(topic: String): WCClient? {
        with(sessionsMap[topic]) {
            return clientsMap[this]
        }
    }

    fun getListener(topic: String): WCClientCallBacksListener? {
        with(sessionsMap[topic]) {
            return clientCallbackListenersMap[this]
        }
    }
}
