package com.example.polarecgdata

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Environment
import android.view.View
import android.view.WindowManager
import com.example.proctocam.Database.DataModel
import java.io.File
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


fun timestampToDateTime(timestamp: Long): String {
    return try {
        val timestampMillis: Long = timestamp / 1000
        val instant = Instant.ofEpochMilli(timestampMillis)
        val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS")
        dateTime.format(dateFormat)
    } catch (e: Exception) {
        e.printStackTrace()
        "Error converting timestamp to date"
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

@SuppressLint("ResourceType")
fun toggleStatusBarColor(activity: Activity) {
    val window = activity.window
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
//    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
//    window.statusBarColor = activity.resources.getColor(androidx.appcompat.R.attr.colorPrimary)
}

interface ActionCallbackclick {
    fun onActionItemClickedCallback()
    fun onDestroyActionModeCallback()
}


interface OnItemClick {
    fun onItemClick(view: View?, inbox: DataModel?, position: Int)
    fun onLongPress(view: View?, inbox: DataModel?, position: Int)
}

