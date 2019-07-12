package com.trustwallet.walletconnect.jsonrpc

import com.trustwallet.walletconnect.JSONRPC_VERSION

data class JsonRpcResponse<T> (
    val jsonrpc: String = JSONRPC_VERSION,
    val id: Int,
    val result: T
)