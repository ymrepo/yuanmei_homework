package com.lagunalabs.swapigraphql.utils

import android.content.Context
import java.io.IOException

object JsonReader {
    fun readJsonFile(context: Context, fileName: String): String? {
        return try {
            val inputStream = context.assets.open(fileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            val jsonString = String(buffer, Charsets.UTF_8)
            jsonString
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}

