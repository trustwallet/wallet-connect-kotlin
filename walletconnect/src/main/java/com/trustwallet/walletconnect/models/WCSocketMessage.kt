package com.trustwallet.walletconnect.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class WCSocketMessage<T>(
    val topic: String,
    val type: MessageType,
    val payload: T
)