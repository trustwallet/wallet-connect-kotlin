package com.trustwallet.walletconnect.models.binance

import com.google.gson.JsonArray
import com.google.gson.JsonObject

class WCBinanceTransferOrder(
        account_number: String,
        chain_id: String,
        data: String?,
        memo: String?,
        sequence: String,
        source: String,
        msgs: List<Message>): WCBinanceOrder<WCBinanceTransferOrder.Message>(account_number, chain_id, data, memo, sequence, source, msgs) {
    class Message(
            val inputs: List<Item>,
            val outputs: List<Item> ): WCBinanceData {

        class Item(
                val address: String,
                val coins: List<Coin>): WCBinanceData {

            class Coin(
                    val amount: Long,
                    val denom: String): WCBinanceData {

                override fun encode(): String {
                    val jsonObject = JsonObject()
                    jsonObject.addProperty("amount", amount)
                    jsonObject.addProperty("denom", denom)
                    return jsonObject.toString()
                }
            }

            override fun encode(): String {
                val jsonObject = JsonObject()
                jsonObject.addProperty("address", address)
                val coins = JsonArray()
                for (item in this.coins) {
                    coins.add(item.encode())
                }
                jsonObject.add("coins", coins)
                return jsonObject.toString()
            }
        }

        override fun encode(): String {
            val jsonObject = JsonObject()
            val inputs = JsonArray()
            for (item in this.inputs) {
                inputs.add(item.encode())
            }
            jsonObject.add("inputs", inputs)
            val outputs = JsonArray()
            for (item in this.outputs) {
                outputs.add(item.encode())
            }
            jsonObject.add("outputs", outputs)
            return jsonObject.toString()
        }
    }
}