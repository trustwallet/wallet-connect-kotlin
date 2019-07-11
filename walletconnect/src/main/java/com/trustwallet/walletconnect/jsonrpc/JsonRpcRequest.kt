package com.trustwallet.walletconnect.jsonrpc

import com.squareup.moshi.JsonClass
import com.trustwallet.walletconnect.JSONRPC_VERSION

@JsonClass(generateAdapter = true)
open class JsonRpcRequest<T>(
    val id: Int,
    val jsonrpc: String = JSONRPC_VERSION,
    val method: String,
    val params: T
)