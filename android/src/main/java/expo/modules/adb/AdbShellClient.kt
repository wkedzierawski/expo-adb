package expo.modules.adb

import android.content.Context
import android.util.Base64
import com.tananaev.adblib.AdbBase64
import com.tananaev.adblib.AdbConnection
import com.tananaev.adblib.AdbCrypto
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.net.ConnectException
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException
import java.security.spec.InvalidKeySpecException
import java.util.concurrent.TimeUnit

internal object AdbShellClient {
  private const val HOST = "127.0.0.1"
  private const val PORT = 5555
  private const val CONNECT_TIMEOUT_MS = 5_000
  private const val KEY_DIRECTORY = "expo-adb"
  private const val PRIVATE_KEY_FILE = "adbkey"
  private const val PUBLIC_KEY_FILE = "adbkey.pub"

  private val adbBase64 = AdbBase64 { payload ->
    Base64.encodeToString(payload, Base64.NO_WRAP)
  }

  data class AvailabilityDetails(
    val available: Boolean,
    val host: String,
    val port: Int,
    val keyPairPresent: Boolean,
    val errorCode: String? = null,
    val errorClass: String? = null,
    val errorMessage: String? = null,
    val hint: String? = null
  ) {
    fun toMap(): Map<String, Any?> {
      return mapOf(
        "available" to available,
        "host" to host,
        "port" to port,
        "keyPairPresent" to keyPairPresent,
        "errorCode" to errorCode,
        "errorClass" to errorClass,
        "errorMessage" to errorMessage,
        "hint" to hint
      )
    }
  }

  fun isAvailable(context: Context): Boolean {
    return getAvailabilityDetails(context).available
  }

  fun getAvailabilityDetails(context: Context): AvailabilityDetails {
    val keyPairPresent = hasPersistedKeyPair(context)

    return try {
      connect(context).use { connection ->
        AvailabilityDetails(
          available = true,
          host = HOST,
          port = PORT,
          keyPairPresent = keyPairPresent || hasPersistedKeyPair(context)
        )
      }
    } catch (exception: Exception) {
      AvailabilityDetails(
        available = false,
        host = HOST,
        port = PORT,
        keyPairPresent = keyPairPresent || hasPersistedKeyPair(context),
        errorCode = classifyError(exception),
        errorClass = exception::class.java.simpleName,
        errorMessage = exception.message,
        hint = hintForError(exception)
      )
    }
  }

  fun executeCommands(context: Context, commands: List<String>): String {
    require(commands.isNotEmpty()) {
      "ADB command list must not be empty"
    }

    val normalizedCommands = commands.map { command ->
      command.trim().takeIf { it.isNotEmpty() }
        ?: throw IllegalArgumentException("ADB commands must not be blank")
    }

    val script = buildString {
      normalizedCommands.forEach { command ->
        append(command)
        append('\n')
      }
      append("exit\n")
    }

    connect(context).use { connection ->
      connection.open("shell:").use { stream ->
        stream.write(script.toByteArray())
        return readShellOutput(stream)
      }
    }
  }

  fun executeCommand(context: Context, command: String): String {
    val normalizedCommand = command.trim().takeIf { it.isNotEmpty() }
      ?: throw IllegalArgumentException("ADB command must not be blank")

    val rawOutput = executeCommands(context, listOf(normalizedCommand))
    return cleanSingleCommandOutput(rawOutput, normalizedCommand)
  }

  private fun connect(context: Context): AdbConnection {
    val crypto = loadOrCreateCrypto(context)
    val socket = Socket()

    try {
      socket.connect(InetSocketAddress(HOST, PORT), CONNECT_TIMEOUT_MS)
      val connection = AdbConnection.create(socket, crypto)
      val connected = connection.connect(CONNECT_TIMEOUT_MS.toLong(), TimeUnit.MILLISECONDS, false)
      if (!connected) {
        connection.close()
        throw IOException("Connected to $HOST:$PORT, but the ADB handshake did not complete")
      }
      return connection
    } catch (exception: Exception) {
      try {
        socket.close()
      } catch (_: IOException) {
      }
      throw exception
    }
  }

