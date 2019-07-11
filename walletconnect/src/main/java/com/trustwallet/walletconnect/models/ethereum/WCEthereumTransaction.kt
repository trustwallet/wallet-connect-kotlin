package com.trustwallet.walletconnect.models.ethereum

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class WCEthereumTransaction(
    val from: String,
    val to: String?,
    val nonce: String?,
    val gasPrice: String?,
    val gasLimit: String?,
    val value: String?,
    val data: String
)