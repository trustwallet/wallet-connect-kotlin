package com.trustwallet.walletconnect.models

import com.squareup.moshi.Json

enum class MessageType {
    @Json(name="pub") PUB,
    @Json(name="sub") SUB
}