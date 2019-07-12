package com.trustwallet.walletconnect.models.session

import com.trustwallet.walletconnect.models.WCPeerMeta

data class WCApproveSessionResponse(
    val approved: Boolean = true,
    val chainId: Int,
    val accounts: Array<String>,
    val peerId: String?,
    val peerMeta: WCPeerMeta?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WCApproveSessionResponse

        if (approved != other.approved) return false
        if (chainId != other.chainId) return false
        if (!accounts.contentEquals(other.accounts)) return false
        if (peerId != other.peerId) return false
        if (peerMeta != other.peerMeta) return false

        return true
    }

    override fun hashCode(): Int {
        var result = approved.hashCode()
        result = 31 * result + chainId
        result = 31 * result + accounts.contentHashCode()
        result = 31 * result + (peerId?.hashCode() ?: 0)
        result = 31 * result + (peerMeta?.hashCode() ?: 0)
        return result
    }
}