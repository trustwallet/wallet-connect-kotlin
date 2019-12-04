package com.trustwallet.walletconnect.models.session

import android.net.Uri

data class WCSession (
    val topic: String,
    val version: String,
    val bridge: String,
    val key: String
) {
    fun toUri(): String = "wc:${topic}@${version}?bridge=${bridge}&key=${key}"

    companion object {
        fun from(from: String): WCSession? {
            if (!from.startsWith("wc:")) {
                return null
            }

            val uriString = from.replace("wc:", "wc://")
            val uri = Uri.parse(uriString)
            val bridge = uri.getQueryParameter("bridge")
            val key = uri.getQueryParameter("key")
            val topic = uri.userInfo
            val version = uri.host

            if (bridge == null || key == null || topic == null || version == null) {
                return null
            }

            return WCSession(topic, version, bridge, key)
        }
    }
}