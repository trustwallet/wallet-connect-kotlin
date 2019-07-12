package com.trustwallet.walletconnect.models.session

data class WCSessionUpdateParam(
    val approved: Boolean,
    val chainId: String?,
    val accounts: Array<String>?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WCSessionUpdateParam

        if (approved != other.approved) return false
        if (chainId != other.chainId) return false
        if (accounts != null) {
            if (other.accounts == null) return false
            if (!accounts.contentEquals(other.accounts)) return false
        } else if (other.accounts != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = approved.hashCode()
        result = 31 * result + (chainId?.hashCode() ?: 0)
        result = 31 * result + (accounts?.contentHashCode() ?: 0)
        return result
    }
}