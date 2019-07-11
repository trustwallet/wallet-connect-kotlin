package com.trustwallet.walletconnect.models.session

import com.squareup.moshi.JsonClass
import com.trustwallet.walletconnect.models.WCPeerMeta

@JsonClass(generateAdapter = true)
class WCSessionRequestParams(
    val peerId: String,
    val peerMeta: WCPeerMeta,
    val chainId: String?
)