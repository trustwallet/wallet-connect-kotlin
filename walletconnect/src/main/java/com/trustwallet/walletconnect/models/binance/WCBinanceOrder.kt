package com.trustwallet.walletconnect.models.binance

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject

interface WCBinanceData {
    fun encode(): String
}

open class WCBinanceOrder<T: WCBinanceData>(
        val account_number: String,
        val chain_id: String,
        val data: String?,
        val memo: String?,
        val sequence: String,
        val source: String,
        val msgs: List<T>) : WCBinanceData {

    override fun encode(): String {
        val jsonArray = JsonArray()
        for (item in msgs) {
            val msgObject = Gson().fromJson(item.encode(), JsonObject::class.java)
            jsonArray.add(msgObject)
        }
        val jsonObject = JsonObject()
        jsonObject.addProperty("account_number", account_number)
        jsonObject.addProperty("chain_id", chain_id)
        jsonObject.addProperty("data", data)
        jsonObject.addProperty("memo", memo)
        jsonObject.add("msgs", jsonArray)
        jsonObject.addProperty("sequence", sequence)
        jsonObject.addProperty("source", source)
        return jsonObject.toString()
    }
}

class WCBinanceTxConfirmParam(
        val ok: Boolean,
        val errorMsg: String?
)

class WCBinanceOrderSignature(
        val signature: String,
        val publicKey: String
)
