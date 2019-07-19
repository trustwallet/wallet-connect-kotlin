package com.trustwallet.walletconnect.models.session

data class WCSessionUpdateParam(
    val approved: Boolean,
    val chainId: String?,
    val accounts: List<String>?
)