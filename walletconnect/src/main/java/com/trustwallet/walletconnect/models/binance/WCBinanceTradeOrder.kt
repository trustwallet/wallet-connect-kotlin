package com.trustwallet.walletconnect.models.binance

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonDeserializer

class WCBinanceTradeOrder(
    account_number: String,
    chain_id: String,
    data: String?,
    memo: String?,
    sequence: String,
    source: String,
    msgs: List<Message>
) : WCBinanceOrder<WCBinanceTradeOrder.Message> (account_number, chain_id, data, memo, sequence, source, msgs) {

    enum class MessageKey(val key: String) {
        ID("id"),
        ORDER_TYPE("ordertype"),
        PRICE("price"),
        QUANTITY("quantity"),
        SENDER("sender"),
        SIDE("side"),
        SYMBOL("symbol"),
        TIME_INFORCE("timeinforce")
    }

    data class Message(
        val id: String,
        val ordertype: Int,
        val price: Long,
        val quantity: Long,
        val sender: String,
        val side: Int,
        val symbol: String,
        val timeinforce: Int
    )
}

val tradeOrderDeserializer: JsonDeserializer<WCBinanceTradeOrder.Message> = jsonDeserializer {
    WCBinanceTradeOrder.Message(
        id = it.json[WCBinanceTradeOrder.MessageKey.ID.key].string,
        ordertype = it.json[WCBinanceTradeOrder.MessageKey.ORDER_TYPE.key].int,
        price = it.json[WCBinanceTradeOrder.MessageKey.PRICE.key].long,
        quantity = it.json[WCBinanceTradeOrder.MessageKey.QUANTITY.key].long,
        sender = it.json[WCBinanceTradeOrder.MessageKey.SENDER.key].string,
        side = it.json[WCBinanceTradeOrder.MessageKey.SIDE.key].int,
        symbol = it.json[WCBinanceTradeOrder.MessageKey.SYMBOL.key].string,
        timeinforce = it.json[WCBinanceTradeOrder.MessageKey.TIME_INFORCE.key].int
    )
}