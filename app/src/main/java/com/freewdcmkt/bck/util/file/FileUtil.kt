package com.freewdcmkt.bck.util.file

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import java.io.File
import java.io.IOException

fun uriToFile(uri: Uri, context: Context): File? {
    val contentResolver = context.contentResolver
    val inputStream = contentResolver.openInputStream(uri) ?: return null
    val fileName = getFileName(uri, contentResolver) ?: "temp_image.jpg"
    val file = File(context.cacheDir, fileName)
    return try {
        file.outputStream().use { output ->
            inputStream.copyTo(output)
        }
        file
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}

fun getFileName(uri: Uri, contentResolver: ContentResolver): String? {
    val cursor = contentResolver.query(
        uri,
        arrayOf(MediaStore.MediaColumns.DISPLAY_NAME),
        null,
        null,
        null
    )
    return cursor?.use {
        if (it.moveToFirst()) {
            it.getString(it.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME))
        } else null
    } ?: uri.path?.let { File(it).name }
}