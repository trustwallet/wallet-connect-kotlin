package com.trustwallet.walletconnect

import android.content.Context
import android.os.Build
import com.trustwallet.walletconnect.models.WCPeerMeta
import com.trustwallet.walletconnect.models.session.WCSession
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.M])
class WCSessionStorageTests {
    private val context = RuntimeEnvironment.systemContext
    private val sharedPreferences = context.getSharedPreferences("tests", Context.MODE_PRIVATE)
    private val storage = WCSessionStorage(sharedPreferences)

    @Test
    fun test_storeItem() {
        val topic = "topic_1"
        val session = WCSession.from("wc:$topic@1?bridge=https%3A%2F%2Fbridge.walletconnect.org&key=some_key")!!
        val item = WCSessionStoreItem(session, "peerId", WCPeerMeta(name = "Some DApp", url = "https://dapp.com"))

        storage.store(item)
        Assert.assertEquals(storage.allSessions.size, 1)
        Assert.assertEquals(storage.allSessions.getValue(topic).session, item.session)
        Assert.assertNotNull(storage.lastSession)
    }
}