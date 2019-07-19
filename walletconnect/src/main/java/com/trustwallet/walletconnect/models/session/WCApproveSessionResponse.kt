package com.trustwallet.walletconnect.models.session

import com.trustwallet.walletconnect.models.WCPeerMeta

data class WCApproveSessionResponse(
    val approved: Boolean = true,
    val chainId: Int,
    val accounts: List<String>,
    val peerId: String?,
    val peerMeta: WCPeerMeta?
)