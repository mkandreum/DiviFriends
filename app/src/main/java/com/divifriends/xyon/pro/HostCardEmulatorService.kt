package com.divifriends.xyon.pro

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import java.nio.charset.Charset
import java.util.*

class HostCardEmulatorService : HostApduService() {

    companion object {
        var groupData: String? = null
        private val AID = "F0010203040506"
        private val SELECT_APDU_HEADER = "00A40400"
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
        val command = byteArrayToHexString(commandApdu)

        if (command.substring(0, SELECT_APDU_HEADER.length) == SELECT_APDU_HEADER) {
            val aid = command.substring(10, 10 + AID.length)
            if (aid.uppercase(Locale.ROOT) == AID) {
                val data = groupData
                if (data != null) {
                    val record = NdefRecord.createTextRecord("en", data)
                    val ndefMessage = NdefMessage(arrayOf(record))
                    val ndefBytes = ndefMessage.toByteArray()

                    // Preparamos la respuesta para el lector
                    // [NDEF Message Length (2 bytes)] + [NDEF Message] + [OK Status (9000)]
                    val response = ByteArray(2 + ndefBytes.size + 2)

                    // NDEF Length
                    response[0] = (ndefBytes.size / 256).toByte()
                    response[1] = (ndefBytes.size % 256).toByte()

                    // NDEF Message
                    System.arraycopy(ndefBytes, 0, response, 2, ndefBytes.size)

                    // Status Word OK
                    response[2 + ndefBytes.size] = 0x90.toByte()
                    response[2 + ndefBytes.size + 1] = 0x00.toByte()

                    return response
                }
            }
        }
        return UNKNOWN_CMD_SW
    }
}