package com.trustwallet.walletconnect.models

import com.google.gson.annotations.SerializedName

enum class MessageType {
    @SerializedName("pub") PUB,
    @SerializedName("sub") SUB
}