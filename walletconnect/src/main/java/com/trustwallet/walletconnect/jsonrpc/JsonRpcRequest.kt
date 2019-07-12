package com.trustwallet.walletconnect.jsonrpc

import com.trustwallet.walletconnect.JSONRPC_VERSION

data class JsonRpcRequest<T>(
    val id: Long,
    val jsonrpc: String = JSONRPC_VERSION,
    val method: String,
    val params: Array<T>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as JsonRpcRequest<*>

        if (id != other.id) return false
        if (jsonrpc != other.jsonrpc) return false
        if (method != other.method) return false
        if (!params.contentEquals(other.params)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + jsonrpc.hashCode()
        result = 31 * result + method.hashCode()
        result = 31 * result + params.contentHashCode()
        return result
    }
}