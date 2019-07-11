package com.trustwallet.walletconnect.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class WCExchangeKeyParam(
    val peerId: String,
    val peerMeta: WCPeerMeta,
    val nextKey: String
)
