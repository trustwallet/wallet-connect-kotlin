package com.trustwallet.walletconnect

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import com.trustwallet.walletconnect.exceptions.InvalidJsonRpcRequestException
import com.trustwallet.walletconnect.exceptions.InvalidSessionException
import com.trustwallet.walletconnect.extensions.hexStringToByteArray
import com.trustwallet.walletconnect.jsonrpc.*
import com.trustwallet.walletconnect.models.*
import com.trustwallet.walletconnect.models.binance.*
import com.trustwallet.walletconnect.models.ethereum.WCEthereumSignMessage
import com.trustwallet.walletconnect.models.ethereum.WCEthereumTransaction
import com.trustwallet.walletconnect.models.session.WCApproveSessionResponse
import com.trustwallet.walletconnect.models.session.WCSession
import com.trustwallet.walletconnect.models.session.WCSessionRequest
import com.trustwallet.walletconnect.models.session.WCSessionUpdate
import com.trustwallet.walletconnect.security.decrypt
import com.trustwallet.walletconnect.security.encrypt
import okhttp3.*
import okio.ByteString
import java.util.*

const val JSONRPC_VERSION = "2.0"

inline fun <reified T> typeToken() = object: TypeToken<T>(){}.type

class WCInteractor (
    private val session: WCSession,
    private val clientMeta: WCPeerMeta,
    private val client: OkHttpClient,
    private val gson: Gson = Gson()
): WebSocketListener() {
    private var socket: WebSocket? = null
    private val clientId = UUID.randomUUID().toString()
    private var peerId: String? = null
    private var handshakeId: Long = -1

    var onFailure: (Throwable) -> Unit = { _ -> Unit}
    var onDisconnect: (code: Int, reason: String) -> Unit = { _, _ -> Unit }
    var onSessionRequest: (id: Long, peer: WCPeerMeta) -> Unit = { _, _ -> Unit }
    var onEthSign: (id: Long, message: WCEthereumSignMessage) -> Unit = { _, _ -> Unit }
    var onEthTransaction: (id: Long, transaction: WCEthereumTransaction) -> Unit = { _, _ -> Unit }
    var onCustomRequest: (id: Long, payload: String) -> Unit = { _, _ -> Unit }
    var onBnbTrade: (id: Long, order: WCBinanceTradeOrder) -> Unit = { _, _ -> Unit }
    var onBnbCancel: (id: Long, order: WCBinanceCancelOrder) -> Unit = { _, _ -> Unit }
    var onBnbTransfer: (id: Long, order: WCBinanceTransferOrder) -> Unit = { _, _ -> Unit }
    var onBnbTxConfirm: (id: Long, order: WCBinanceTxConfirmParam) -> Unit = { _, _ -> Unit }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        subscribe(session.topic)
        subscribe(clientId)
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        val message = WCMessageParser(gson, text).message
        val payload = String(decrypt(message.payload, session.key.hexStringToByteArray()), Charsets.UTF_8)
        val request = gson.fromJson<JsonRpcRequest<JsonArray>>(payload, typeToken<JsonRpcRequest<JsonArray>>())

        try {
            val method = request.wcMethod
            if (method != null) {
                handleRequest(request)
            } else {
                onCustomRequest(request.id, payload)
            }
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        onFailure(t)
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        handshakeId = -1
        onDisconnect(code, reason)
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

    fun approveSesssion(accounts: List<String>, chainId: Int): Boolean {
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
            params = listOf(
                WCSessionUpdate(
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

    private fun handleRequest(request: JsonRpcRequest<JsonArray>) {
        when (request.wcMethod) {
            WCMethod.SESSION_REQUEST -> {
                val param = gson.fromJson<List<WCSessionRequest>>(request.params, typeToken<List<WCSessionRequest>>())
                        .firstOrNull() ?: throw InvalidJsonRpcRequestException()
                handshakeId = request.id
                peerId = param.peerId
                onSessionRequest(request.id, param.peerMeta)
            }
            WCMethod.SESSION_UPDATE -> {
                val param = gson.fromJson<List<WCSessionUpdate>>(request.params, typeToken<List<WCSessionUpdate>>())
                        .firstOrNull() ?: throw InvalidJsonRpcRequestException()
                if (!param.approved) {
                    disconnect()
                }
            }
            WCMethod.ETH_SIGN -> {
                val params = gson.fromJson<List<String>>(request.params, typeToken<List<String>>())
                if (params.size < 2)
                    throw InvalidJsonRpcRequestException()
                onEthSign(request.id, WCEthereumSignMessage(params, WCEthereumSignMessage.WCMessageType.MESSAGE))
            }
            WCMethod.ETH_PERSONAL_SIGN -> {
                val params = gson.fromJson<List<String>>(request.params, typeToken<List<String>>())
                if (params.size < 2)
                    throw InvalidJsonRpcRequestException()
                onEthSign(request.id, WCEthereumSignMessage(params, WCEthereumSignMessage.WCMessageType.PERSONAL_MESSAGE))
            }
            WCMethod.ETH_SIGN_TYPE_DATA -> {
                val params = gson.fromJson<List<String>>(request.params, typeToken<List<String>>())
                if (params.size < 2)
                    throw InvalidJsonRpcRequestException()
                onEthSign(request.id, WCEthereumSignMessage(params, WCEthereumSignMessage.WCMessageType.TYPED_MESSAGE))
            }
            WCMethod.ETH_SIGN_TRANSACTION -> {
                val param = gson.fromJson<List<WCEthereumTransaction>>(request.params, typeToken<List<WCEthereumTransaction>>())
                        .firstOrNull() ?: throw InvalidJsonRpcRequestException()
                onEthTransaction(request.id, param)
            }
            WCMethod.ETH_SEND_TRANSACTION ->{
                val param = gson.fromJson<List<WCEthereumTransaction>>(request.params, typeToken<List<WCEthereumTransaction>>())
                        .firstOrNull() ?: throw InvalidJsonRpcRequestException()
                onEthTransaction(request.id, param)
            }
            WCMethod.BNB_SIGN -> {
                val cancelOrder = try {
                    gson.fromJson<List<WCBinanceCancelOrder>>(request.params, typeToken<List<WCBinanceCancelOrder>>())
                            .firstOrNull() ?: throw InvalidJsonRpcRequestException()
                } catch (e: JsonParseException) { null }

                val tradeOrder = try {
                    gson.fromJson<List<WCBinanceTradeOrder>>(request.params, typeToken<List<WCBinanceTradeOrder>>())
                            .firstOrNull() ?: throw InvalidJsonRpcRequestException()
                } catch (e: JsonParseException) { null }
                val transferOrder = try {
                    gson.fromJson<List<WCBinanceTransferOrder>>(request.params, typeToken<List<WCBinanceTransferOrder>>())
                            .firstOrNull() ?: throw InvalidJsonRpcRequestException()
                } catch (e: JsonParseException) { null }

                if (cancelOrder != null) {
                    onBnbCancel(request.id, cancelOrder)
                }

                if (tradeOrder != null) {
                    onBnbTrade(request.id, tradeOrder)
                }

                if (transferOrder != null) {
                    onBnbTransfer(request.id, transferOrder)
                }
            }
            WCMethod.BNB_TRANSACTION_CONFIRM -> {
                val param = gson.fromJson<List<WCBinanceTxConfirmParam>>(request.params, typeToken<List<WCBinanceTxConfirmParam>>())
                        .firstOrNull() ?: throw InvalidJsonRpcRequestException()
                onBnbTxConfirm(request.id, param)
            }
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
}

class WCMessageParser(gson: Gson, text: String) {
    val message: WCSocketMessage<WCEncryptionPayload>

    init {
        message = try {
             gson.fromJson(text, typeToken<WCSocketMessage<WCEncryptionPayload>>())
        } catch (e: JsonParseException) {
            val message = gson.fromJson<WCSocketMessage<String>>(text, typeToken<WCSocketMessage<String>>())
            val payload = gson.fromJson<WCEncryptionPayload>(message.payload, typeToken<WCEncryptionPayload>())
            WCSocketMessage(
                topic = message.topic,
                type = message.type,
                payload = payload
            )
        }
    }
}

private fun generateId(): Long {
    return Date().time
}