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
    class Message(
        val id: String,
        val ordertype: Int,
        val price: Long,
        val quantity: Long,
        val sender: String,
        val side: Int,
        val symbol: String,
        val timeinforce: Int
    ): WCBinanceData
}

val tradeOrderDeserializer: JsonDeserializer<WCBinanceTradeOrder.Message> = jsonDeserializer {
    WCBinanceTradeOrder.Message(
        id = it.json["id"].string,
        ordertype = it.json["ordertype"].int,
        price = it.json["price"].long,
        quantity = it.json["quantity"].long,
        sender = it.json["sender"].string,
        side = it.json["side"].int,
        symbol = it.json["symbol"].string,
        timeinforce = it.json["timeinforce"].int
    )
}