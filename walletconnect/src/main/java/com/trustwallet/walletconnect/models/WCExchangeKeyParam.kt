package com.trustwallet.walletconnect.models

data class WCExchangeKeyParam(
    val peerId: String,
    val peerMeta: WCPeerMeta,
    val nextKey: String
)
