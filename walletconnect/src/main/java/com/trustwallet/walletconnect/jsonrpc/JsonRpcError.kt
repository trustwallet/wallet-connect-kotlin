package com.trustwallet.walletconnect.jsonrpc

data class JsonRpcError (
    val code: Int,
    val message: String
)