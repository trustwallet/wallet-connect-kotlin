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
class WCSessionStorageTests {
    private val context = RuntimeEnvironment.systemContext
    private val sharedPreferences = context.getSharedPreferences("tests", Context.MODE_PRIVATE)
    private val storage = WCSessionStorage(sharedPreferences)

    @Before
    fun before() {
        sharedPreferences.edit().clear().commit()
    }

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

    @Test
    fun test_getLastSession() {
        val topic1 = "topic_1"
        val session1 = WCSession.from("wc:$topic1@1?bridge=https%3A%2F%2Fbridge.walletconnect.org&key=some_key")!!
        val item1 = WCSessionStoreItem(session1, "peerId", WCPeerMeta(name = "Some DApp", url = "https://dapp.com"))

        val topic2 = "topic_2"
        val session2 = WCSession.from("wc:$topic2@1?bridge=https%3A%2F%2Fbridge.walletconnect.org&key=some_key")!!
        val item2 = WCSessionStoreItem(session2, "peerId", WCPeerMeta(name = "Some DApp", url = "https://dapp.com"))

        storage.store(item1)
        storage.store(item2)
        Assert.assertEquals(storage.allSessions.size, 2)
        Assert.assertEquals(storage.allSessions.getValue(topic1).session, item1.session)
        Assert.assertNotNull(storage.lastSession)
        Assert.assertEquals(storage.lastSession!!.session, item2.session)
    }

    @Test
    fun test_clearSession() {
        val topic1 = "topic_1"
        val session1 = WCSession.from("wc:$topic1@1?bridge=https%3A%2F%2Fbridge.walletconnect.org&key=some_key")!!
        val item1 = WCSessionStoreItem(session1, "peerId", WCPeerMeta(name = "Some DApp", url = "https://dapp.com"))

        val topic2 = "topic_2"
        val session2 = WCSession.from("wc:$topic2@1?bridge=https%3A%2F%2Fbridge.walletconnect.org&key=some_key")!!
        val item2 = WCSessionStoreItem(session2, "peerId", WCPeerMeta(name = "Some DApp", url = "https://dapp.com"))

        storage.store(item1)
        storage.store(item2)
        storage.clear(topic2)
        Assert.assertEquals(storage.allSessions.size, 1)
        Assert.assertNull(storage.allSessions[topic2])
        Assert.assertNotNull(storage.lastSession)
        Assert.assertEquals(storage.lastSession!!.session, session1)
        Assert.assertNotNull(storage.allSessions[topic1])
    }

    @Test
    fun test_clearAll() {
        val topic1 = "topic_1"
        val session1 = WCSession.from("wc:$topic1@1?bridge=https%3A%2F%2Fbridge.walletconnect.org&key=some_key")!!
        val item1 = WCSessionStoreItem(session1, "peerId", WCPeerMeta(name = "Some DApp", url = "https://dapp.com"))

        val topic2 = "topic_2"
        val session2 = WCSession.from("wc:$topic2@1?bridge=https%3A%2F%2Fbridge.walletconnect.org&key=some_key")!!
        val item2 = WCSessionStoreItem(session2, "peerId", WCPeerMeta(name = "Some DApp", url = "https://dapp.com"))

        storage.store(item1)
        storage.store(item2)
        storage.clearAll()
        Assert.assertEquals(storage.allSessions.size, 0)
        Assert.assertNull(storage.lastSession)
    }

    @Test
    fun test_load() {
        val topic = "topic"
        val session = WCSession.from("wc:$topic@1?bridge=https%3A%2F%2Fbridge.walletconnect.org&key=some_key")!!
        val item = WCSessionStoreItem(session, "peerId", WCPeerMeta(name = "Some DApp", url = "https://dapp.com"))

        storage.store(item)
        Assert.assertNotNull(storage.load(topic))
        Assert.assertEquals(storage.load(topic)!!.session, session)
        Assert.assertNull(storage.load("other_topic"))
    }
}