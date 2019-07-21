package com.trustwallet.walletconnect.models.ethereum

data class WCEthereumTransaction(
    val from: String,
    val to: String?,
    val nonce: String?,
    val gasPrice: String?,
    val gasLimit: String?,
    val value: String?,
    val data: String
)