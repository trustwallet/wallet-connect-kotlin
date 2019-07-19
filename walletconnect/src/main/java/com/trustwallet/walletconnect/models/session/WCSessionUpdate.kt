package com.trustwallet.walletconnect.models.session

data class WCSessionUpdate(
    val approved: Boolean,
    val chainId: String?,
    val accounts: List<String>?
)