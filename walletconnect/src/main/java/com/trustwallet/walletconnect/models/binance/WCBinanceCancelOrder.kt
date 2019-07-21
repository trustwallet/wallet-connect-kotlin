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

    enum class MessageKey(val key: String) {
        REFID("refid"),
        SENDER("sender"),
        SYMBOL("symbol")
    }

    data class Message(
        val refid: String,
        val sender: String,
        val symbol: String
    )
}

val cancelOrderDeserializer: JsonDeserializer<WCBinanceCancelOrder.Message> = jsonDeserializer {
    WCBinanceCancelOrder.Message(
        refid = it.json[WCBinanceCancelOrder.MessageKey.REFID.key].string,
        sender = it.json[WCBinanceCancelOrder.MessageKey.SENDER.key].string,
        symbol = it.json[WCBinanceCancelOrder.MessageKey.SYMBOL.key].string
    )
}