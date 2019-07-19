package com.trustwallet.walletconnect.models.binance

import org.junit.Assert.*
import org.junit.Test

class WCBinanceTradePairTests {
    @Test
    fun test_parse() {
        val symbol = "BNB_ETH.B-261"
        val pair = WCBinanceTradePair.from(symbol)

        assertEquals(pair?.from, "BNB")
        assertEquals(pair?.to, "ETH.B")

        val symbol2 = "000-0E1_BNB"
        val pair2 = WCBinanceTradePair.from(symbol2)

        assertEquals(pair2?.from, "000")
        assertEquals(pair2?.to, "BNB")

        val symbol3 = "CRYPRICE-150_BTC.B-918"
        val pair3 = WCBinanceTradePair.from(symbol3)

        assertEquals(pair3?.from, "CRYPRICE")
        assertEquals(pair3?.to, "BTC.B")
    }
}