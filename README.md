# WalletConnect

[![](https://jitpack.io/v/trustwallet/wallet-connect-kotlin.svg)](https://jitpack.io/#trustwallet/wallet-connect-kotlin)

[WalletConnect](https://walletconnect.org/) Kotlin SDK, implements 1.0.0 websocket based protocol.

## Demo
<img src="docs/demo.gif" width="250">

## Features

- [x] Connect and disconnect
- [x] Approve / Reject / Kill session
- [x] Approve and reject `eth_sign` / `personal_sign` / `eth_signTypedData`
- [x] Approve and reject `eth_signTransaction` / `eth_sendTransaction`
- [x] Approve and reject `bnb_sign` (binance dex orders)
- [x] session persistent / recovery

## Installation

Add it in your root build.gradle at the end of repositories:
```gradle
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```
Add the following line to your app's build.gradle:

```gradle
dependencies {
    implementation "com.github.TrustWallet:wallet-connect-kotlin:$wc_version"
}
```

## Usage

parse session from scanned QR code:

```kotlin
val peerMeta = WCPeerMeta(name = "App name", url = "https://website.com")
val string = "wc:..."
val session = WCSession.from(string) ?: throw InvalidSessionError // invalid session
// handle session
wcClient.connect(wcSession, peerMeta)
```

configure and handle incoming message:

```kotlin
val wcClient = WCClient(GsonBuilder(), okHttpClient)

wcClient.onDisconnect = { _, _ -> 
    onDisconnect() 
}

wcClient.onSessionRequest = { _, peer -> 
    // ask for user consent
}

wcClient.onDisconnect = { _, _ -> 
    // handle disconnect
}
wcClient.onFailure = { t -> 
    // handle failure
}
wcClient.onGetAccounts = { id -> 
    // handle get_accounts
}

wcClient.onEthSign = { id, message -> 
    // handle eth_sign, personal_sign, eth_signTypedData
}
wcClient.onEthSignTransaction = { id, transaction -> 
    // handle eth_signTransaction
}
wcClient.onEthSendTransaction = { id, transaction -> 
    // handle eth_sendTransaction
}

wcClient.onSignTransaction = { id, transaction -> 
    // handle bnb_sign
}
```

approve session

```kotlin
wcClient.approveSession(listOf(address), chainId)
```

approve request

```kotlin
wcClient.approveRequest(id, signResult) // hex formatted sign
```

disconnect

```kotlin
if (wcClient.session != null) {
    wcClient.killSession()
} else {
    wcClient.disconnect()
}
```

## License

WalletConnect is available under the MIT license. See the LICENSE file for more info.
