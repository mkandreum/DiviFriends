package com.divifriends.xyon.pro

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import java.util.Locale

class HostCardEmulatorService : HostApduService() {

    companion object {
        @Volatile
        var groupData: String? = null

        private const val AID = "F0010203040506"
        private const val SELECT_APDU_HEADER = "00A40400"

        private val SELECT_OK_SW = hexStringToByteArray("9000")
        private val UNKNOWN_CMD_SW = hexStringToByteArray("0000")

        fun hexStringToByteArray(s: String): ByteArray {
            val len = s.length
            val data = ByteArray(len / 2)
            var i = 0
            while (i < len) {
                data[i / 2] = ((Character.digit(s[i], 16) shl 4) + Character.digit(s[i + 1], 16)).toByte()
                i += 2
            }
            return data
        }

        fun byteArrayToHexString(bytes: ByteArray): String {
            val hexChars = CharArray(bytes.size * 2)
            for (j in bytes.indices) {
                val v = bytes[j].toInt() and 0xFF
                hexChars[j * 2] = "0123456789ABCDEF"[v ushr 4]
                hexChars[j * 2 + 1] = "0123456789ABCDEF"[v and 0x0F]
            }
            return String(hexChars)
        }
    }

    override fun onDeactivated(reason: Int) {
        groupData = null
    }

    override fun processCommandApdu(commandApdu: ByteArray, extras: Bundle?): ByteArray {
        // APDUs can come from any reader in range, so never assume their length.
        if (commandApdu.size < 5) return UNKNOWN_CMD_SW
        val command = byteArrayToHexString(commandApdu)
        if (command.length < 10 + AID.length) return UNKNOWN_CMD_SW
        if (!command.startsWith(SELECT_APDU_HEADER)) return UNKNOWN_CMD_SW

        val aid = command.substring(10, 10 + AID.length)
        if (!aid.equals(AID, ignoreCase = true)) return UNKNOWN_CMD_SW

        val data = groupData ?: return UNKNOWN_CMD_SW
        val record = NdefRecord.createTextRecord("en", data)
        val ndefBytes = NdefMessage(arrayOf(record)).toByteArray()

        // [NDEF length (2 bytes)] + [NDEF message] + [SW 90 00]
        val response = ByteArray(2 + ndefBytes.size + 2)
        response[0] = (ndefBytes.size / 256).toByte()
        response[1] = (ndefBytes.size % 256).toByte()
        System.arraycopy(ndefBytes, 0, response, 2, ndefBytes.size)
        System.arraycopy(SELECT_OK_SW, 0, response, 2 + ndefBytes.size, SELECT_OK_SW.size)
        return response
    }
}
