# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project
adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- [Sample app](app)
- Update [README.md](README.md)
- `ViewBinding`

### Changed

- Upgrade Kotlin, Gradle, all dependencies
- sourceCompatibility/targetCompatibility Java 8 -> Java 11
- (sample) app module depends on library module directly instead of using Jitpack

### Removed

- jcenter()
- `appcompat-v7` dependency from library
- `kotlin android extensions`

## [1.5.4] - 2021-11-11

### Changed

- Allow chain changes on session approve

## [1.5.3] - 2021-10-06

### Added

- EIP-1559 fields to ETH transaction model

## [1.5.2] - 2021-05-15

### Fixed

- Parse ETH transaction payload params ([#26](https://github.com/trustwallet/wallet-connect-kotlin/issues/26))

## [1.5.1] - 2021-05-14

### Fixed

- Parse ETH transaction payload params ([#26](https://github.com/trustwallet/wallet-connect-kotlin/issues/26))

## [1.5.0] - 2020-09-30

### Added

- Persist chainId

## [1.4.9] - 2020-06-16

### Added

- Decode gas and gasLimit in WCEthereumTransaction

### Changed

- Upgrade Gradle dependencies


[Unreleased]: https://github.com/trustwallet/wallet-connect-kotlin/compare/1.0.0...HEAD

[1.5.4]: https://github.com/trustwallet/wallet-connect-kotlin/compare/1.5.3...1.5.4

[1.5.3]: https://github.com/trustwallet/wallet-connect-kotlin/compare/1.5.2...1.5.3

[1.5.2]: https://github.com/trustwallet/wallet-connect-kotlin/compare/1.5.1...1.5.2

[1.5.1]: https://github.com/trustwallet/wallet-connect-kotlin/compare/1.5.0...1.5.1

[1.5.0]: https://github.com/trustwallet/wallet-connect-kotlin/compare/1.4.9...1.5.0

[1.4.9]: https://github.com/trustwallet/wallet-connect-kotlin/compare/1.4.8...1.4.9