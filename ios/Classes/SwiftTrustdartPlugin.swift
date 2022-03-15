import Flutter
import UIKit
import WalletCore

public class SwiftTrustdartPlugin: NSObject, FlutterPlugin {
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "trustdart", binaryMessenger: registrar.messenger())
    let instance = SwiftTrustdartPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
  }
  
  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    
    switch call.method {
            case "generateMnemonic":
                let passphrase: String = call.arguments as! String
                let wallet = HDWallet(strength: 128, passphrase: passphrase)
                if wallet != nil {
                    result(wallet!.mnemonic)
                } else {
                    result(FlutterError(code: "no_wallet",
                                        message: "Could not generate wallet, why?",
                                        details: nil))
                }
            case "checkMnemonic":
                let args = call.arguments as! [String: String]
                let mnemonic: String? = args["mnemonic"]
                let passphrase: String? = args["passphrase"]
                if mnemonic != nil {
                    let wallet = HDWallet(mnemonic: mnemonic!, passphrase: passphrase!)
                    
                    if wallet != nil {
                        result(true)
                    } else {
                        result(FlutterError(code: "no_wallet",
                                            message: "Could not generate wallet, why?",
                                            details: nil))
                    }
                } else {
                    result(FlutterError(code: "arguments_null",
                                        message: "[mnemonic] cannot be null",
                                        details: nil))
                }
                
            case "generateAddress":
                let args = call.arguments as! [String: String]
                let path: String? = args["path"]
                let coin: String? = args["coin"]
                let mnemonic: String? = args["mnemonic"]
                let passphrase: String? = args["passphrase"]
                if path != nil && coin != nil && mnemonic != nil {
                    let wallet = HDWallet(mnemonic: mnemonic!, passphrase: passphrase!)
                    if wallet != nil {
                        let address: [String: String]? = generateAddress(wallet: wallet!, path: path!, coin: coin!)
                        if address == nil {
                            result(FlutterError(code: "address_null",
                                                message: "Failed to generate address",
                                                details: nil))
                        } else {
                            result(address)
                        }
                    } else {
                        result(FlutterError(code: "no_wallet",
                                            message: "Could not generate wallet, why?",
                                            details: nil))
                    }
                } else {
                    result(FlutterError(code: "arguments_null",
                                        message: "[path] and [coin] and [mnemonic] cannot be null",
                                        details: nil))
                }
            case "validateAddress":
                let args = call.arguments as! [String: String]
                let address: String? = args["address"]
                let coin: String? = args["coin"]
                if address != nil && coin != nil {
                    let isValid: Bool = validateAddress(address: address!, coin: coin!)
                    result(isValid)
                } else {
                    result(FlutterError(code: "arguments_null",
                                        message: "[address] and [coin] cannot be null",
                                        details: nil))
                }
            case "signTransaction":
                let args = call.arguments as! [String: Any]
                let coin: String? = args["coin"] as? String
                let path: String? = args["path"] as? String
                let txData: [String: Any]? = args["txData"] as? [String: Any]
                let mnemonic: String? = args["mnemonic"] as? String
                let passphrase: String? = args["passphrase"] as? String
                let privateString: String? = args["privateString"] as? String
                let isUseMaxAmount: Bool? = args["isUseMaxAmount"] as? Bool
                if coin != nil && path != nil && txData != nil && mnemonic != nil {
                    let wallet = HDWallet(mnemonic: mnemonic!, passphrase: passphrase!)
                    
                    if wallet != nil {
                        let txHash: String? = signHDTransaction(wallet: wallet!, coin: coin!, path: path!, txData: txData!, isUseMaxAmount: isUseMaxAmount)
                        if txHash == nil {
                            result(FlutterError(code: "txhash_null",
                                                message: "Failed to buid and sign transaction",
                                                details: nil))
                        } else {
                            result(txHash)
                        }
                    } else {
                        result(FlutterError(code: "no_wallet",
                                            message: "Could not generate wallet, why?",
                                            details: nil))
                    }
                } else {
                    if coin != nil && txData != nil && privateString != nil {
                        let txHash: String? = signSimpleTransaction(privateString: privateString!, coin: coin!, txData: txData!, isUseMaxAmount: isUseMaxAmount)
                        if txHash == nil {
                            result(FlutterError(code: "txhash_null",
                                                message: "Failed to buid and sign transaction",
                                                details: nil))
                        } else {
                            result(txHash)
                        }
                    } else {
                        result(FlutterError(code: "arguments_null",
                                            message: "[coin], [txData] and [path] or [privateString] or [mnemonic] cannot be null",
                                            details: nil))
                    }
                }
            case "getPublicKey":
                let args = call.arguments as! [String: String]
                let path: String? = args["path"]
                let coin: String? = args["coin"]
                let mnemonic: String? = args["mnemonic"]
                let passphrase: String? = args["passphrase"]
                if path != nil && coin != nil && mnemonic != nil {
                    let wallet = HDWallet(mnemonic: mnemonic!, passphrase: passphrase!)
                    
                    if wallet != nil {
                        let publicKey: String? = getPublicKey(wallet: wallet!, path: path!, coin: coin!)
                        if publicKey == nil {
                            result(FlutterError(code: "address_null",
                                                message: "Failed to generate address",
                                                details: nil))
                        } else {
                            result(publicKey)
                        }
                    } else {
                        result(FlutterError(code: "no_wallet",
                                            message: "Could not generate wallet, why?",
                                            details: nil))
                    }
                } else {
                    result(FlutterError(code: "arguments_null",
                                        message: "[path] and [coin] and [mnemonic] cannot be null",
                                        details: nil))
                }
            case "getPrivateKey":
                let args = call.arguments as! [String: String]
                let path: String? = args["path"]
                let coin: String? = args["coin"]
                let mnemonic: String? = args["mnemonic"]
                let passphrase: String? = args["passphrase"]
                if path != nil && coin != nil && mnemonic != nil {
                    let wallet = HDWallet(mnemonic: mnemonic!, passphrase: passphrase!)
                    
                    if wallet != nil {
                        let privateKey: String? = getPrivateKey(wallet: wallet!, path: path!, coin: coin!)
                        if privateKey == nil {
                            result(FlutterError(code: "address_null",
                                                message: "Failed to generate address",
                                                details: nil))
                        } else {
                            result(privateKey)
                        }
                    } else {
                        result(FlutterError(code: "no_wallet",
                                            message: "Could not generate wallet, why?",
                                            details: nil))
                    }
                } else {
                    result(FlutterError(code: "arguments_null",
                                        message: "[path] and [coin] and [mnemonic] cannot be null",
                                        details: nil))
                }
            default:
                result(FlutterMethodNotImplemented)
            }
  }
    
    func generateAddress(wallet: HDWallet, path: String, coin: String) -> [String: String]? {
        var addressMap: [String: String]?
        switch coin {
        case "BTC":
            let privateKey = wallet.getKey(coin: CoinType.bitcoin, derivationPath: path)
            let publicKey = privateKey.getPublicKeySecp256k1(compressed: true)
            let legacyAddress = BitcoinAddress(publicKey: publicKey, prefix: 0)
            let scriptHashAddress = BitcoinAddress(publicKey: publicKey, prefix: 5)
            addressMap = ["legacy": legacyAddress!.description,
                          "segwit": CoinType.bitcoin.deriveAddress(privateKey: privateKey),
                          "p2sh": scriptHashAddress!.description,
            ]
        case "ETH":
            let privateKey = wallet.getKey(coin: CoinType.ethereum, derivationPath: path)
            addressMap = ["legacy": CoinType.ethereum.deriveAddress(privateKey: privateKey)]
        case "XTZ":
            let privateKey = wallet.getKey(coin: CoinType.tezos, derivationPath: path)
            addressMap = ["legacy": CoinType.tezos.deriveAddress(privateKey: privateKey)]
        case "TRX":
            let privateKey = wallet.getKey(coin: CoinType.tron, derivationPath: path)
            addressMap = ["legacy": CoinType.tron.deriveAddress(privateKey: privateKey)]
        case "SOL":
            let privateKey = wallet.getKey(coin: CoinType.solana, derivationPath: path)
            addressMap = ["legacy": CoinType.solana.deriveAddress(privateKey: privateKey)]
        default:
            addressMap = nil
        }
        return addressMap
        
    }
    
    func validateAddress(address: String, coin: String) -> Bool {
        var isValid: Bool
        switch coin {
        case "BTC":
            isValid = CoinType.bitcoin.validate(address: address)
        case "ETH":
            isValid = CoinType.ethereum.validate(address: address)
        case "XTZ":
            isValid = CoinType.tezos.validate(address: address)
        case "TRX":
            isValid = CoinType.tron.validate(address: address)
        case "SOL":
            isValid = CoinType.solana.validate(address: address)
        default:
            isValid = false
        }
        return isValid

    }
    
    func getPublicKey(wallet: HDWallet, path: String, coin: String) -> String? {
        var publicKey: String?
        switch coin {
        case "BTC":
            let privateKey = wallet.getKey(coin: CoinType.bitcoin, derivationPath: path)
            publicKey = privateKey.getPublicKeySecp256k1(compressed: true).data.base64EncodedString()
        case "ETH":
            let privateKey = wallet.getKey(coin: CoinType.ethereum, derivationPath: path)
            publicKey = privateKey.getPublicKeySecp256k1(compressed: true).data.base64EncodedString()
        case "XTZ":
            let privateKey = wallet.getKey(coin: CoinType.tezos, derivationPath: path)
            publicKey = privateKey.getPublicKeyEd25519().data.base64EncodedString()
        case "TRX":
            let privateKey = wallet.getKey(coin: CoinType.tron, derivationPath: path)
            publicKey = privateKey.getPublicKeySecp256k1(compressed: true).data.base64EncodedString()
        case "SOL":
            let privateKey = wallet.getKey(coin: CoinType.solana, derivationPath: path)
            publicKey = privateKey.getPublicKeyEd25519().data.base64EncodedString()
        default:
            publicKey = nil
        }
        return publicKey;
    }
    
    func getPrivateKey(wallet: HDWallet, path: String, coin: String) -> String? {
        var privateKey: String?
        switch coin {
        case "BTC":
            privateKey = wallet.getKey(coin: CoinType.bitcoin, derivationPath: path).data.base64EncodedString()
        case "ETH":
            privateKey = wallet.getKey(coin: CoinType.ethereum, derivationPath: path).data.base64EncodedString()
        case "XTZ":
            privateKey = wallet.getKey(coin: CoinType.tezos, derivationPath: path).data.base64EncodedString()
        case "TRX":
            privateKey = wallet.getKey(coin: CoinType.tron, derivationPath: path).data.base64EncodedString()
        case "SOL":
            privateKey = wallet.getKey(coin: CoinType.solana, derivationPath: path).data.base64EncodedString()
        default:
            privateKey = nil
        }
        return privateKey;
    }
    
    func signHDTransaction(wallet: HDWallet, coin: String, path: String, txData: [String: Any], isUseMaxAmount: Bool?) -> String? {
        var txHash: String?
        switch coin {
        case "BTC":
            txHash = signBitcoinTransaction(wallet: wallet, privateString: nil, coin: .bitcoin, txData: txData, isUseMaxAmount: isUseMaxAmount)
        case "LTC":
            txHash = signBitcoinTransaction(wallet: wallet, privateString: nil, coin: .litecoin, txData: txData, isUseMaxAmount: isUseMaxAmount)
        case "BCH":
            txHash = signBitcoinTransaction(wallet: wallet, privateString: nil, coin: .bitcoinCash, txData: txData, isUseMaxAmount: isUseMaxAmount)
        case "ETH":
            txHash = signEthereumTransaction(wallet: wallet, path: path, txData: txData)
        case "XTZ":
            txHash = signTezosTransaction(wallet: wallet, path: path, txData: txData)
        case "TRX":
            txHash = signTronTransaction(wallet: wallet, path: path, txData: txData)
        case "SOL":
            txHash = signSolanaTransaction(wallet: wallet, path: path, txData: txData)
        default:
            txHash = nil
        }
        return txHash

    }
    
    func signSimpleTransaction(privateString: String, coin: String, txData: [String: Any], isUseMaxAmount: Bool?) -> String? {
        var txHash: String?
        switch coin {
        case "BTC":
            txHash = signBitcoinTransaction(wallet: nil, privateString: privateString, coin: .bitcoin, txData: txData, isUseMaxAmount: isUseMaxAmount)
        case "LTC":
            txHash = signBitcoinTransaction(wallet: nil, privateString: privateString, coin: .litecoin, txData: txData, isUseMaxAmount: isUseMaxAmount)
        case "BCH":
            txHash = signBitcoinTransaction(wallet: nil, privateString: privateString, coin: .bitcoinCash, txData: txData, isUseMaxAmount: isUseMaxAmount)
        default:
            txHash = nil
        }
        return txHash

    }
    
    private func objToJson(from object:Any) -> String? {
        guard let data = try? JSONSerialization.data(withJSONObject: object, options: []) else {
            return nil
        }
        return String(data: data, encoding: String.Encoding.utf8)
    }
    
     func signTezosTransaction(wallet: HDWallet, path: String, txData:  [String: Any]) -> String? {
        let privateKey = wallet.getKey(coin: CoinType.tezos, derivationPath: path)
        let opJson =  objToJson(from:txData)
        let result = AnySigner.signJSON(opJson!, key: privateKey.data, coin: CoinType.tezos)
        return result
      }

    func signEthereumTransaction(wallet: HDWallet, path: String, txData:  [String: Any]) -> String? {
        var privateKey = wallet.getKey(coin: CoinType.ethereum, derivationPath: "m/44" + path)
        let address = CoinType.ethereum.deriveAddress(privateKey: privateKey)
        if address.lowercased() != (txData["fromAddress"] as! String).lowercased() {
            privateKey = wallet.getKey(coin: CoinType.ethereum, derivationPath: "m/49" + path)
        }
        let signerInput = EthereumSigningInput.with {
            $0.gasPrice = Data(hexString: txData["gasPrice"] as! String)!
            $0.gasLimit = Data(hexString: txData["gasLimit"] as! String)!
            $0.chainID = Data(hexString: txData["chainId"] as! String)!
            $0.nonce = Data(hexString: txData["nonce"] as! String)!
            $0.toAddress =  txData["toAddress"] as! String
            $0.transaction = EthereumTransaction.with {
                $0.transfer = EthereumTransaction.Transfer.with {
                    $0.amount = Data(hexString: txData["amount"] as! String)!
                }
            }
            $0.privateKey = privateKey.data
        }
        let output: EthereumSigningOutput = AnySigner.sign(input: signerInput, coin: .ethereum)
        let resultOther = "0x" + output.encoded.hexString;
        return resultOther
      }
    
    func signSolanaTransaction(wallet: HDWallet, path: String, txData:  [String: Any]) -> String? {
        let privateKey = wallet.getKey(coin: CoinType.solana, derivationPath: path)
        let opJson =  objToJson(from:txData)
        let result = AnySigner.signJSON(opJson!, key: privateKey.data, coin: CoinType.solana)
        return result
      }
    
    func signTronTransaction(wallet: HDWallet, path: String, txData:  [String: Any]) -> String? {
       let cmd = txData["cmd"] as! String
        var txHash: String?
        let privateKey = wallet.getKey(coin: CoinType.tron, derivationPath: path)
        switch cmd {
        case "TRC20":
                let contract = TronTransferTRC20Contract.with {
                    $0.ownerAddress = txData["ownerAddress"] as! String
                    $0.toAddress = txData["toAddress"] as! String
                    $0.contractAddress = txData["contractAddress"] as! String
                    $0.amount = Data(hexString: txData["amount"] as! String)!
                }
                
                let input = TronSigningInput.with {
                    $0.transaction = TronTransaction.with {
                        $0.feeLimit = txData["feeLimit"] as! Int64
                        $0.transferTrc20Contract = contract
                        $0.timestamp = txData["timestamp"] as! Int64
                        $0.blockHeader = TronBlockHeader.with {
                            $0.timestamp = txData["blockTime"] as! Int64
                            $0.number = txData["number"] as! Int64
                            $0.version = txData["version"] as! Int32
                            $0.txTrieRoot = Data(hexString: txData["txTrieRoot"] as! String)!
                            $0.parentHash = Data(hexString: txData["parentHash"] as! String)!
                            $0.witnessAddress = Data(hexString: txData["witnessAddress"] as! String)!
                        }
                    }
                    $0.privateKey = privateKey.data
                }
                let output: TronSigningOutput = AnySigner.sign(input: input, coin: CoinType.tron)
                txHash = output.json
        case "TRC10":
            let transferAsset = TronTransferAssetContract.with {
                $0.ownerAddress = txData["ownerAddress"] as! String
                $0.toAddress = txData["toAddress"] as! String
                $0.amount = txData["amount"] as! Int64
                $0.assetName = txData["assetName"] as! String
            }
            let input = TronSigningInput.with {
                $0.transaction = TronTransaction.with {
                    $0.transferAsset = transferAsset
                    $0.timestamp = txData["timestamp"] as! Int64
                    $0.blockHeader = TronBlockHeader.with {
                        $0.timestamp = txData["blockTime"] as! Int64
                        $0.number = txData["number"] as! Int64
                        $0.version = txData["version"] as! Int32
                        $0.txTrieRoot = Data(hexString: txData["txTrieRoot"] as! String)!
                        $0.parentHash = Data(hexString: txData["parentHash"] as! String)!
                        $0.witnessAddress = Data(hexString: txData["witnessAddress"] as! String)!
                    }
                }
                $0.privateKey = privateKey.data
            }
            let output: TronSigningOutput = AnySigner.sign(input: input, coin: CoinType.tron)
            txHash = output.json
        case "TRX":
            let transfer = TronTransferContract.with {
                $0.ownerAddress = txData["ownerAddress"] as! String
                $0.toAddress = txData["toAddress"] as! String
                $0.amount = txData["amount"] as! Int64
            }
            let input = TronSigningInput.with {
                $0.transaction = TronTransaction.with {
                    $0.transfer = transfer
                    $0.timestamp = txData["timestamp"] as! Int64
                    $0.blockHeader = TronBlockHeader.with {
                        $0.timestamp = txData["blockTime"] as! Int64
                        $0.number = txData["number"] as! Int64
                        $0.version = txData["version"] as! Int32
                        $0.txTrieRoot = Data(hexString: txData["txTrieRoot"] as! String)!
                        $0.parentHash = Data(hexString: txData["parentHash"] as! String)!
                        $0.witnessAddress = Data(hexString: txData["witnessAddress"] as! String)!
                    }
                }
                $0.privateKey = privateKey.data
            }
            let output: TronSigningOutput = AnySigner.sign(input: input, coin: CoinType.tron)
            txHash = output.json
        case "CONTRACT":
            txHash = ""
        case "FREEZE":
            let contract = TronFreezeBalanceContract.with {
                $0.frozenBalance = txData["frozenBalance"] as! Int64
                $0.frozenDuration = txData["frozenDuration"] as! Int64
                $0.ownerAddress = txData["ownerAddress"] as! String
                $0.resource = txData["resource"] as! String
            }
            let input = TronSigningInput.with {
                $0.transaction = TronTransaction.with {
                    $0.freezeBalance = contract
                    $0.timestamp = txData["timestamp"] as! Int64
                    $0.blockHeader = TronBlockHeader.with {
                        $0.timestamp = txData["blockTime"] as! Int64
                        $0.number = txData["number"] as! Int64
                        $0.version = txData["version"] as! Int32
                        $0.txTrieRoot = Data(hexString: txData["txTrieRoot"] as! String)!
                        $0.parentHash = Data(hexString: txData["parentHash"] as! String)!
                        $0.witnessAddress = Data(hexString: txData["witnessAddress"] as! String)!
                    }
                }
                $0.privateKey = privateKey.data
            }
            let output: TronSigningOutput = AnySigner.sign(input: input, coin: CoinType.tron)
            txHash = output.json
        default:
            txHash = nil
        }
        return txHash
    }

    func signBitcoinTransaction(wallet: HDWallet?, privateString: String?, coin: CoinType, txData:  [String: Any], isUseMaxAmount: Bool? = false) -> String? {
        let utxos: [[String: Any]] = txData["utxos"] as! [[String: Any]]
        var unspent: [BitcoinUnspentTransaction] = []
        var privateKeys: [Data] = []
        var scripts: [String: Data] = [:]
        var key: PrivateKey
        for utx in utxos {
            let script = BitcoinScript(data: Data(hexString: utx["script"] as! String)!)
            if privateString == nil && wallet != nil {
                // for hd wallet
                key = wallet!.getKey(coin: coin, derivationPath: utx["path"] as! String)
            } else {
                // for simple wallet
                key = PrivateKey(data: Data(hexString: privateString!)!)!
            }
            if script.isPayToScriptHash {
                let pubkey = key.getPublicKeySecp256k1(compressed: false)
                let scriptHash = script.matchPayToScriptHash()!
                scripts[scriptHash.hexString] = BitcoinScript.buildPayToWitnessPubkeyHash(hash: pubkey.bitcoinKeyHash).data
                // if coin == CoinType.litecoin {
                //     let bytesSha256 = Hash.sha256(data: pubkey.data)
                //     let bytesRipemd160 = Hash.ripemd(data: bytesSha256)
                //     scripts[scriptHash.hexString] = BitcoinScript.buildPayToPublicKeyHash(hash: bytesRipemd160).data
                // } else {
                //     scripts[scriptHash.hexString] = BitcoinScript.buildPayToWitnessPubkeyHash(hash: pubkey.bitcoinKeyHash).data
                // }
            } else if script.isPayToWitnessScriptHash {
            } else if script.isPayToWitnessPublicKeyHash {
            } else {
            }
            unspent.append(BitcoinUnspentTransaction.with {
                $0.outPoint.hash = Data.reverse(hexString: utx["txid"] as! String)
                $0.outPoint.index = utx["vout"] as! UInt32
                $0.outPoint.sequence = UINT32_MAX
                $0.amount = utx["value"] as! Int64
                $0.script = script.data
            })
            privateKeys.append(key.data)
        }
        let input: BitcoinSigningInput = BitcoinSigningInput.with {
            $0.hashType = BitcoinScript.hashTypeForCoin(coinType: coin)
            $0.amount = txData["amount"] as! Int64
            $0.toAddress = txData["toAddress"] as! String
            $0.changeAddress = txData["changeAddress"] as! String
            $0.coinType = coin.rawValue
//          $0.byteFee = txData["byteFee"] as! Int64
            $0.privateKey = privateKeys
            $0.scripts = scripts
            $0.useMaxAmount = isUseMaxAmount!
            $0.plan = BitcoinTransactionPlan.with {
                $0.amount = txData["amount"] as! Int64
                $0.fee = txData["fees"] as! Int64
                $0.change = txData["change"] as! Int64
                $0.utxos = unspent
            }
        }
        let output: BitcoinSigningOutput = AnySigner.sign(input: input, coin: coin)
        let hexString: String = output.encoded.hexString
        return hexString
    }
    
}
