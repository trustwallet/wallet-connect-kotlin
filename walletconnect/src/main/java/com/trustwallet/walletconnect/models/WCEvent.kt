package com.trustwallet.walletconnect.models

import com.squareup.moshi.Json

enum class WCEvent {
    @Json(name = "wc_sessionRequest") SESSION_REQUEST,
    @Json(name = "wc_sessionUpdate") SESSION_UPDATE,
    @Json(name = "eth_sign") ETH_SIGN,
    @Json(name = "personal_sign") ETH_PERSONAL_SIGN,
    @Json(name = "eth_signTypedData") ETH_SIGN_TYPE_DATA,
    @Json(name = "eth_signTransaction") ETH_SIGN_TRANSACTION,
    @Json(name = "eth_sendTransaction") ETH_SEND_TRANSACTION,
}