package com.trustwallet.walletconnect

import com.github.salomonbrys.kotson.*
import com.google.gson.*
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

class WCInteractor (
    private val session: WCSession,
    private val clientMeta: WCPeerMeta,
    private val client: OkHttpClient,
    builder: GsonBuilder = GsonBuilder()
): WebSocketListener() {
    private val gson = builder
            .registerTypeAdapter(cancelOrderDeserializer)
            .registerTypeAdapter(tradeOrderDeserializer)
            .registerTypeAdapter(transferOrderDeserializer)
            .create()

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
        val message = gson.fromJson<WCSocketMessage>(text)
        val encrypted = gson.fromJson<WCEncryptionPayload>(message.payload)
        val payload = String(decrypt(encrypted, session.key.hexStringToByteArray()), Charsets.UTF_8)
        val request = gson.fromJson<JsonRpcRequest<JsonArray>>(payload, typeToken<JsonRpcRequest<JsonArray>>())

        try {
            val method = request.method
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
        return encryptAndSend(gson.toJson(response))
    }

    fun updateSession(accounts: List<String>? = null, chainId: Int? = null, approved: Boolean = true): Boolean {
        val request = JsonRpcRequest(
            id = generateId(),
            method = WCMethod.SESSION_UPDATE,
            params = listOf(
                WCSessionUpdate(
                    approved = approved,
                    chainId = chainId,
                    accounts = accounts
                )
            )
        )
        return encryptAndSend(gson.toJson(request))
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

        return encryptAndSend(gson.toJson(response))
    }

    fun killSession(): Boolean {
        return updateSession(approved = false) && disconnect()
    }

    fun <T> approveRequest(id: Long, result: T): Boolean {
        val response = JsonRpcResponse(
            id = id,
            result = result
        )

        return encryptAndSend(gson.toJson(response))
    }

    fun rejectRequest(id: Long, message: String): Boolean {
        val response = JsonRpcResponse(
            id = id,
            result = JsonRpcError(
                code = -32000,
                message = message
            )
        )

        return encryptAndSend(gson.toJson(response))
    }

    private fun handleRequest(request: JsonRpcRequest<JsonArray>) {
        when (request.method) {
            WCMethod.SESSION_REQUEST -> {
                val param = gson.fromJson<List<WCSessionRequest>>(request.params)
                        .firstOrNull() ?: throw InvalidJsonRpcRequestException()
                handshakeId = request.id
                peerId = param.peerId
                onSessionRequest(request.id, param.peerMeta)
            }
            WCMethod.SESSION_UPDATE -> {
                val param = gson.fromJson<List<WCSessionUpdate>>(request.params)
                        .firstOrNull() ?: throw InvalidJsonRpcRequestException()
                if (!param.approved) {
                    disconnect()
                }
            }
            WCMethod.ETH_SIGN -> {
                val params = gson.fromJson<List<String>>(request.params)
                if (params.size < 2)
                    throw InvalidJsonRpcRequestException()
                onEthSign(request.id, WCEthereumSignMessage(params, WCEthereumSignMessage.WCSignType.MESSAGE))
            }
            WCMethod.ETH_PERSONAL_SIGN -> {
                val params = gson.fromJson<List<String>>(request.params)
                if (params.size < 2)
                    throw InvalidJsonRpcRequestException()
                onEthSign(request.id, WCEthereumSignMessage(params, WCEthereumSignMessage.WCSignType.PERSONAL_MESSAGE))
            }
            WCMethod.ETH_SIGN_TYPE_DATA -> {
                val params = gson.fromJson<List<String>>(request.params)
                if (params.size < 2)
                    throw InvalidJsonRpcRequestException()
                onEthSign(request.id, WCEthereumSignMessage(params, WCEthereumSignMessage.WCSignType.TYPED_MESSAGE))
            }
            WCMethod.ETH_SIGN_TRANSACTION -> {
                val param = gson.fromJson<List<WCEthereumTransaction>>(request.params)
                        .firstOrNull() ?: throw InvalidJsonRpcRequestException()
                onEthTransaction(request.id, param)
            }
            WCMethod.ETH_SEND_TRANSACTION ->{
                val param = gson.fromJson<List<WCEthereumTransaction>>(request.params)
                        .firstOrNull() ?: throw InvalidJsonRpcRequestException()
                onEthTransaction(request.id, param)
            }
            WCMethod.BNB_SIGN -> {
                try {
                    val order = gson.fromJson<List<WCBinanceCancelOrder>>(request.params)
                            .firstOrNull() ?: throw InvalidJsonRpcRequestException()
                    onBnbCancel(request.id, order)
                } catch (e: NoSuchElementException) { }

                try {
                    val order = gson.fromJson<List<WCBinanceTradeOrder>>(request.params)
                            .firstOrNull() ?: throw InvalidJsonRpcRequestException()
                    onBnbTrade(request.id, order)
                } catch (e: NoSuchElementException) {  }

                try {
                    val order = gson.fromJson<List<WCBinanceTransferOrder>>(request.params)
                            .firstOrNull() ?: throw InvalidJsonRpcRequestException()
                    onBnbTransfer(request.id, order)
                } catch (e: NoSuchElementException) { }
            }
            WCMethod.BNB_TRANSACTION_CONFIRM -> {
                val param = gson.fromJson<List<WCBinanceTxConfirmParam>>(request.params)
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

    private fun encryptAndSend(result: String): Boolean {
        val payload = gson.toJson(encrypt(result.toByteArray(Charsets.UTF_8), session.key.hexStringToByteArray()))
        val message = WCSocketMessage(
            topic = peerId ?: session.topic,
            type = MessageType.PUB,
            payload = payload
        )
        return socket?.send(gson.toJson(message)) ?: false
    }


    private fun disconnect(): Boolean {
        return socket?.close(1000, null) ?: false
    }
}

private fun generateId(): Long {
    return Date().time
}