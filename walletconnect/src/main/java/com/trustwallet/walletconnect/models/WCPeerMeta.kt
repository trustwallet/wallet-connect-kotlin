package com.trustwallet.walletconnect.models

data class WCPeerMeta (
    val name: String,
    val url: String,
    val description: String,
    val icons: Array<String>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WCPeerMeta

        if (name != other.name) return false
        if (url != other.url) return false
        if (description != other.description) return false
        if (!icons.contentEquals(other.icons)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + url.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + icons.contentHashCode()
        return result
    }
}