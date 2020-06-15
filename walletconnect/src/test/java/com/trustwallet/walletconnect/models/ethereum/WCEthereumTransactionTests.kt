package com.trustwallet.walletconnect.models.ethereum

import com.github.salomonbrys.kotson.fromJson
import com.google.gson.GsonBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WCEthereumTransactionTests {
    private val gson = GsonBuilder()
            .create()

    @Test
    fun test_parseCancelOrder() {
        val jsonString = """
        {
            "from": "0xc36edf48e21cf395b206352a1819de658fd7f988",
            "gas": "0x77fb",
            "gasPrice": "0xb2d05e00",
            "nonce": "0x64",
            "to": "0x00000000000c2e074ec69a0dfb2997ba6c7d2e1e",
            "value": "0x0",
            "data": ""
        }
        """


        val tx = gson.fromJson<WCEthereumTransaction>(jsonString)
        assertEquals(tx.gas, "0x77fb")
        assertNull(tx.gasLimit)
    }

}