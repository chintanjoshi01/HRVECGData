package com.example.polarecgdata.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Environment
import android.view.View
import android.view.WindowManager
import androidx.sqlite.db.SimpleSQLiteQuery
import com.example.polarecgdata.R
import com.example.polarecgdata.database.DataModel
import com.example.polarecgdata.database.DatabaseHelper
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.Executors


const val ACTION_UPDATE_DATA = "action_update_data"
const val ECG_DATA_KEY = "ecg_data_key"
const val HR_DATA_KEY = "hr_data_key"
const val STATUS_DATA_KEY = "status_data_key"
const val BL_DATA_KEY = "bl_data_key"
const val FR_DATA_KEY = "fr_data_key"

var NAME: String = ""
var ID: String = ""

 lateinit  var dataModel :DataModel

class UpdateDataEvent(val newData: DataModel)

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
    val currentTimeMillis = Date().time
    val calendar = Calendar.getInstance(TimeZone.getDefault())
    calendar.timeInMillis = currentTimeMillis
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS", Locale.getDefault())
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

interface onAPICallBack {
    fun connect()
    fun disconnect()
}

fun updateProcedure(colName: String, whereVal: String?, id: String?, database: DatabaseHelper) {
    val s = "UPDATE DataTable SET $colName=? WHERE deviceId LIKE ?"
    val executor = Executors.newSingleThreadExecutor()
    executor.execute {
        database.dao
            ?.updateData(SimpleSQLiteQuery(s, arrayOf<Any?>(whereVal, id)))
        executor.shutdown()
    }
}


fun updateProcedureEmpty(
    id: String?,
    database: DatabaseHelper
) {
    val s =
        "UPDATE DataTable \n" +
                "SET ecg = '', hr = '', timestamp = '', battery = '', firmware = '', status = '' \n" +
                "WHERE deviceId LIKE ?\n"
    val executor = Executors.newSingleThreadExecutor()
    executor.execute {
        database.dao
            ?.updateData(SimpleSQLiteQuery(s, arrayOf<Any?>(id)))
        executor.shutdown()
    }
}

data class DataReportModel(val name: String, val id: String)


