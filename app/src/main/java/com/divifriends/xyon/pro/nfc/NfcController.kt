package com.divifriends.xyon.pro.nfc

import android.app.Activity
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.provider.Settings
import com.divifriends.xyon.pro.HostCardEmulatorService

/**
 * Bridges the Compose UI to the platform NFC stack.
 *
 * Sending: the data is handed to [HostCardEmulatorService] which emulates a card.
 * Receiving: reader mode is enabled and the APDU response of the emulated card is
 * decoded into the original JSON payload.
 */
class NfcController(
    private val activity: Activity,
    @Volatile var onReceived: ((String) -> Unit)? = null,
    @Volatile var onError: ((String) -> Unit)? = null
) : NfcAdapter.ReaderCallback {

    private val adapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(activity)

    val available: Boolean get() = adapter != null
    val enabled: Boolean get() = adapter?.isEnabled == true

    fun openSettings() {
        runCatching { activity.startActivity(Intent(Settings.ACTION_NFC_SETTINGS)) }
    }

    fun share(json: String) {
        HostCardEmulatorService.groupData = json
    }

    fun startReceiving() {
        adapter?.enableReaderMode(
            activity,
            this,
            NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_NFC_B or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
            null
        )
    }

    fun stopReceiving() {
        runCatching { adapter?.disableReaderMode(activity) }
        HostCardEmulatorService.groupData = null
    }

    override fun onTagDiscovered(tag: Tag?) {
        if (tag == null) return
        val isoDep = IsoDep.get(tag)
        if (isoDep == null) {
            notifyError("Esta etiqueta NFC no es compatible.")
            return
        }
        try {
            isoDep.connect()
            isoDep.timeout = 5_000
            val response = isoDep.transceive(SELECT_APDU)
            isoDep.close()
            val json = decodeNdefResponse(response)
            if (json == null) {
                notifyError("No se pudo leer la invitaci\u00f3n NFC.")
            } else {
                activity.runOnUiThread { onReceived?.invoke(json) }
            }
        } catch (e: Exception) {
            runCatching { isoDep.close() }
            notifyError("Error al leer la invitaci\u00f3n.")
        }
    }

    private fun notifyError(message: String) {
        activity.runOnUiThread { onError?.invoke(message) }
    }

    companion object {
        // Lc = 0x07 to match the 7-byte AID registered in aid_list.xml / HostCardEmulatorService.
        private val SELECT_APDU: ByteArray =
            hexToBytes("00A4040007F0010203040506")

        fun hexToBytes(hex: String): ByteArray {
            val data = ByteArray(hex.length / 2)
            var i = 0
            while (i < hex.length) {
                data[i / 2] = ((Character.digit(hex[i], 16) shl 4) +
                    Character.digit(hex[i + 1], 16)).toByte()
                i += 2
            }
            return data
        }

        /**
         * The emulated card replies with: [ndefLength hi][ndefLength lo][NDEF][90 00].
         * Returns the decoded text payload or null when the exchange is invalid.
         */
        fun decodeNdefResponse(response: ByteArray): String? {
            if (response.size < 4) return null
            val sw1 = response[response.size - 2].toInt() and 0xFF
            val sw2 = response[response.size - 1].toInt() and 0xFF
            if (sw1 != 0x90 || sw2 != 0x00) return null

            val body = response.copyOfRange(0, response.size - 2)
            if (body.size < 2) return null
            val length = ((body[0].toInt() and 0xFF) shl 8) or (body[1].toInt() and 0xFF)
            if (length <= 0 || 2 + length > body.size) return null

            val ndefBytes = body.copyOfRange(2, 2 + length)
            return runCatching {
                val message = NdefMessage(ndefBytes)
                val payload = message.records.firstOrNull()?.payload ?: return null
                if (payload.isEmpty()) return null
                val languageCodeLength = payload[0].toInt() and 0x3F
                val textBytes = payload.copyOfRange(1 + languageCodeLength, payload.size)
                String(textBytes, Charsets.UTF_8)
            }.getOrNull()
        }
    }
}
