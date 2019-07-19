package com.trustwallet.walletconnect.jsonrpc

data class JsonRpcError (
    val code: Int,
    val message: String
) {
    companion object {
        fun serverError(message: String) = JsonRpcError(-32000, message)
        fun invalidParams(message: String) = JsonRpcError(-32602, message)
        fun invalidRequest(message: String) = JsonRpcError(-32600, message)
        fun parseError(message: String) = JsonRpcError(-32700, message)
        fun methodNotFound(message: String) = JsonRpcError(-32601, message)
    }
}