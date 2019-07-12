package com.trustwallet.walletconnect

import com.github.salomonbrys.kotson.fromJson
import com.github.salomonbrys.kotson.int
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import com.trustwallet.walletconnect.exceptions.InvalidJsonRpcRequestException
import com.trustwallet.walletconnect.extensions.hexStringToByteArray
import com.trustwallet.walletconnect.jsonrpc.JsonRpcRequest
import com.trustwallet.walletconnect.models.*
import com.trustwallet.walletconnect.models.ethereum.WCEthereumSignPayload
import com.trustwallet.walletconnect.models.session.WCSession
import com.trustwallet.walletconnect.models.session.WCSessionUpdateParams
import com.trustwallet.walletconnect.security.decrypt
import com.trustwallet.walletconnect.security.encrypt
import okhttp3.*
import okio.ByteString
import java.util.*
import kotlin.concurrent.schedule

const val JSONRPC_VERSION = "2.0"

private const val PING_TIMER_PERIOD: Long = 15_000

class WCInteractor (
    private val session: WCSession,
    private val meta: WCPeerMeta,
    private val client: OkHttpClient,
    private val delegate: WCInteractorDelegate
): WebSocketListener() {
    private val socket: WebSocket
    private val gson: Gson
    private val clientId = UUID.randomUUID().toString()
    private var peerId: String? = null
    private var timer: Timer? = null

    init {
        val request = Request.Builder()
            .url(session.bridge)
            .build()

        socket = client.newWebSocket(request, this)

        gson = GsonBuilder().create()
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        startPingTimer()
        subscribe(session.topic)
        subscribe(clientId)
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        val message = WCMessageParser(gson, text).parse()
        val payload = String(decrypt(message.payload, session.key.hexStringToByteArray()), Charsets.UTF_8)
        val obj = JsonParser().parse(payload).obj
        val id = obj.get("id").int
        val method = WCMethod.from(obj.get("method").string)

        if (method != null) {
            try {
                handleMethod(method, id, payload)
            } catch (e: Exception) {
                delegate.onFailure(e)
            }
        }

        delegate.onCustomRequest(id, payload)
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

    private fun handleMethod(method: WCMethod, id: Int, payload: String) {
        when (method) {
            WCMethod.SESSION_REQUEST -> delegate.onSessionRequest(id, getFirstParam(payload))
            WCMethod.SESSION_UPDATE -> {
                if (!getFirstParam<WCSessionUpdateParams>(payload).approved) {
                    disconnect()
                }
            }
            WCMethod.ETH_SIGN -> {
                val params = getParams<String>(payload)
                if (params.size < 2)
                    throw InvalidJsonRpcRequestException()
                delegate.onEthSign(id, WCEthereumSignPayload(params[1].hexStringToByteArray(), params))
            }
            WCMethod.ETH_PERSONAL_SIGN -> {
                val params = getParams<String>(payload)
                if (params.size < 2)
                    throw InvalidJsonRpcRequestException()
                delegate.onEthSign(id, WCEthereumSignPayload(params[0].hexStringToByteArray(), params))
            }
            WCMethod.ETH_SIGN_TYPE_DATA -> {
                val params = getParams<String>(payload)
                if (params.size < 2)
                    throw InvalidJsonRpcRequestException()
                delegate.onEthSign(id, WCEthereumSignPayload(params[0].toByteArray(Charsets.UTF_8), params))
            }
            WCMethod.ETH_SIGN_TRANSACTION -> delegate.onEthTransaction(id, getFirstParam(payload))
            WCMethod.ETH_SEND_TRANSACTION -> delegate.onEthTransaction(id, getFirstParam(payload))
        }
    }
    
    private fun subscribe(topic: String):Boolean {
        val message = WCSocketMessage(
            topic = topic,
            type = MessageType.SUB,
            payload = ""
        )
        return socket.send(gson.toJson(message))
    }

    private fun encryptAndSend(data: ByteArray):Boolean {
        val message = WCSocketMessage(
            topic = peerId ?: session.topic,
            type = MessageType.PUB,
            payload = encrypt(data, session.key.hexStringToByteArray())
        )
        return socket.send(gson.toJson(message))
    }


    private fun disconnect() {
        socket.close(1000, null)
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

    private fun <T> getParams(payload: String): Array<T> {
        return gson.fromJson<JsonRpcRequest<T>>(payload).params
    }

    private fun <T> getFirstParam(payload: String): T {
        val params = getParams<T>(payload)
        if (params.isEmpty())
            throw InvalidJsonRpcRequestException()
        return params[0]
    }
}

class WCMessageParser(private val gson: Gson, private val text: String) {
    fun parse(): WCSocketMessage<WCEncryptionPayload> {
        return try {
            gson.fromJson(text)
        } catch (e: JsonParseException) {
            val message = gson.fromJson<WCSocketMessage<String>>(text)
            val payload = gson.fromJson<WCEncryptionPayload>(message.payload)

            return WCSocketMessage(
                topic = message.topic,
                type = message.type,
                payload = payload
            )
        }
    }
}