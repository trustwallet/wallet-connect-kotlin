package com.trustwallet.walletconnect

import com.trustwallet.walletconnect.models.WCPeerMeta
import com.trustwallet.walletconnect.models.ethereum.WCEthereumSignPayload
import com.trustwallet.walletconnect.models.ethereum.WCEthereumTransaction

interface FailureListener {
    fun onFailure(t: Throwable)
}

interface DisconnectListener {
    fun onDisconnect(code: Int, reason: String)
}

interface SessionRequestListener {
    fun onSessionRequest(id: Long, peer: WCPeerMeta)
}

interface EthSignListener {
    fun onEthSign(id: Long, payload: WCEthereumSignPayload)
}

interface EthTransactionListener {
    fun onEthTransaction(id: Long, transaction: WCEthereumTransaction)
}

interface CustomRequestListener {
    fun onCustomRequest(id: Long, payload: String)
}