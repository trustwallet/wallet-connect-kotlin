package com.trustwallet.walletconnect.models

data class WCPeerMeta (
    val name: String,
    val url: String,
    val description: String? = null,
    val icons: List<String> = listOf("")
)