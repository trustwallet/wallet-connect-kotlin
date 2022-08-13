package com.trustwallet.walletconnect.models.session

import com.google.gson.annotations.SerializedName

data class WCAddNetwork(
    @SerializedName("chainId")
    val chainIdHex: String,
    val chainName: String?,
    val rpcUrls: List<String>?,
    val blockExplorerUrls: List<String>?,
    val nativeCurrency: WcAddNetworkNativeCurrency
) {
    data class WcAddNetworkNativeCurrency(
        val symbol: String,
        val name: String,
        val decimals: Int,
    )
}