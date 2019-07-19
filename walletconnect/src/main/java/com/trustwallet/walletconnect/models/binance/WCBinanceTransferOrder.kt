package com.trustwallet.walletconnect.models.binance

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonDeserializer

class WCBinanceTransferOrder(
    account_number: String,
    chain_id: String,
    data: String?,
    memo: String?,
    sequence: String,
    source: String,
    msgs: List<Message>
): WCBinanceOrder<WCBinanceTransferOrder.Message>(account_number, chain_id, data, memo, sequence, source, msgs) {
    class Message(
        val inputs: List<Item>,
        val outputs: List<Item>
    ): WCBinanceData {
        class Item(
            val address: String,
            val coins: List<Coin>
        ): WCBinanceData {
            class Coin(
                val amount: Long,
                val denom: String
            ): WCBinanceData
        }
    }
}

val transferOrderDeserializer: JsonDeserializer<WCBinanceTransferOrder.Message> = jsonDeserializer {
    WCBinanceTransferOrder.Message(
        inputs = it.context.deserialize(it.json["inputs"].array),
        outputs = it.context.deserialize(it.json["outputs"].array)
    )
}