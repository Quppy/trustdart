package africa.ejara.trustdart

import androidx.annotation.NonNull
import org.json.JSONObject

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

import com.google.protobuf.ByteString
import java.util.Base64

import wallet.core.jni.HDWallet
import wallet.core.jni.CoinType
import wallet.core.jni.BitcoinAddress
import wallet.core.java.AnySigner
import wallet.core.jni.Hash
import wallet.core.jni.PrivateKey

import wallet.core.jni.BitcoinScript
import wallet.core.jni.proto.Bitcoin
import wallet.core.jni.proto.Tron
import wallet.core.jni.proto.Ethereum

import android.os.Build
import androidx.annotation.RequiresApi
import kotlin.math.round

/** TrustdartPlugin */
class TrustdartPlugin: FlutterPlugin, MethodCallHandler {

  init {
    System.loadLibrary("TrustWalletCore")
  }

  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel : MethodChannel

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "trustdart")
    channel.setMethodCallHandler(this)
  }

  @RequiresApi(Build.VERSION_CODES.O)
  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    when(call.method) {
      "generateMnemonic" -> {
        val passphrase: String = call.arguments()
        val wallet: HDWallet? = HDWallet(128, passphrase)

        if (wallet != null) {
          result.success(wallet.mnemonic())
        } else {
          result.error("no_wallet",
                  "Could not generate wallet, why?", null)
        }
      }
      "checkMnemonic" -> {
        val mnemonic: String? = call.argument("mnemonic")
        val passphrase: String? = call.argument("passphrase")
        if (mnemonic != null) {
          val wallet: HDWallet? = HDWallet(mnemonic, passphrase)

          if (wallet != null) {
            result.success(true)
          } else {
            result.error("no_wallet",
                    "Could not generate wallet, why?", null)
          }
        } else {
          result.error("arguments_null", "[mnemonic] cannot be null", null)
        }
      }
      "generateAddress" -> {
        val path: String? = call.argument("path")
        val coin: String? = call.argument("coin")
        val mnemonic: String? = call.argument("mnemonic")
        val passphrase: String? = call.argument("passphrase")
        if (path != null && coin != null && mnemonic != null) {
          val wallet: HDWallet? = HDWallet(mnemonic, passphrase)

          if (wallet != null) {
            val address: Map<String, String?>? = generateAddress(wallet, path, coin)
            if (address == null) result.error("address_null", "failed to generate address", null) else result.success(address)
          } else {
            result.error("no_wallet",
                    "Could not generate wallet, why?", null)
          }
        } else {
          result.error("arguments_null", "[path] and [coin] and [mnemonic] cannot be null", null)
        }
      }
      "validateAddress" -> {
        val address: String? = call.argument("address")
        val coin: String? = call.argument("coin")
        if (address != null && coin != null) {
          val isValid: Boolean = validateAddress(coin, address)
          result.success(isValid)
        } else {
          result.error("arguments_null", "$address and $coin cannot be null", null)
        }
      }
      "signTransaction" -> {
        val coin: String? = call.argument("coin")
        val path: String? = call.argument("path")
        val mnemonic: String? = call.argument("mnemonic")
        val passphrase: String? = call.argument("passphrase")
        val privateString: String? = call.argument("privateString")
        val isUseMaxAmount: Boolean? = call.argument("isUseMaxAmount")
        val txData: Map<String, Any>? = call.argument("txData")
        if (txData != null && coin != null && path != null && mnemonic != null) {
          val wallet: HDWallet? = HDWallet(mnemonic, passphrase)

          if (wallet != null) {
            val txHash: String? = signHDTransaction(wallet, coin, path, txData, isUseMaxAmount == true)
            if (txHash == null) result.error("txhash_null", "failed to buid and sign transaction", null) else result.success(txHash)
          } else {
            result.error("no_wallet",
                    "Could not generate wallet, why?", null)
          }
        } else {
          if (txData != null && coin != null && privateString != null) {
            val txHash: String? = signSimpleTransaction(privateString, coin, txData, isUseMaxAmount == true)
            if (txHash == null) result.error("txhash_null", "failed to buid and sign transaction", null) else result.success(txHash)
          } else {
            result.error("arguments_null", "[txData], [coin] and [path] and [mnemonic] cannot be null", null)
          }
        }
      }
      "getPublicKey" -> {
        val path: String? = call.argument("path")
        val coin: String? = call.argument("coin")
        val mnemonic: String? = call.argument("mnemonic")
        val passphrase: String? = call.argument("passphrase")
        if (path != null && coin != null && mnemonic != null) {
          val wallet: HDWallet? = HDWallet(mnemonic, passphrase)

          if (wallet != null) {
            val publicKey: String? = getPublicKey(wallet, coin, path)
            if (publicKey == null) result.error("address_null", "failed to generate address", null) else result.success(publicKey)
          } else {
            result.error("no_wallet",
                    "Could not generate wallet, why?", null)
          }
        } else {
          result.error("arguments_null", "[path] and [coin] and [mnemonic] cannot be null", null)
        }
      }
      "getPrivateKey" -> {
        val path: String? = call.argument("path")
        val coin: String? = call.argument("coin")
        val mnemonic: String? = call.argument("mnemonic")
        val passphrase: String? = call.argument("passphrase")
        if (path != null && coin != null && mnemonic != null) {
          val wallet: HDWallet? = HDWallet(mnemonic, passphrase)

          if (wallet != null) {
            val privateKey: String? = getPrivateKey(wallet, coin, path)
            if (privateKey == null) result.error("address_null", "failed to generate address", null) else result.success(privateKey)
          } else {
            result.error("no_wallet",
                    "Could not generate wallet, why?", null)
          }
        } else {
          result.error("arguments_null", "[path] and [coin] and [mnemonic] cannot be null", null)
        }
      }
      else -> result.notImplemented()
    }
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }

  private fun generateAddress(wallet: HDWallet, path: String, coin: String): Map<String, String?>? {
    return when(coin) {
      "BTC" -> {
        val privateKey = wallet.getKey(CoinType.BITCOIN, path)
        val publicKey = privateKey.getPublicKeySecp256k1(true)
        val address = BitcoinAddress(publicKey, CoinType.BITCOIN.p2pkhPrefix())
        mapOf("legacy" to address.description(), "segwit" to CoinType.BITCOIN.deriveAddress(privateKey))
      }
      "ETH" -> {
        val privateKey = wallet.getKey(CoinType.ETHEREUM, path)
        mapOf("legacy" to CoinType.ETHEREUM.deriveAddress(privateKey))
      }
      "XTZ" -> {
        val privateKey = wallet.getKey(CoinType.TEZOS, path)
        mapOf("legacy" to CoinType.TEZOS.deriveAddress(privateKey))
      }
      "TRX" -> {
        val privateKey = wallet.getKey(CoinType.TRON, path)
        mapOf("legacy" to CoinType.TRON.deriveAddress(privateKey))
      }
      "SOL" -> {
        val privateKey = wallet.getKey(CoinType.SOLANA, path)
        mapOf("legacy" to CoinType.SOLANA.deriveAddress(privateKey))
      }
      else -> null
    }
  }

  private fun validateAddress(coin: String, address: String): Boolean {
    return when(coin) {
      "BTC" -> {
        CoinType.BITCOIN.validate(address)
      }
      "ETH" -> {
        CoinType.ETHEREUM.validate(address)
      }
      "XTZ" -> {
        CoinType.TEZOS.validate(address)
      }
      "TRX" -> {
        CoinType.TRON.validate(address)
      }
      "SOL" -> {
        CoinType.SOLANA.validate(address)
      }
      else -> false
    }
  }

  @RequiresApi(Build.VERSION_CODES.O)
  private fun getPublicKey(wallet: HDWallet, coin: String, path: String): String? {
    return when(coin) {
      "BTC" -> {
        val privateKey = wallet.getKey(CoinType.BITCOIN, path)
        val publicKey = privateKey.getPublicKeySecp256k1(true)
        Base64.getEncoder().encodeToString(publicKey.data())
      }
      "ETH" -> {
        val privateKey = wallet.getKey(CoinType.ETHEREUM, path)
        val publicKey = privateKey.getPublicKeySecp256k1(true)
        Base64.getEncoder().encodeToString(publicKey.data())
      }
      "XTZ" -> {
        val privateKey = wallet.getKey(CoinType.TEZOS, path)
        val publicKey = privateKey.getPublicKeyEd25519()
        Base64.getEncoder().encodeToString(publicKey.data())
      }
      "TRX" -> {
        val privateKey = wallet.getKey(CoinType.TRON, path)
        val publicKey = privateKey.getPublicKeySecp256k1(true)
        Base64.getEncoder().encodeToString(publicKey.data())
      }
      "SOL" -> {
        val privateKey = wallet.getKey(CoinType.SOLANA, path)
        val publicKey = privateKey.getPublicKeyEd25519()
        Base64.getEncoder().encodeToString(publicKey.data())
      }
      else -> null
    }
  }

  @RequiresApi(Build.VERSION_CODES.O)
  private fun getPrivateKey(wallet: HDWallet, coin: String, path: String): String? {
    return when(coin) {
      "BTC" -> {

        val privateKey = wallet.getKey(CoinType.BITCOIN, path)
        Base64.getEncoder().encodeToString(privateKey.data())
      }
      "ETH" -> {
        val privateKey = wallet.getKey(CoinType.ETHEREUM, path)
        Base64.getEncoder().encodeToString(privateKey.data())
      }
      "XTZ" -> {
        val privateKey = wallet.getKey(CoinType.TEZOS, path)
        Base64.getEncoder().encodeToString(privateKey.data())
      }
      "TRX" -> {
        val privateKey = wallet.getKey(CoinType.TRON, path)
        Base64.getEncoder().encodeToString(privateKey.data())
      }
      "SOL" -> {
        val privateKey = wallet.getKey(CoinType.SOLANA, path)
        Base64.getEncoder().encodeToString(privateKey.data())
      }
      else -> null
    }
  }

  private fun signHDTransaction(wallet: HDWallet, coin: String, path: String, txData: Map<String, Any>, isUseMaxAmount: Boolean = false): String? {
    return when(coin) {
      "XTZ" -> {
        signTezosTransaction(wallet, path, txData)
      }
      "ETH" -> {
        signEthereumTransaction(wallet, path, txData)
      }
      "BTC" -> {
        signBitcoinTransaction(wallet, null, CoinType.BITCOIN, txData, isUseMaxAmount)
      }
      "LTC" -> {
        signBitcoinTransaction(wallet, null, CoinType.LITECOIN, txData, isUseMaxAmount)
      }
      "BCH" -> {
        signBitcoinTransaction(wallet, null, CoinType.BITCOINCASH, txData, isUseMaxAmount)
      }
      "TRX" -> {
        signTronTransaction(wallet, path, txData)
      }
      "SOL" -> {
        signSolanaTransaction(wallet, path, txData)
      }
      else -> null
    }
  }

  private fun signSimpleTransaction(privateString: String, coin: String, txData: Map<String, Any>, isUseMaxAmount: Boolean = false): String? {
    return when(coin) {
      "BTC" -> {
        signBitcoinTransaction(null, privateString, CoinType.BITCOIN, txData, isUseMaxAmount)
      }
      "LTC" -> {
        signBitcoinTransaction(null, privateString, CoinType.LITECOIN, txData, isUseMaxAmount)
      }
      "BCH" -> {
        signBitcoinTransaction(null, privateString, CoinType.BITCOINCASH, txData, isUseMaxAmount)
      }
      else -> null
    }
  }

  private fun signTronTransaction(wallet: HDWallet, path: String, txData: Map<String, Any>): String? {
    val cmd = txData["cmd"] as String
    val privateKey = wallet.getKey(CoinType.TRON, path)
    val txHash: String?;
    when(cmd) {
      "TRC20" -> {
        val trc20Contract = Tron.TransferTRC20Contract.newBuilder()
                .setOwnerAddress(txData["ownerAddress"] as String)
                .setContractAddress(txData["contractAddress"] as String)
                .setToAddress(txData["toAddress"] as String)
                .setAmount(ByteString.copyFrom(Numeric.hexStringToByteArray((txData["amount"] as String))))

        val blockHeader = Tron.BlockHeader.newBuilder()
                .setTimestamp(txData["blockTime"] as Long)
                .setTxTrieRoot(ByteString.copyFrom(Numeric.hexStringToByteArray((txData["txTrieRoot"] as String))))
                .setParentHash(ByteString.copyFrom(Numeric.hexStringToByteArray((txData["parentHash"] as String))))
                .setNumber((txData["number"] as Int).toLong())
                .setWitnessAddress(ByteString.copyFrom(Numeric.hexStringToByteArray((txData["witnessAddress"] as String))))
                .setVersion(txData["version"] as Int)
                .build()

        val transaction = Tron.Transaction.newBuilder()
                .setTimestamp(txData["timestamp"] as Long)
                .setTransferTrc20Contract(trc20Contract)
                .setBlockHeader(blockHeader)
                .setFeeLimit((txData["feeLimit"] as Int).toLong())
                .build()

        val signingInput = Tron.SigningInput.newBuilder()
                .setTransaction(transaction)
                .setPrivateKey(ByteString.copyFrom(privateKey.data()))

        val output = AnySigner.sign(signingInput.build(), CoinType.TRON, Tron.SigningOutput.parser())
        txHash = output.json
      }
      "TRC10" -> {
        val trc10Contract = Tron.TransferAssetContract.newBuilder()
                .setOwnerAddress(txData["ownerAddress"] as String)
                .setAssetName(txData["assetName"] as String)
                .setToAddress(txData["toAddress"] as String)
                .setAmount((txData["amount"] as Int).toLong())

        val blockHeader = Tron.BlockHeader.newBuilder()
                .setTimestamp(txData["blockTime"] as Long)
                .setTxTrieRoot(ByteString.copyFrom(Numeric.hexStringToByteArray((txData["txTrieRoot"] as String))))
                .setParentHash(ByteString.copyFrom(Numeric.hexStringToByteArray((txData["parentHash"] as String))))
                .setNumber((txData["number"] as Int).toLong())
                .setWitnessAddress(ByteString.copyFrom(Numeric.hexStringToByteArray((txData["witnessAddress"] as String))))
                .setVersion(txData["version"] as Int)
                .build()

        val transaction = Tron.Transaction.newBuilder()
                .setTimestamp(txData["timestamp"] as Long)
                .setTransferAsset(trc10Contract)
                .setBlockHeader(blockHeader)
                .build()

        val signingInput = Tron.SigningInput.newBuilder()
                .setTransaction(transaction)
                .setPrivateKey(ByteString.copyFrom(privateKey.data()))

        val output = AnySigner.sign(signingInput.build(), CoinType.TRON, Tron.SigningOutput.parser())
        txHash = output.json
      }
      "TRX" -> {
        val transfer = Tron.TransferContract.newBuilder()
                .setOwnerAddress(txData["ownerAddress"] as String)
                .setToAddress(txData["toAddress"] as String)
                .setAmount((txData["amount"]  as Int).toLong())

        val blockHeader = Tron.BlockHeader.newBuilder()
                .setTimestamp(txData["blockTime"] as Long)
                .setTxTrieRoot(ByteString.copyFrom(Numeric.hexStringToByteArray((txData["txTrieRoot"] as String))))
                .setParentHash(ByteString.copyFrom(Numeric.hexStringToByteArray((txData["parentHash"] as String))))
                .setNumber((txData["number"] as Int).toLong())
                .setWitnessAddress(ByteString.copyFrom(Numeric.hexStringToByteArray((txData["witnessAddress"] as String))))
                .setVersion(txData["version"] as Int)
                .build()

        val transaction = Tron.Transaction.newBuilder()
                .setTimestamp(txData["timestamp"] as Long)
                .setTransfer(transfer)
                .setBlockHeader(blockHeader)
                .build()

        val signingInput = Tron.SigningInput.newBuilder()
                .setTransaction(transaction)
                .setPrivateKey(ByteString.copyFrom(privateKey.data()))

        val output = AnySigner.sign(signingInput.build(), CoinType.TRON, Tron.SigningOutput.parser())
        txHash = output.json
      }
      "FREEZE" -> {
        val freezeContract = Tron.FreezeBalanceContract.newBuilder()
                .setOwnerAddress(txData["ownerAddress"] as String)
                .setResource(txData["resource"] as String)
                .setFrozenDuration((txData["frozenDuration"] as Int).toLong())
                .setFrozenBalance((txData["frozenBalance"] as Int).toLong())

        val blockHeader = Tron.BlockHeader.newBuilder()
                .setTimestamp(txData["blockTime"] as Long)
                .setTxTrieRoot(ByteString.copyFrom(Numeric.hexStringToByteArray((txData["txTrieRoot"] as String))))
                .setParentHash(ByteString.copyFrom(Numeric.hexStringToByteArray((txData["parentHash"] as String))))
                .setNumber((txData["number"] as Int).toLong())
                .setWitnessAddress(ByteString.copyFrom(Numeric.hexStringToByteArray((txData["witnessAddress"] as String))))
                .setVersion(txData["version"] as Int)
                .build()

        val transaction = Tron.Transaction.newBuilder()
                .setTimestamp(txData["timestamp"] as Long)
                .setFreezeBalance(freezeContract)
                .setBlockHeader(blockHeader)
                .build()

        val signingInput = Tron.SigningInput.newBuilder()
                .setTransaction(transaction)
                .setPrivateKey(ByteString.copyFrom(privateKey.data()))

        val output = AnySigner.sign(signingInput.build(), CoinType.TRON, Tron.SigningOutput.parser())
        txHash = output.json
      }
      "CONTRACT" -> {
        txHash = null
      }
      else -> txHash = null;
    }
    return txHash;
  }

  private fun signTezosTransaction(wallet: HDWallet, path: String, txData: Map<String, Any>): String? {
    val privateKey = wallet.getKey(CoinType.TEZOS, path)
    val opJson =  JSONObject(txData).toString();
    val result = AnySigner.signJSON(opJson, privateKey.data(), CoinType.TEZOS.value())
    return result
  }

  private fun signEthereumTransaction(wallet: HDWallet, path: String, txData: Map<String, Any>): String? {
    var key = wallet.getKey(CoinType.ETHEREUM, "m/44$path")
    val address: String = CoinType.ETHEREUM.deriveAddress(key)
    if(address.lowercase() != (txData["fromAddress"] as String).lowercase()) {
      key = wallet.getKey(CoinType.ETHEREUM, "m/49$path")
    }
    val signerInput = Ethereum.SigningInput.newBuilder().apply {
          gasPrice = ByteString.copyFrom(Numeric.hexStringToByteArray(txData["gasPrice"] as String))
          gasLimit = ByteString.copyFrom(Numeric.hexStringToByteArray(txData["gasLimit"] as String))
          chainId = ByteString.copyFrom(Numeric.hexStringToByteArray(txData["chainId"] as String))
          nonce = ByteString.copyFrom(Numeric.hexStringToByteArray(txData["nonce"] as String))
          toAddress = txData["toAddress"] as String
          transaction = Ethereum.Transaction.newBuilder().apply {
            transfer = Ethereum.Transaction.Transfer.newBuilder().apply {
                amount = ByteString.copyFrom(Numeric.hexStringToByteArray(txData["amount"] as String))
            }.build()
          }.build()

          privateKey = ByteString.copyFrom(key.data())
      }.build()
    val output = AnySigner.sign(signerInput, CoinType.ETHEREUM, Ethereum.SigningOutput.parser())
    val result = Numeric.toHexString(output.encoded.toByteArray())
    return "0x$result"
  }

  private fun signSolanaTransaction(wallet: HDWallet, path: String, txData: Map<String, Any>): String? {
    val privateKey = wallet.getKey(CoinType.SOLANA, path)
    val opJson =  JSONObject(txData).toString();
    val result = AnySigner.signJSON(opJson, privateKey.data(), CoinType.SOLANA.value())
    return result
  }

  private fun signBitcoinTransaction(wallet: HDWallet?, privateString: String?, coin: CoinType, txData: Map<String, Any>, isUseMaxAmount: Boolean): String? {
    @Suppress("UNCHECKED_CAST")
    val utxos: List<Map<String, Any>> = txData["utxos"] as List<Map<String, Any>>
    var key: PrivateKey

    val input = Bitcoin.SigningInput.newBuilder().apply {
        this.amount = (txData["amount"] as Int).toLong()
        this.hashType = BitcoinScript.hashTypeForCoin(coin)
        this.toAddress = txData["toAddress"] as String
        this.changeAddress = txData["changeAddress"] as String
        this.coinType = coin.value()
        this.useMaxAmount = isUseMaxAmount
        this.byteFee = round((txData["feeRate"] as Int).toDouble()/1000).toLong()
    }

    for (utx in utxos) {
        var script = BitcoinScript(Numeric.hexStringToByteArray(utx["script"] as String))
        if (privateString == null && wallet != null) {
            // for hd wallet
            key = wallet.getKey(coin, utx["path"] as String)
        } else {
            // for simple wallet
            key = PrivateKey(Numeric.hexStringToByteArray(privateString as String))
        }
        if (script.isPayToScriptHash) {
            val pubkey = key.getPublicKeySecp256k1(true)
            val scriptHash = script.matchPayToScriptHash()
            val scriptHashString = Numeric.toHexString(scriptHash)
            val pubkeySha256RIPEMD = Hash.sha256RIPEMD(pubkey.data())
            input.putScripts(scriptHashString, ByteString.copyFrom(BitcoinScript.buildPayToWitnessPubkeyHash(pubkeySha256RIPEMD).data()))
        } else if (script.isPayToWitnessScriptHash) {
        } else if (script.isPayToWitnessPublicKeyHash) {
        } else {
        }
    val txHash = Numeric.hexStringToByteArray(utx["txid"] as String);
        txHash.reverse();
        val outPoint = Bitcoin.OutPoint.newBuilder().apply {
            this.hash = ByteString.copyFrom(txHash)
            this.index = utx["vout"] as Int
            this.sequence = Long.MAX_VALUE.toInt()
        }.build()
        val utxo = Bitcoin.UnspentTransaction.newBuilder().apply {
            this.amount = (utx["value"] as Int).toLong()
            this.outPoint = outPoint
            this.script = ByteString.copyFrom(script.data())
        }.build()
        input.addUtxo(utxo)
        input.addPrivateKey(ByteString.copyFrom(key.data()))
    }

    var output = AnySigner.sign(input.build(), coin, Bitcoin.SigningOutput.parser())

    output = AnySigner.sign(input.build(), coin, Bitcoin.SigningOutput.parser())
    val hexString = Numeric.toHexString(output.encoded.toByteArray())

    return hexString
  }
}
