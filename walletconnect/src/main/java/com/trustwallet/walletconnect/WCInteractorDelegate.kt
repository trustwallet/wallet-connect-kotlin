package com.trustwallet.walletconnect

import com.trustwallet.walletconnect.models.WCPeerMeta
import com.trustwallet.walletconnect.models.ethereum.WCEthereumSignPayload
import com.trustwallet.walletconnect.models.ethereum.WCEthereumTransaction

interface WCInteractorDelegate {
    fun onFailure(t: Throwable)

    fun onDisconnect(code: Int, reason: String)

    fun onSessionRequest(id: Long, peer: WCPeerMeta)

    fun onEthSign(id: Long, payload: WCEthereumSignPayload)

    fun onEthTransaction(id: Long, transaction: WCEthereumTransaction)

    fun onCustomRequest(id: Long, payload: String)
}