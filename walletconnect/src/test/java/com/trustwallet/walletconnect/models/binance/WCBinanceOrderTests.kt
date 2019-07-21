package com.trustwallet.walletconnect.models.binance

import com.github.salomonbrys.kotson.*
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.trustwallet.walletconnect.jsonrpc.JsonRpcRequest
import org.junit.Test
import org.junit.Assert.*
import java.util.NoSuchElementException

class WCBinanceOrderTests {
    val gson = GsonBuilder()
            .registerTypeAdapter(cancelOrderDeserializer)
            .registerTypeAdapter(tradeOrderDeserializer)
            .registerTypeAdapter(transferOrderDeserializer)
            .create()

    @Test
    fun test_parseCancelOrder() {
        val json = """
            {
              "id": 1,
              "jsonrpc": "2.0",
              "method": "bnb_sign",
              "params": [
                {
                  "account_number": "29",
                  "chain_id": "Binance-Chain-Tigris",
                  "data": null,
                  "memo": "",
                  "msgs": [
                    {
                      "refid": "33BBF307B98146F13D20693CF946C2D77A4CAF28-300",
                      "sender": "bnb1xwalxpaes9r0z0fqdy70j3kz6aayetegur38gl",
                      "symbol": "PVT-554_BNB"
                    }
                  ],
                  "sequence": "300",
                  "source": "1"
                }
              ]
            }"""


        val request = gson.fromJson<JsonRpcRequest<JsonArray>>(json)
        val cancelOrder = gson.fromJson<List<WCBinanceCancelOrder>>(request.params).first()
        assertNotNull(cancelOrder)
    }

    @Test
    fun test_parseTradeOrder() {
        val json = """
            {
              "id": 1,
              "jsonrpc": "2.0",
              "method": "bnb_sign",
              "params": [
                {
                  "account_number": "29",
                  "chain_id": "Binance-Chain-Tigris",
                  "data": null,
                  "memo": "",
                  "msgs": [
                    {
                      "id": "33BBF307B98146F13D20693CF946C2D77A4CAF28-300",
                      "ordertype": 2,
                      "price": 7800,
                      "quantity": 10000000000,
                      "sender": "bnb1xwalxpaes9r0z0fqdy70j3kz6aayetegur38gl",
                      "side": 1,
                      "symbol": "PVT-554_BNB",
                      "timeinforce": 1
                    }
                  ],
                  "sequence": "299",
                  "source": "1"
                }
              ]
            }"""


        val request = gson.fromJson<JsonRpcRequest<JsonArray>>(json)
        val cancelOrder = gson.fromJson<List<WCBinanceTradeOrder>>(request.params).first()
        assertNotNull(cancelOrder)
    }

    @Test
    fun test_parseTransferOrder() {
        val json = """
            {
              "id": 1,
              "jsonrpc": "2.0",
              "method": "bnb_sign",
              "params": [
                {
                  "account_number": "29",
                  "chain_id": "Binance-Chain-Tigris",
                  "data": null,
                  "memo": "Testing",
                  "msgs": [
                    {
                      "inputs": [
                        {
                          "address": "bnb1xwalxpaes9r0z0fqdy70j3kz6aayetegur38gl",
                          "coins": [
                            {
                              "amount": 1000000,
                              "denom": "BNB"
                            }
                          ]
                        }
                      ],
                      "outputs": [
                        {
                          "address": "bnb14u7newkxwdhcuhddvtg2n8n96m9tqxejsjuuhn",
                          "coins": [
                            {
                              "amount": 1000000,
                              "denom": "BNB"
                            }
                          ]
                        }
                      ]
                    }
                  ],
                  "sequence": "301",
                  "source": "1"
                }
              ]
            }
            """

        val request = gson.fromJson<JsonRpcRequest<JsonArray>>(json)
        val order = gson.fromJson<List<WCBinanceTransferOrder>>(request.params).first()
        assertNotNull(order)
    }

    @Test(expected = NoSuchElementException::class)
    fun test_parseInvalidTradeOrder() {
        val json = """
            {
              "id": 1,
              "jsonrpc": "2.0",
              "method": "bnb_sign",
              "params": [
                {
                  "account_number": "29",
                  "chain_id": "Binance-Chain-Tigris",
                  "data": null,
                  "memo": "",
                  "msgs": [
                    {
                      "refid": "33BBF307B98146F13D20693CF946C2D77A4CAF28-300",
                      "sender": "bnb1xwalxpaes9r0z0fqdy70j3kz6aayetegur38gl",
                      "symbol": "PVT-554_BNB"
                    }
                  ],
                  "sequence": "300",
                  "source": "1"
                }
              ]
            }"""


        val request = gson.fromJson<JsonRpcRequest<JsonArray>>(json)
        gson.fromJson<List<WCBinanceTradeOrder>>(request.params).first()
    }
}