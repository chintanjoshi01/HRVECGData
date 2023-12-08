package com.example.polarecgdata

import android.content.Context
import android.os.Environment
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun timestampToDateTime(timestamp: Long): String {
    try {
        // Convert timestamp to Date
        val date = Date(timestamp)

        // Create a SimpleDateFormat object with the desired format
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        // Format the Date object
        return dateFormat.format(date)
    } catch (e: Exception) {
        e.printStackTrace()
        return "Error converting timestamp to date"
    }
}

fun createAppDirectoryInDoc(context: Context): File? {
    val directory =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
    val appDirectory = File(directory, context.resources.getString(R.string.app_name))
    if (!appDirectory.exists()) {
        val directoryCreated = appDirectory.mkdir()
        if (!directoryCreated) {
            // Failed to create the directory
            return null
        }
    }
    return appDirectory
}