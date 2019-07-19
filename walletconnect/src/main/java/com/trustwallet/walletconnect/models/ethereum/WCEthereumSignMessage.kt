package com.trustwallet.walletconnect.models.ethereum

data class WCEthereumSignMessage (
    val raw: List<String>,
    val type: WCMessageType
) {
    enum class WCMessageType {
        MESSAGE, PERSONAL_MESSAGE, TYPED_MESSAGE
    }

    val data get() = when (type) {
        WCMessageType.MESSAGE -> raw[1]
        WCMessageType.PERSONAL_MESSAGE -> raw[0]
        WCMessageType.TYPED_MESSAGE -> raw[0]
    }
}