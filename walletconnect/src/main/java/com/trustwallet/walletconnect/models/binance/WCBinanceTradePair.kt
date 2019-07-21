package com.trustwallet.walletconnect.models.binance

data class WCBinanceTradePair(val from: String, val to: String) {
    companion object {
        fun from(symbol: String): WCBinanceTradePair? {
            val pair = symbol.split("_")

            return if (pair.size > 1) {
                val firstParts = pair[0].split("-")
                val secondParts = pair[1].split("-")
                WCBinanceTradePair(firstParts[0], secondParts[0])
            } else {
                null
            }
        }
    }
}
