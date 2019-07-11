package com.trustwallet.walletconnect.jsonrpc

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class JsonRpcError (
    val code: Int,
    val message: String
)