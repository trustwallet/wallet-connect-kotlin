package com.trustwallet.walletconnect

import com.github.salomonbrys.kotson.*
import com.google.gson.*
import com.trustwallet.walletconnect.exceptions.InvalidJsonRpcRequestException
import com.trustwallet.walletconnect.exceptions.InvalidSessionException
import com.trustwallet.walletconnect.extensions.hexStringToByteArray
import com.trustwallet.walletconnect.jsonrpc.JsonRpcError
import com.trustwallet.walletconnect.jsonrpc.JsonRpcRequest
import com.trustwallet.walletconnect.jsonrpc.JsonRpcResponse
import com.trustwallet.walletconnect.models.*
import com.trustwallet.walletconnect.models.ethereum.WCEthereumSignPayload
import com.trustwallet.walletconnect.models.session.WCApproveSessionResponse
import com.trustwallet.walletconnect.models.session.WCSession
import com.trustwallet.walletconnect.models.session.WCSessionRequestParam
import com.trustwallet.walletconnect.models.session.WCSessionUpdateParam
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
    private val clientMeta: WCPeerMeta,
    private val client: OkHttpClient,
    private val gson: Gson = Gson()
): WebSocketListener() {
    private var socket: WebSocket? = null
    private val clientId = UUID.randomUUID().toString()
    private var peerId: String? = null
    private var timer: Timer? = null
    private var handshakeId: Long = -1

    var failureListener: FailureListener? = null
    var disconnectListener: DisconnectListener? = null
    var sessionRequestListener: SessionRequestListener? = null
    var ethSignListener: EthSignListener? = null
    var ethTransactionListener: EthTransactionListener? = null
    var customRequestListener: CustomRequestListener? = null

    override fun onOpen(webSocket: WebSocket, response: Response) {
        startPingTimer()
        subscribe(session.topic)
        subscribe(clientId)
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        val message = WCMessageParser(gson, text).parse()
        val payload = String(decrypt(message.payload, session.key.hexStringToByteArray()), Charsets.UTF_8)
        val helper = WCMessageHelper(payload)
        val id = helper.id
        val method = helper.method

        if (method != null) {
            try {
                handleMethod(method, id, payload)
            } catch (e: Exception) {
                failureListener?.onFailure(e)
            }
        }

        customRequestListener?.onCustomRequest(id, payload)
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        stopPingTimer()
        failureListener?.onFailure(t)
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        stopPingTimer()
        handshakeId = -1
        disconnectListener?.onDisconnect(code, reason)
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        // Pong messages should be ignored for now
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        // Closing event should be ignored for now
    }

    fun connect() {
        val request = Request.Builder()
            .url(session.bridge)
            .build()

        socket = client.newWebSocket(request, this)
    }

    fun approveSesssion(accounts: Array<String>, chainId: Int): Boolean {
        if (handshakeId <= 0) {
            throw InvalidSessionException()
        }

        val result = WCApproveSessionResponse(
            chainId = chainId,
            accounts = accounts,
            peerId = peerId,
            peerMeta = clientMeta
        )
        val response = JsonRpcResponse(
            id = handshakeId,
            result = result
        )
        return encryptAndSend(gson.toJson(response).toByteArray(Charsets.UTF_8))
    }

    fun rejectSession(message: String = "Session rejected"): Boolean {
        if (handshakeId <= 0) {
            throw InvalidSessionException()
        }

        val response = JsonRpcResponse(
            id = handshakeId,
            result = JsonRpcError(
                code = -32000,
                message = message
            )
        )

        return encryptAndSend(gson.toJson(response).toByteArray(Charsets.UTF_8))
    }

    fun killSession(): Boolean {
        val request = JsonRpcRequest(
            id = generateId(),
            method = WCMethod.SESSION_UPDATE.method,
            params = arrayOf(
                WCSessionUpdateParam(
                    approved = false,
                    chainId = null,
                    accounts = null
                )
            )
        )

        return encryptAndSend(gson.toJson(request).toByteArray(Charsets.UTF_8))
                && disconnect()
    }

    fun <T> approveRequest(id: Long, result: T): Boolean {
        val response = JsonRpcResponse(
            id = id,
            result = result
        )

        return encryptAndSend(gson.toJson(response).toByteArray(Charsets.UTF_8))
    }

    fun rejectRequest(id: Long, message: String): Boolean {
        val response = JsonRpcResponse(
            id = id,
            result = JsonRpcError(
                code = -32000,
                message = message
            )
        )

        return encryptAndSend(gson.toJson(response).toByteArray(Charsets.UTF_8))
    }

    private fun handleMethod(method: WCMethod, id: Long, payload: String) {
        when (method) {
            WCMethod.SESSION_REQUEST -> {
                val param = getFirstParam<WCSessionRequestParam>(payload)
                handshakeId = id
                peerId = param.peerId
                sessionRequestListener?.onSessionRequest(id, getFirstParam(payload))
            }
            WCMethod.SESSION_UPDATE -> {
                if (!getFirstParam<WCSessionUpdateParam>(payload).approved) {
                    disconnect()
                }
            }
            WCMethod.ETH_SIGN -> {
                val params = getParams<String>(payload)
                if (params.size < 2)
                    throw InvalidJsonRpcRequestException()
                ethSignListener?.onEthSign(id, WCEthereumSignPayload(params[1].hexStringToByteArray(), params))
            }
            WCMethod.ETH_PERSONAL_SIGN -> {
                val params = getParams<String>(payload)
                if (params.size < 2)
                    throw InvalidJsonRpcRequestException()
                ethSignListener?.onEthSign(id, WCEthereumSignPayload(params[0].hexStringToByteArray(), params))
            }
            WCMethod.ETH_SIGN_TYPE_DATA -> {
                val params = getParams<String>(payload)
                if (params.size < 2)
                    throw InvalidJsonRpcRequestException()
                ethSignListener?.onEthSign(id, WCEthereumSignPayload(params[0].toByteArray(Charsets.UTF_8), params))
            }
            WCMethod.ETH_SIGN_TRANSACTION -> ethTransactionListener?.onEthTransaction(id, getFirstParam(payload))
            WCMethod.ETH_SEND_TRANSACTION -> ethTransactionListener?.onEthTransaction(id, getFirstParam(payload))
        }
    }
    
    private fun subscribe(topic: String): Boolean {
        val message = WCSocketMessage(
            topic = topic,
            type = MessageType.SUB,
            payload = ""
        )
        return socket?.send(gson.toJson(message)) ?: false
    }

    private fun encryptAndSend(data: ByteArray): Boolean {
        val message = WCSocketMessage(
            topic = peerId ?: session.topic,
            type = MessageType.PUB,
            payload = encrypt(data, session.key.hexStringToByteArray())
        )
        return socket?.send(gson.toJson(message)) ?: false
    }


    private fun disconnect(): Boolean {
        return socket?.close(1000, null) ?: false
    }

    private fun startPingTimer() {
        timer = Timer()
        timer?.schedule(0, PING_TIMER_PERIOD) {
            socket?.send(ByteString.EMPTY)
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

class WCMessageHelper(payload: String) {
    enum class MetadataKey(val key: String) {
        ID("id"),
        METHOD("method"),
    }

    private val obj = JsonParser().parse(payload).obj
    val id get() = obj.get(MetadataKey.ID.key).long
    val method get() = WCMethod.from(obj.get(MetadataKey.METHOD.key).string)
}

private fun generateId(): Long {
    return Date().time
}