package com.trustwallet.walletconnect.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class WCPeerMeta (
    val name: String,
    val url: String,
    val description: String,
    val icons: Array<String>
)