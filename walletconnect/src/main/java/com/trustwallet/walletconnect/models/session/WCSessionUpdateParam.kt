package com.trustwallet.walletconnect.models.session

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class WCSessionUpdateParam(
    val approved: Boolean,
    val chainId: String?,
    val accounts: List<String>?
)