  private fun loadOrCreateCrypto(context: Context): AdbCrypto {
    val keyDirectory = File(context.filesDir, KEY_DIRECTORY).apply {
      mkdirs()
    }
    val privateKey = File(keyDirectory, PRIVATE_KEY_FILE)
    val publicKey = File(keyDirectory, PUBLIC_KEY_FILE)

    return try {
      if (privateKey.exists() && publicKey.exists()) {
        AdbCrypto.loadAdbKeyPair(adbBase64, privateKey, publicKey)
      } else {
        generateAndStoreCrypto(privateKey, publicKey)
      }
    } catch (_: InvalidKeySpecException) {
      generateAndStoreCrypto(privateKey, publicKey)
    }
  }

  private fun hasPersistedKeyPair(context: Context): Boolean {
    val keyDirectory = File(context.filesDir, KEY_DIRECTORY)
    val privateKey = File(keyDirectory, PRIVATE_KEY_FILE)
    val publicKey = File(keyDirectory, PUBLIC_KEY_FILE)

    return privateKey.exists() && publicKey.exists()
  }

  private fun classifyError(exception: Exception): String {
    return when (exception) {
      is ConnectException -> "connection_refused"
      is SocketTimeoutException -> "timeout"
      is InvalidKeySpecException -> "invalid_key_pair"
      is IOException -> {
        if (exception.message?.contains("handshake", ignoreCase = true) == true) {
          "handshake_failed"
        } else {
          "io_error"
        }
      }

      else -> "unknown_error"
    }
  }

  private fun hintForError(exception: Exception): String {
    return when (classifyError(exception)) {
      "connection_refused" ->
        "Nothing is listening on 127.0.0.1:5555. Developer options alone are not enough; the device must expose a local ADB daemon on that port."

      "timeout" ->
        "Connection to 127.0.0.1:5555 timed out. The device may not expose local ADB, or OEM networking policy may block it."

      "invalid_key_pair" ->
        "Stored ADB keys are invalid. Clear the app data or remove the saved keypair and try again."

      "handshake_failed" ->
        "The port is reachable, but the ADB handshake did not complete. The device may require authorization or reject app-originated ADB connections."

      "io_error" ->
        "Local ADB communication failed during socket or stream setup. Check whether the device really exposes ADB on 127.0.0.1:5555."

      else ->
        "Unknown local ADB failure. Check the device's developer settings and whether local ADB over TCP is actually exposed."
    }
  }

  private fun readShellOutput(stream: com.tananaev.adblib.AdbStream): String {
    val output = ByteArrayOutputStream()
    while (true) {
      try {
        output.write(stream.read())
      } catch (_: IOException) {
        break
      }
    }

    return output.toString(Charsets.UTF_8.name())
  }

  private fun cleanSingleCommandOutput(rawOutput: String, command: String): String {
    val normalizedRawOutput = rawOutput.replace("\r\n", "\n")
    val cleanedLines = normalizedRawOutput
      .split('\n')
      .map { line -> stripShellPrompt(line).trim() }
      .filterNot { trimmedLine ->
        trimmedLine.isEmpty() || trimmedLine == command || trimmedLine == "exit"
      }

    return cleanedLines.joinToString("\n").trim()
  }

  private fun stripShellPrompt(line: String): String {
    val shellPromptMatch = Regex("""^.*?[#$]\s""").find(line) ?: return line
    return line.removeRange(shellPromptMatch.range)
  }

  private fun generateAndStoreCrypto(privateKey: File, publicKey: File): AdbCrypto {
    val crypto = AdbCrypto.generateAdbKeyPair(adbBase64)
    crypto.saveAdbKeyPair(privateKey, publicKey)
    return crypto
  }
}
