package com.trustwallet.walletconnect.models.binance

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonDeserializer

class WCBinanceCancelOrder(
    account_number: String,
    chain_id: String,
    data: String?,
    memo: String?,
    sequence: String,
    source: String,
    msgs: List<Message>
): WCBinanceOrder<WCBinanceCancelOrder.Message>(account_number, chain_id, data, memo, sequence, source, msgs) {
    class Message(
        val refid: String,
        val sender: String,
        val symbol: String
    ): WCBinanceData
}

val cancelOrderDeserializer: JsonDeserializer<WCBinanceCancelOrder.Message> = jsonDeserializer {
    WCBinanceCancelOrder.Message(
        refid = it.json["refid"].string,
        sender = it.json["sender"].string,
        symbol = it.json["symbol"].string
    )
}