package com.divifriends.xyon.pro.data

import android.content.Context
import com.divifriends.xyon.pro.data.model.AppData
import kotlinx.serialization.json.Json
import java.io.File

/** Abstraction over persistence so the repository stays testable. */
interface AppDataStore {
    fun read(): AppData
    fun write(data: AppData)
}

class FileAppDataStore(private val file: File) : AppDataStore {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }

    override fun read(): AppData = runCatching {
        if (file.exists()) json.decodeFromString(AppData.serializer(), file.readText()) else AppData()
    }.getOrElse { AppData() }

    override fun write(data: AppData) {
        runCatching {
            file.parentFile?.mkdirs()
            val tmp = File(file.parentFile, "${file.name}.tmp")
            tmp.writeText(json.encodeToString(AppData.serializer(), data))
            if (file.exists()) file.delete()
            if (!tmp.renameTo(file)) {
                tmp.copyTo(file, overwrite = true)
                tmp.delete()
            }
        }
    }

    companion object {
        const val FILE_NAME = "divifriends_data.json"
        fun json(): Json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            prettyPrint = true
        }
    }
}

fun Context.createAppDataStore(): AppDataStore =
    FileAppDataStore(File(filesDir, FileAppDataStore.FILE_NAME))
