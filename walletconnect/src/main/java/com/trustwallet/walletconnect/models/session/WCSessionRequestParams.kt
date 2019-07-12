package com.trustwallet.walletconnect.models.session

import com.trustwallet.walletconnect.models.WCPeerMeta

data class WCSessionRequestParams(
    val peerId: String,
    val peerMeta: WCPeerMeta,
    val chainId: String?
)