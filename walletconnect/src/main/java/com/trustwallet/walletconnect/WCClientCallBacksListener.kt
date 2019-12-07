package com.trustwallet.walletconnect

import com.trustwallet.walletconnect.models.WCPeerMeta
import com.trustwallet.walletconnect.models.WCSignTransaction
import com.trustwallet.walletconnect.models.binance.WCBinanceCancelOrder
import com.trustwallet.walletconnect.models.binance.WCBinanceTradeOrder
import com.trustwallet.walletconnect.models.binance.WCBinanceTransferOrder
import com.trustwallet.walletconnect.models.binance.WCBinanceTxConfirmParam
import com.trustwallet.walletconnect.models.ethereum.WCEthereumSignMessage
import com.trustwallet.walletconnect.models.ethereum.WCEthereumTransaction

class WCClientCallBacksListener {
  var onFailure: (topic: String, Throwable) -> Unit = { _, _ -> Unit }
  var onDisconnect: (topic: String, code: Int, reason: String) -> Unit = { _, _, _ -> Unit }
  var onSessionRequest: (topic: String, id: Long, peer: WCPeerMeta) -> Unit = { _, _, _ -> Unit }
  var onEthSign: (topic: String, id: Long, message: WCEthereumSignMessage) -> Unit =
    { _, _, _ -> Unit }
  var onEthSignTransaction: (topic: String, id: Long, transaction: WCEthereumTransaction) -> Unit =
    { _, _, _ -> Unit }
  var onEthSendTransaction: (topic: String, id: Long, transaction: WCEthereumTransaction) -> Unit =
    { _, _, _ -> Unit }
  var onCustomRequest: (topic: String, id: Long, payload: String) -> Unit = { _, _, _ -> Unit }
  var onBnbTrade: (topic: String, id: Long, order: WCBinanceTradeOrder) -> Unit =
    { _, _, _ -> Unit }
  var onBnbCancel: (topic: String, id: Long, order: WCBinanceCancelOrder) -> Unit =
    { _, _, _ -> Unit }
  var onBnbTransfer: (topic: String, id: Long, order: WCBinanceTransferOrder) -> Unit =
    { _, _, _ -> Unit }
  var onBnbTxConfirm: (topic: String, id: Long, order: WCBinanceTxConfirmParam) -> Unit =
    { _, _, _ -> Unit }
  var onGetAccounts: (topic: String, id: Long) -> Unit = { _, _ -> Unit }
  var onSignTransaction: (topic: String, id: Long, transaction: WCSignTransaction) -> Unit =
    { _, _, _ -> Unit }
}
