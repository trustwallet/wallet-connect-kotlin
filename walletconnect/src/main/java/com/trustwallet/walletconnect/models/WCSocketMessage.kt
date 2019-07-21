package com.trustwallet.walletconnect.models

data class WCSocketMessage(
    val topic: String,
    val type: MessageType,
    val payload: String
)