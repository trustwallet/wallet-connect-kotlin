package com.trustwallet.walletconnect.models.ethereum

data class WCEthereumSignPayload (
    val data: ByteArray,
    val raw: Array<String>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WCEthereumSignPayload

        if (!data.contentEquals(other.data)) return false
        if (!raw.contentEquals(other.raw)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + raw.contentHashCode()
        return result
    }
}