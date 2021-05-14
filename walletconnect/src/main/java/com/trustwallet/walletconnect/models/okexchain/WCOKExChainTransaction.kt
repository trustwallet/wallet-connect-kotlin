package com.trustwallet.walletconnect.models.okexchain

data class WCOKExChainTransaction(
    val from: String,
    val to: String?,
    val value: String?,
    val gasLimit: String?,
    val gasPrice: String?,
    val accountNumber: String?,
    val sequenceNumber: String?,
    val symbol: String?,
    val contractAddress: String?,
    val data: String?
)