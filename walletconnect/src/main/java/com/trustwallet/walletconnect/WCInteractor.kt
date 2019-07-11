package com.trustwallet.walletconnect

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.trustwallet.walletconnect.exceptions.InvalidMessageException
import com.trustwallet.walletconnect.exceptions.InvalidPayloadException
import com.trustwallet.walletconnect.extensions.hexStringToByteArray
import com.trustwallet.walletconnect.jsonrpc.JsonRpcRequest
import com.trustwallet.walletconnect.models.MessageType
import com.trustwallet.walletconnect.models.WCEncryptionPayload
import com.trustwallet.walletconnect.models.WCPeerMeta
import com.trustwallet.walletconnect.models.WCSocketMessage
import com.trustwallet.walletconnect.models.session.WCSession
import com.trustwallet.walletconnect.security.decrypt
import com.trustwallet.walletconnect.security.encrypt
import okhttp3.*
import okio.ByteString
import java.util.*
import kotlin.concurrent.schedule

const val JSONRPC_VERSION = "2.0"

private const val PING_TIMER_PERIOD: Long = 15_000

class WCInteractor (
    val session: WCSession,
    val meta: WCPeerMeta,
    val client: OkHttpClient,
    val delegate: WCInteractorDelegate
): WebSocketListener() {
    private val socket: WebSocket
    private val moshi: Moshi
    private val clientId = UUID.randomUUID().toString()
    private var peerId: String? = null
    private var timer: Timer? = null

    init {
        val request = Request.Builder()
            .url(session.bridge)
            .build()

        socket = client.newWebSocket(request, this)

        moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    private fun subscribe(topic: String):Boolean {
        val message = WCSocketMessage(
            topic = topic,
            type = MessageType.SUB,
            payload = ""
        )
        val adapter = moshi.adapter<WCSocketMessage<String>>(WCSocketMessage::class.java)
        return socket.send(adapter.toJson(message))
    }

    private fun encryptAndSend(data: ByteArray):Boolean {
        val message = WCSocketMessage(
            topic = peerId ?: session.topic,
            type = MessageType.PUB,
            payload = encrypt(data, session.key.hexStringToByteArray())
        )
        val adapter = moshi.adapter<WCSocketMessage<WCEncryptionPayload>>(WCSocketMessage::class.java)
        return socket.send(adapter.toJson(message))
    }

    private fun startPingTimer() {
        timer = Timer()
        timer?.schedule(0, PING_TIMER_PERIOD) {
            socket.send(ByteString.EMPTY)
        }
    }

    private fun stopPingTimer() {
        timer?.cancel()
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        startPingTimer()
        subscribe(session.topic)
        subscribe(clientId)
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        val message = WCMessageParser(moshi, text).parse()
        val payload = String(decrypt(message.payload, session.key.hexStringToByteArray()), Charsets.UTF_8)
        val adapter = moshi.adapter<JsonRpcRequest<Any>>(JsonRpcRequest::class.java)
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        stopPingTimer()
        delegate.onFailure(t)
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        stopPingTimer()
        delegate.onDisconnect(code, reason)
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        // Pong messages should be ignored for now
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        // Closing event should be ignored for now
    }
}

class WCMessageParser(val moshi: Moshi, val text: String) {
    fun parse(): WCSocketMessage<WCEncryptionPayload> {
        val adapter = moshi.adapter<WCSocketMessage<WCEncryptionPayload>>(WCSocketMessage::class.java)
        val message = adapter.fromJson(text)
        if (message != null)
            return message

        val stringAdapter = moshi.adapter<WCSocketMessage<String>>(WCSocketMessage::class.java)
        val payloadAdapter = moshi.adapter<WCEncryptionPayload>(WCEncryptionPayload::class.java)

        val stringMessage = stringAdapter.fromJson(text) ?: throw InvalidMessageException()
        val payload = payloadAdapter.fromJson(stringMessage.payload) ?: throw InvalidPayloadException()

        return WCSocketMessage(
            topic = stringMessage.topic,
            type = stringMessage.type,
            payload = payload
        )
    }
}