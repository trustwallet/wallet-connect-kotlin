package com.trustwallet.walletconnect

import android.util.Log
import com.github.salomonbrys.kotson.*
import com.google.gson.*
import com.trustwallet.walletconnect.exceptions.InvalidJsonRpcParamsException
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
const val WS_CLOSE_NORMAL = 1000

class WCInteractor (
    private var session: WCSession,
    private var peerMeta: WCPeerMeta,
    private val client: OkHttpClient,
    builder: GsonBuilder = GsonBuilder()
): WebSocketListener() {
    private val TAG = "WCInteractor"

    private val gson = builder
        .serializeNulls()
        .registerTypeAdapter(cancelOrderSerializer)
        .registerTypeAdapter(cancelOrderDeserializer)
        .registerTypeAdapter(tradeOrderSerializer)
        .registerTypeAdapter(tradeOrderDeserializer)
        .registerTypeAdapter(transferOrderSerializer)
        .registerTypeAdapter(transferOrderDeserializer)
        .create()

    private var socket: WebSocket? = null
    private var peerId = UUID.randomUUID().toString()
    private var remotePeerId: String? = null
    private var handshakeId: Long = -1

    var onFailure: (Throwable) -> Unit = { _ -> Unit}
    var onDisconnect: (code: Int, reason: String) -> Unit = { _, _ -> Unit }
    var onSessionRequest: (id: Long, peer: WCPeerMeta) -> Unit = { _, _ -> Unit }
    var onEthSign: (id: Long, message: WCEthereumSignMessage) -> Unit = { _, _ -> Unit }
    var onEthSignTransaction: (id: Long, transaction: WCEthereumTransaction) -> Unit = { _, _ -> Unit }
    var onEthSendTransaction: (id: Long, transaction: WCEthereumTransaction) -> Unit = { _, _ -> Unit }
    var onCustomRequest: (id: Long, payload: String) -> Unit = { _, _ -> Unit }
    var onBnbTrade: (id: Long, order: WCBinanceTradeOrder) -> Unit = { _, _ -> Unit }
    var onBnbCancel: (id: Long, order: WCBinanceCancelOrder) -> Unit = { _, _ -> Unit }
    var onBnbTransfer: (id: Long, order: WCBinanceTransferOrder) -> Unit = { _, _ -> Unit }
    var onBnbTxConfirm: (id: Long, order: WCBinanceTxConfirmParam) -> Unit = { _, _ -> Unit }
    var onGetAccounts: (id: Long) -> Unit = { _ -> Unit }
    var onSignTransaction: (id: Long, transaction: WCSignTransaction) -> Unit = {_, _ -> Unit }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        Log.d(TAG, "<< websocket opened >>")

        // The Session.topic channel is used to listen session request messages only.
        subscribe(session.topic)
        // The peerId channel is used to listen to all messages sent to this client.
        subscribe(peerId)
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        try {
            Log.d(TAG, "<== message $text")
            val message = gson.fromJson<WCSocketMessage>(text)
            val encrypted = gson.fromJson<WCEncryptionPayload>(message.payload)
            val payload = String(decrypt(encrypted, session.key.hexStringToByteArray()), Charsets.UTF_8)
            Log.d(TAG, "<== decrypted $payload")

            val request = gson.fromJson<JsonRpcRequest<JsonArray>>(payload, typeToken<JsonRpcRequest<JsonArray>>())
            val method = request.method
            if (method != null) {
                handleRequest(request)
            } else {
                onCustomRequest(request.id, payload)
            }
        } catch (e: InvalidJsonRpcParamsException) {
            invalidParams(e.requestId)
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        onFailure(t)
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        Log.d(TAG,"<< websocket closed >>")
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        Log.d(TAG,"<== pong")
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        handshakeId = -1
        remotePeerId = null
        onDisconnect(code, reason)
    }

    fun connect(session: WCSession? = null, peerMeta: WCPeerMeta? = null, peerId: String? = null) {
        if (session != null && peerMeta != null && peerId != null) {
            disconnect()
            this.session = session
            this.peerMeta = peerMeta
            this.peerId = peerId
        }

        val request = Request.Builder()
            .url(this.session.bridge)
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
            peerMeta = peerMeta
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

        val response = JsonRpcErrorResponse(
            id = handshakeId,
            error = JsonRpcError.serverError(
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

    fun rejectRequest(id: Long, message: String = "Reject by the user"): Boolean {
        val response = JsonRpcErrorResponse(
            id = id,
            error = JsonRpcError.serverError(
                message = message
            )
        )
        return encryptAndSend(gson.toJson(response))
    }

    private fun invalidParams(id: Long): Boolean {
        val response = JsonRpcErrorResponse(
            id = id,
            error = JsonRpcError.invalidParams(
                message = "Invalid parameters"
            )
        )

        return encryptAndSend(gson.toJson(response))
    }

    private fun handleRequest(request: JsonRpcRequest<JsonArray>) {
        when (request.method) {
            WCMethod.SESSION_REQUEST -> {
                val param = gson.fromJson<List<WCSessionRequest>>(request.params)
                        .firstOrNull() ?: throw InvalidJsonRpcParamsException(request.id)
                handshakeId = request.id
                remotePeerId = param.peerId
                onSessionRequest(request.id, param.peerMeta)
            }
            WCMethod.SESSION_UPDATE -> {
                val param = gson.fromJson<List<WCSessionUpdate>>(request.params)
                        .firstOrNull() ?: throw InvalidJsonRpcParamsException(request.id)
                if (!param.approved) {
                    disconnect()
                }
            }
            WCMethod.ETH_SIGN -> {
                val params = gson.fromJson<List<String>>(request.params)
                if (params.size < 2)
                    throw InvalidJsonRpcParamsException(request.id)
                onEthSign(request.id, WCEthereumSignMessage(params, WCEthereumSignMessage.WCSignType.MESSAGE))
            }
            WCMethod.ETH_PERSONAL_SIGN -> {
                val params = gson.fromJson<List<String>>(request.params)
                if (params.size < 2)
                    throw InvalidJsonRpcParamsException(request.id)
                onEthSign(request.id, WCEthereumSignMessage(params, WCEthereumSignMessage.WCSignType.PERSONAL_MESSAGE))
            }
            WCMethod.ETH_SIGN_TYPE_DATA -> {
                val params = gson.fromJson<List<String>>(request.params)
                if (params.size < 2)
                    throw InvalidJsonRpcParamsException(request.id)
                onEthSign(request.id, WCEthereumSignMessage(params, WCEthereumSignMessage.WCSignType.TYPED_MESSAGE))
            }
            WCMethod.ETH_SIGN_TRANSACTION -> {
                val param = gson.fromJson<List<WCEthereumTransaction>>(request.params)
                        .firstOrNull() ?: throw InvalidJsonRpcParamsException(request.id)
                onEthSignTransaction(request.id, param)
            }
            WCMethod.ETH_SEND_TRANSACTION ->{
                val param = gson.fromJson<List<WCEthereumTransaction>>(request.params)
                        .firstOrNull() ?: throw InvalidJsonRpcParamsException(request.id)
                onEthSendTransaction(request.id, param)
            }
            WCMethod.BNB_SIGN -> {
                try {
                    val order = gson.fromJson<List<WCBinanceCancelOrder>>(request.params)
                            .firstOrNull() ?: throw InvalidJsonRpcParamsException(request.id)
                    onBnbCancel(request.id, order)
                } catch (e: NoSuchElementException) { }

                try {
                    val order = gson.fromJson<List<WCBinanceTradeOrder>>(request.params)
                            .firstOrNull() ?: throw InvalidJsonRpcParamsException(request.id)
                    onBnbTrade(request.id, order)
                } catch (e: NoSuchElementException) {  }

                try {
                    val order = gson.fromJson<List<WCBinanceTransferOrder>>(request.params)
                            .firstOrNull() ?: throw InvalidJsonRpcParamsException(request.id)
                    onBnbTransfer(request.id, order)
                } catch (e: NoSuchElementException) { }
            }
            WCMethod.BNB_TRANSACTION_CONFIRM -> {
                val param = gson.fromJson<List<WCBinanceTxConfirmParam>>(request.params)
                        .firstOrNull() ?: throw InvalidJsonRpcParamsException(request.id)
                onBnbTxConfirm(request.id, param)
            }
            WCMethod.GET_ACCOUNTS -> {
                onGetAccounts(request.id)
            }
            WCMethod.SIGN_TRANSACTION -> {
                val param = gson.fromJson<List<WCSignTransaction>>(request.params)
                    .firstOrNull() ?: throw InvalidJsonRpcParamsException(request.id)
                onSignTransaction(request.id, param)
            }
        }
    }
    
    private fun subscribe(topic: String): Boolean {
        val message = WCSocketMessage(
            topic = topic,
            type = MessageType.SUB,
            payload = ""
        )
        val json = gson.toJson(message)
        Log.d(TAG,"==> subscribe $json")

        return socket?.send(gson.toJson(message)) ?: false
    }

    private fun encryptAndSend(result: String): Boolean {
        Log.d(TAG,"==> message $result")
        val payload = gson.toJson(encrypt(result.toByteArray(Charsets.UTF_8), session.key.hexStringToByteArray()))
        val message = WCSocketMessage(
            // Once the remotePeerId is defined, all messages must be sent to this channel. The session.topic channel
            // will be used only to respond the session request message.
            topic = remotePeerId ?: session.topic,
            type = MessageType.PUB,
            payload = payload
        )
        val json = gson.toJson(message)
        Log.d(TAG,"==> encrypted $json")

        return socket?.send(json) ?: false
    }


    private fun disconnect(): Boolean {
        return socket?.close(WS_CLOSE_NORMAL, null) ?: false
    }
}

private fun generateId(): Long {
    return Date().time
}