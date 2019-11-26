package com.trustwallet.walletconnect

import android.content.Context
import android.os.Build
import com.trustwallet.walletconnect.models.WCPeerMeta
import com.trustwallet.walletconnect.models.session.WCSession
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.M])
class WCSessionStoreTests {
    private val context = RuntimeEnvironment.systemContext
    private val sharedPreferences = context.getSharedPreferences("tests", Context.MODE_PRIVATE)
    private val storage = WCSessionStore(sharedPreferences)

    companion object {
        const val SESSION_KEY = "org.walletconnect.session"
    }

    @Before
    fun before() {
        sharedPreferences.edit().clear().commit()
    }

    @Test
    fun test_store() {
        val topic = "topic_1"
        val session = WCSession.from("wc:$topic@1?bridge=https%3A%2F%2Fbridge.walletconnect.org&key=some_key")!!
        val item = WCSessionStoreItem(session, "peerId", WCPeerMeta(name = "Some DApp", url = "https://dapp.com"))

        storage.session = item
        Assert.assertNotNull(sharedPreferences.getString(SESSION_KEY, null))
    }

    @Test
    fun test_remove() {
        val topic = "topic_1"
        val session = WCSession.from("wc:$topic@1?bridge=https%3A%2F%2Fbridge.walletconnect.org&key=some_key")!!
        val item = WCSessionStoreItem(session, "peerId", WCPeerMeta(name = "Some DApp", url = "https://dapp.com"))

        storage.session = item
        storage.session = null
        Assert.assertFalse(sharedPreferences.contains(SESSION_KEY))
    }
}