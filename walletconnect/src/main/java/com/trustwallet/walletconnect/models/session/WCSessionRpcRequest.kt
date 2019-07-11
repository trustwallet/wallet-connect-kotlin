package com.trustwallet.walletconnect.models.session

import com.squareup.moshi.JsonClass
import com.trustwallet.walletconnect.jsonrpc.JsonRpcRequest

@JsonClass(generateAdapter = true)
class WCSessionRpcRequest(
    id: Int,
    params: WCSessionRequestParams
): JsonRpcRequest<WCSessionRequestParams>(
    id = id,
    method = "wc_sessionRequest",
    params = params
)