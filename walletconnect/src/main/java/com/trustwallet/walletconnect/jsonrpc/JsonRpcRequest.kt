package com.trustwallet.walletconnect.jsonrpc

import com.trustwallet.walletconnect.JSONRPC_VERSION
import com.trustwallet.walletconnect.models.WCMethod

data class JsonRpcRequest<T>(
    val id: Long,
    val jsonrpc: String = JSONRPC_VERSION,
    val method: String,
    val params: T
) {
    val wcMethod:WCMethod? get() = WCMethod.from(method)
}