package com.trustwallet.walletconnect.models

data class WCSocketMessage<T>(
    val topic: String,
    val type: MessageType,
    val payload: T
)