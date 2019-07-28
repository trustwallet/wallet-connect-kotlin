package com.trustwallet.walletconnect.models.binance

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject

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
        val orderType: Int,
        val price: Long,
        val quantity: Long,
        val sender: String,
        val side: Int,
        val symbol: String,
        val timeInforce: Int
    )
}

val tradeOrderDeserializer = jsonDeserializer {
    WCBinanceTradeOrder.Message(
        id = it.json[WCBinanceTradeOrder.MessageKey.ID.key].string,
        orderType = it.json[WCBinanceTradeOrder.MessageKey.ORDER_TYPE.key].int,
        price = it.json[WCBinanceTradeOrder.MessageKey.PRICE.key].long,
        quantity = it.json[WCBinanceTradeOrder.MessageKey.QUANTITY.key].long,
        sender = it.json[WCBinanceTradeOrder.MessageKey.SENDER.key].string,
        side = it.json[WCBinanceTradeOrder.MessageKey.SIDE.key].int,
        symbol = it.json[WCBinanceTradeOrder.MessageKey.SYMBOL.key].string,
        timeInforce = it.json[WCBinanceTradeOrder.MessageKey.TIME_INFORCE.key].int
    )
}

val tradeOrderSerializer = jsonSerializer<WCBinanceTradeOrder.Message> {
    val jsonObject = JsonObject()
    jsonObject.addProperty(WCBinanceTradeOrder.MessageKey.ID.key, it.src.id)
    jsonObject.addProperty(WCBinanceTradeOrder.MessageKey.ORDER_TYPE.key, it.src.orderType)
    jsonObject.addProperty(WCBinanceTradeOrder.MessageKey.PRICE.key, it.src.price)
    jsonObject.addProperty(WCBinanceTradeOrder.MessageKey.QUANTITY.key, it.src.quantity)
    jsonObject.addProperty(WCBinanceTradeOrder.MessageKey.SENDER.key, it.src.sender)
    jsonObject.addProperty(WCBinanceTradeOrder.MessageKey.SIDE.key, it.src.side)
    jsonObject.addProperty(WCBinanceTradeOrder.MessageKey.SYMBOL.key, it.src.symbol)
    jsonObject.addProperty(WCBinanceTradeOrder.MessageKey.TIME_INFORCE.key, it.src.timeInforce)

    jsonObject
}