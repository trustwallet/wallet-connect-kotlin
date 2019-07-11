package com.trustwallet.walletconnect.models

import android.os.Build
import com.trustwallet.walletconnect.models.session.WCSession
import org.junit.Test

import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class WCSessionTests {

    @Test
    fun test_from() {
        val uri = "wc:217374f6-8735-472d-a743-23bd7d26d106@1?bridge=https%3A%2F%2Fbridge.walletconnect.org&key=d565a3e6cc792fa789bbea26b3f257fb436cfba2de48d2490b3e0248168d4b6b"
        val session = WCSession.from(uri)

        assertEquals("217374f6-8735-472d-a743-23bd7d26d106", session?.topic)
        assertEquals("1", session?.version)
        assertEquals("https://bridge.walletconnect.org", session?.bridge)
        assertEquals("d565a3e6cc792fa789bbea26b3f257fb436cfba2de48d2490b3e0248168d4b6b", session?.key)
    }
}