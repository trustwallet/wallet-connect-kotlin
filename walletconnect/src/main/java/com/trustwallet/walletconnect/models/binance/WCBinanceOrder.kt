package com.trustwallet.walletconnect.models.binance

import com.google.gson.annotations.SerializedName

open class WCBinanceOrder<T>(
    @SerializedName("account_number")
    val accountNumber: String,
    @SerializedName("chain_id")
    val chainId: String,
    val data: String?,
    val memo: String?,
    val sequence: String,
    val source: String,
    val msgs: List<T>
)

data class WCBinanceTxConfirmParam(
    val ok: Boolean,
    val errorMsg: String?
)
