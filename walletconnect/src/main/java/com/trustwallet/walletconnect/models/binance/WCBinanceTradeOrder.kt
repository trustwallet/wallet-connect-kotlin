package com.trustwallet.walletconnect.models.binance

import com.google.gson.JsonObject

class WCBinanceTradeOrder(
        account_number: String,
        chain_id: String,
        data: String?,
        memo: String?,
        sequence: String,
        source: String,
        msgs: List<Message>) : WCBinanceOrder<WCBinanceTradeOrder.Message> (account_number, chain_id, data, memo, sequence, source, msgs) {
    class Message(
            val id: String,
            val ordertype: Int,
            val price: Long,
            val quantity: Long,
            val sender: String,
            val side: Int,
            val symbol: String,
            val timeinforce: Int): WCBinanceData {

        override fun encode(): String {
            val jsonObject = JsonObject()
            jsonObject.addProperty("id", id)
            jsonObject.addProperty("ordertype", ordertype)
            jsonObject.addProperty("price", price)
            jsonObject.addProperty("quantity", quantity)
            jsonObject.addProperty("sender", sender)
            jsonObject.addProperty("side", side)
            jsonObject.addProperty("symbol", symbol)
            jsonObject.addProperty("timeinforce", timeinforce)
            return jsonObject.toString()
        }
    }
}