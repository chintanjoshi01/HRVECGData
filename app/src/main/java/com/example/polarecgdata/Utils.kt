package com.example.polarecgdata

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Environment
import android.view.View
import android.view.WindowManager
import com.example.proctocam.Database.DataModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone


fun timestampToDateTime(millis: Long): String {
    val hours = millis / (1000 * 60 * 60) % 24
    val minutes = millis / (1000 * 60) % 60
    val seconds = millis / 1000 % 60
    val milliseconds = millis % 1000
    return String.format(
        "%02d:%02d:%02d:%02d:%02d:%02d",
        hours,
        minutes,
        seconds,
        milliseconds / 100,
        milliseconds % 100 / 10,
        milliseconds % 10
    )
}

fun getCurrentLocalDateTimeWithMillis(): String {
    val currentTimeMillis = System.currentTimeMillis()
    val calendar = Calendar.getInstance(TimeZone.getDefault())
    calendar.timeInMillis = currentTimeMillis
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
    return sdf.format(calendar.time)
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
}

interface ActionCallbackclick {
    fun onActionItemClickedCallback()
    fun onDestroyActionModeCallback()
}


interface OnItemClick {
    fun onItemClick(view: View?, inbox: DataModel?, position: Int)
    fun onLongPress(view: View?, inbox: DataModel?, position: Int)
}
interface onAPICallBack{
    fun connect()
    fun disconnect()
}

