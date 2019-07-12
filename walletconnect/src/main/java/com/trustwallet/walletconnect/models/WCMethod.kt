package com.trustwallet.walletconnect.models

enum class WCMethod(val method: String) {
    SESSION_REQUEST("wc_sessionRequest"),
    SESSION_UPDATE("wc_sessionUpdate"),
    ETH_SIGN("eth_sign"),
    ETH_PERSONAL_SIGN("personal_sign"),
    ETH_SIGN_TYPE_DATA("eth_signTypedData"),
    ETH_SIGN_TRANSACTION("eth_signTransaction"),
    ETH_SEND_TRANSACTION("eth_sendTransaction");

    companion object {
        fun from(method: String): WCMethod? {
            for (m in values()) {
                if (m.method == method) {
                    return m
                }
            }

            return null
        }
    }
}