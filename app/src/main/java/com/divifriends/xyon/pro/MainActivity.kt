package com.divifriends.xyon.pro

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Bundle
import android.provider.Settings
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.divifriends.xyon.pro.databinding.ActivityMainBinding
import java.io.IOException
import java.nio.charset.Charset

class MainActivity : AppCompatActivity(), NfcAdapter.ReaderCallback {

    private lateinit var webView: WebView
    private var uploadMessage: ValueCallback<Array<Uri>>? = null
    private var nfcAdapter: NfcAdapter? = null

    private var fileContentToSave: String? = null

    private val fileChooserLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            uploadMessage?.onReceiveValue(
                WebChromeClient.FileChooserParams.parseResult(result.resultCode, result.data)
            )
        } else {
            uploadMessage?.onReceiveValue(null)
        }
        uploadMessage = null
    }

    private val saveFileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.also { uri ->
                fileContentToSave?.let { content ->
                    writeContentToUri(uri, content)
                }
            }
        } else {
            Toast.makeText(this, "Guardado cancelado", Toast.LENGTH_SHORT).show()
        }
        fileContentToSave = null
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        webView = binding.webView

        webView.isVerticalScrollBarEnabled = false
        webView.isHorizontalScrollBarEnabled = false

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
        }

        webView.webViewClient = WebViewClient()
        setupWebChromeClient()

        webView.addJavascriptInterface(WebAppInterface(this), "AndroidBridge")

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null) {
            // No se muestra toast aquí para no ser molesto al inicio.
            // La comprobación se hará al pulsar el botón.
        }

        // No es necesario procesar el intent aquí para HCE, el servicio lo maneja
        // handleIntent(intent)

        webView.loadUrl("file:///android_asset/index.html")
    }

    private fun setupWebChromeClient() {
        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                uploadMessage?.onReceiveValue(null)
                uploadMessage = filePathCallback

                val intent = fileChooserParams?.createIntent()
                try {
                    fileChooserLauncher.launch(intent)
                    return true
                } catch (e: Exception) {
                    uploadMessage = null
                    Toast.makeText(this@MainActivity, "No se puede abrir el selector de archivos", Toast.LENGTH_SHORT).show()
                    return false
                }
            }
        }
    }

    fun triggerSaveFile(fileName: String, content: String) {
        fileContentToSave = content
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
            putExtra(Intent.EXTRA_TITLE, fileName)
        }
        try {
            saveFileLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "No se puede abrir el diálogo de guardado", Toast.LENGTH_SHORT).show()
            fileContentToSave = null
        }
    }

    private fun writeContentToUri(uri: Uri, content: String) {
        try {
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(content.toByteArray())
                Toast.makeText(this, "¡Respaldo guardado con éxito!", Toast.LENGTH_LONG).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Error al guardar el archivo", Toast.LENGTH_SHORT).show()
        }
    }

    // --- LÓGICA NFC ---

    fun prepareNfcSharing(groupDataJson: String) {
        HostCardEmulatorService.groupData = groupDataJson
    }

    fun enableNfcReaderMode() {
        nfcAdapter?.enableReaderMode(this, this, NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_NFC_F, null)
    }

    fun disableNfcReaderMode() {
        nfcAdapter?.disableReaderMode(this)
    }

    override fun onTagDiscovered(tag: Tag?) {
        val ndef = Ndef.get(tag)
        if (ndef == null) {
            runOnUiThread { Toast.makeText(this, "No se pudo leer la invitación NFC.", Toast.LENGTH_SHORT).show() }
            return
        }

        try {
            ndef.connect()
            val ndefMessage = ndef.ndefMessage
            val record = ndefMessage.records.firstOrNull()
            if (record != null) {
                // El payload de un TextRecord tiene bytes de estado al principio, hay que saltarlos
                val languageCodeLength = record.payload[0].toInt() and 0x3F
                val textPayload = record.payload.copyOfRange(languageCodeLength + 1, record.payload.size)
                val payload = String(textPayload, Charset.forName("UTF-8"))

                runOnUiThread {
                    // Escapamos las comillas simples para que no rompan la cadena de JS
                    val escapedPayload = payload.replace("'", "\\'")
                    webView.evaluateJavascript("javascript:handleNfcDataReceived('$escapedPayload')", null)
                }
            }
            ndef.close()
        } catch (e: Exception) {
            e.printStackTrace()
            runOnUiThread { Toast.makeText(this, "Error al leer la invitación.", Toast.LENGTH_SHORT).show() }
        }
    }


    override fun onPause() {
        super.onPause()
        disableNfcReaderMode()
    }


    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    class WebAppInterface(private val context: Context) {

        @JavascriptInterface
        fun saveFile(fileName: String, content: String) {
            if (context is MainActivity) {
                context.runOnUiThread {
                    context.triggerSaveFile(fileName, content)
                }
            }
        }

        @JavascriptInterface
        fun prepareNfcSharing(groupDataJson: String) {
            if (context is MainActivity) {
                context.runOnUiThread {
                    context.prepareNfcSharing(groupDataJson)
                }
            }
        }

        @JavascriptInterface
        fun enableNfcReader() {
            if (context is MainActivity) {
                context.runOnUiThread {
                    context.enableNfcReaderMode()
                }
            }
        }

        @JavascriptInterface
        fun disableNfcReader() {
            if (context is MainActivity) {
                context.runOnUiThread {
                    context.disableNfcReaderMode()
                }
            }
        }

        @JavascriptInterface
        fun checkNfcStatus(): Boolean {
            val nfcAdapter = NfcAdapter.getDefaultAdapter(context)
            return nfcAdapter?.isEnabled ?: false
        }

        @JavascriptInterface
        fun openNfcSettings() {
            if (context is MainActivity) {
                context.startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
            }
        }
    }
}
