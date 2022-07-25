package com.trustwallet.walletconnect.models.session

import com.google.gson.annotations.SerializedName

data class WCChangeNetwork(
    @SerializedName("chainId")
    val chainIdHex: String
)