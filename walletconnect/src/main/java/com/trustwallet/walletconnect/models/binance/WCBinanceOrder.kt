package com.trustwallet.walletconnect.models.binance

open class WCBinanceOrder<T>(
    val account_number: String,
    val chain_id: String,
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
