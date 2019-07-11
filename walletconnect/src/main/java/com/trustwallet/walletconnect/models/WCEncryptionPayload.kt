package com.trustwallet.walletconnect.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class WCEncryptionPayload(
    val data: String,
    val hmac: String,
    val iv: String
)