package com.trustwallet.walletconnect.models.binance

interface WCBinanceData

open class WCBinanceOrder<T: WCBinanceData>(
    val account_number: String,
    val chain_id: String,
    val data: String?,
    val memo: String?,
    val sequence: String,
    val source: String,
    val msgs: List<T>
) : WCBinanceData

class WCBinanceTxConfirmParam(
    val ok: Boolean,
    val errorMsg: String?
)

class WCBinanceOrderSignature(
    val signature: String,
    val publicKey: String
)
