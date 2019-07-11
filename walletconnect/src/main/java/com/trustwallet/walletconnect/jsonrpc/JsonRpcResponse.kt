package com.trustwallet.walletconnect.jsonrpc

import com.squareup.moshi.JsonClass
import com.trustwallet.walletconnect.JSONRPC_VERSION

@JsonClass(generateAdapter = true)
class JsonRpcResponse<T> (
    val jsonrpc: String = JSONRPC_VERSION,
    val id: Int,
    val result: T
)