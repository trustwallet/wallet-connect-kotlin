package com.trustwallet.walletconnect.models.binance

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject

class WCBinanceTransferOrder(
    account_number: String,
    chain_id: String,
    data: String?,
    memo: String?,
    sequence: String,
    source: String,
    msgs: List<Message>
): WCBinanceOrder<WCBinanceTransferOrder.Message>(account_number, chain_id, data, memo, sequence, source, msgs) {

    enum class MessageKey(val key: String) {
        INPUTS("inputs"),
        OUTPUTS("outputs")
    }

    data class Message(
        val inputs: List<Item>,
        val outputs: List<Item>
    ) {

        data class Item(
            val address: String,
            val coins: List<Coin>
        ) {

            data class Coin(
                val amount: Long,
                val denom: String
            )
        }
    }
}

val transferOrderDeserializer = jsonDeserializer {
    WCBinanceTransferOrder.Message(
        inputs = it.context.deserialize(it.json[WCBinanceTransferOrder.MessageKey.INPUTS.key].array),
        outputs = it.context.deserialize(it.json[WCBinanceTransferOrder.MessageKey.OUTPUTS.key].array)
    )
}

val transferOrderSerializer = jsonSerializer<WCBinanceTransferOrder.Message> {
    val jsonObject = JsonObject()
    jsonObject.addProperty(WCBinanceTransferOrder.MessageKey.INPUTS.key, it.context.serialize(it.src.inputs))
    jsonObject.addProperty(WCBinanceTransferOrder.MessageKey.OUTPUTS.key, it.context.serialize(it.src.outputs))
    jsonObject
}