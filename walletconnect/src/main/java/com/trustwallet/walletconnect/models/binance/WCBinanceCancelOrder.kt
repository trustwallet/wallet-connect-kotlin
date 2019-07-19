package com.trustwallet.walletconnect.models.binance

import com.google.gson.JsonObject

class WCBinanceCancelOrder(
        account_number: String,
        chain_id: String,
        data: String?,
        memo: String?,
        sequence: String,
        source: String,
        msgs: List<Message>
): WCBinanceOrder<WCBinanceCancelOrder.Message>(account_number, chain_id, data, memo, sequence, source, msgs) {
    class Message (
            val refid: String,
            val sender: String,
            val symbol: String) : WCBinanceData {

        override fun encode(): String {
            val jsonObject = JsonObject()
            jsonObject.addProperty("refid", refid)
            jsonObject.addProperty("sender", sender)
            jsonObject.addProperty("symbol", symbol)
            return jsonObject.toString()
        }
    }
}