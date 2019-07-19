package com.trustwallet.walletconnect.jsonrpc

import com.trustwallet.walletconnect.JSONRPC_VERSION

data class JsonRpcRequest<T>(
    val id: Long,
    val jsonrpc: String = JSONRPC_VERSION,
    val method: String,
    val params: T
)