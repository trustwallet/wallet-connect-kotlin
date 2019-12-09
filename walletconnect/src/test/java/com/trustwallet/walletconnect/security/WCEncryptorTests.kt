package com.trustwallet.walletconnect.security

import android.os.Build
import com.trustwallet.walletconnect.extensions.hexStringToByteArray
import com.trustwallet.walletconnect.models.WCEncryptionPayload
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class WCEncryptorTests {
    @Test
    fun test_decrypt() {
        val data = "1b3db3674de082d65455eba0ae61cfe7e681c8ef1132e60c8dbd8e52daf18f4fea42cc76366c83351dab6dca52682ff81f828753f89a21e1cc46587ca51ccd353914ffdd3b0394acfee392be6c22b3db9237d3f717a3777e3577dd70408c089a4c9c85130a68c43b0a8aadb00f1b8a8558798104e67aa4ff027b35d4b989e7fd3988d5dcdd563105767670be735b21c4"
        val hmac = "a33f868e793ca4fcca964bcb64430f65e2f1ca7a779febeaf94c5373d6df48b3"
        val iv = "89ef1d6728bac2f1dcde2ef9330d2bb8"
        val key = "5caa3a74154cee16bd1b570a1330be46e086474ac2f4720530662ef1a469662c".hexStringToByteArray()
        val payload = WCEncryptionPayload(
            data = data,
            iv = iv,
            hmac = hmac
        )

        val decrypted = String(decryptMessage(payload, key), Charsets.UTF_8)
        val expected = "{\"id\":1554098597199736,\"jsonrpc\":\"2.0\",\"method\":\"wc_sessionUpdate\",\"params\":[{\"approved\":false,\"chainId\":null,\"accounts\":null}]}"
        Assert.assertEquals(expected, decrypted)
    }

    @Test
    fun test_encrypt() {
        val expected = "{\"id\":1554098597199736,\"jsonrpc\":\"2.0\",\"method\":\"wc_sessionUpdate\",\"params\":[{\"approved\":false,\"chainId\":null,\"accounts\":null}]}".hexStringToByteArray()
        val key = "5caa3a74154cee16bd1b570a1330be46e086474ac2f4720530662ef1a469662c".hexStringToByteArray()
        val payload = encrypt(data = expected, key = key)
        val decrypted = decryptMessage(payload, key)
        Assert.assertArrayEquals(expected, decrypted)
    }
}