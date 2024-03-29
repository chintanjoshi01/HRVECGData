package com.example.polarecgdata.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Environment
import android.view.View
import android.view.WindowManager
import androidx.sqlite.db.SimpleSQLiteQuery
import com.example.polarecgdata.BuildConfig
import com.example.polarecgdata.R
import com.example.polarecgdata.database.DataModel
import com.example.polarecgdata.database.DatabaseHelper
import timber.log.Timber
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

lateinit var dataModel: DataModel

lateinit var devicePhoneId: String

lateinit var remoteTree: TimberRemoteTree

class UpdateDataEvent(val newData: DataModel)

data class RemoteLog(
    var priority: String,
    var tag: String?,
    var message: String,
    var throwable: String?,
    val time: String
)


data class DeviceDetails(
    val deviceId: String,
    val osVersion: String = Build.VERSION.RELEASE,
    val manufacturer: String = Build.MANUFACTURER,
    val brand: String = Build.BRAND,
    val device: String = Build.DEVICE,
    val model: String = Build.MODEL,
    val appVersionName: String = BuildConfig.VERSION_NAME,
    val appVersionCode: Int = BuildConfig.VERSION_CODE
)

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
            val mediaStorageDir = File(Environment.getExternalStorageDirectory(), "Documents")
            if (!mediaStorageDir.exists()) {
                mediaStorageDir.mkdir()
                if (!mediaStorageDir.mkdirs()) {
                    remoteTree.log(1, "Directory not created")
                    Timber.plant(remoteTree)
                }
            }
            val appDir = File(mediaStorageDir, context.resources.getString(R.string.app_name))
            if (!appDir.exists()) {
                appDir.mkdir()
                if (!mediaStorageDir.mkdirs()) {
                    val appdir2 = context.getExternalFilesDir(null)!!
                    val documentsDirectory = File(appdir2, "Documents")
                    if (!documentsDirectory.exists()) {
                        documentsDirectory.mkdirs()
                    }
                    val appDirectory2 = File(documentsDirectory, context.resources.getString(R.string.app_name))
                    if (!appDirectory2.exists()) {
                        appDirectory2.mkdirs()
                    }
                    return appDirectory2

                }
            }
            return appDir
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


