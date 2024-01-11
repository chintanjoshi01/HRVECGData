package com.example.polarecgdata.work

import android.content.Context
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.polarecgdata.database.DataModelUpdateData
import com.example.polarecgdata.database.DatabaseHelper
import com.example.polarecgdata.utils.DeviceDetails
import com.example.polarecgdata.utils.TimberRemoteTree
import com.example.polarecgdata.utils.createAppDirectoryInDoc
import com.example.polarecgdata.utils.remoteTree
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

// CsvExportWorker.kt
class CsvExportWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {


    companion object {
        lateinit var deviceIddd: String
    }

    override fun doWork(): Result {
        return try {
            val database = DatabaseHelper.getInstance(context)
            runBlocking {
                if (database != null) {
                    exportDataToCsv(applicationContext, database)
                }
            }
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            remoteTree.log(1,"CsvExportWorker doWork Exception --->> \n ${e.message} stackTrace --> \n ${e.printStackTrace()}")
            Timber.plant(remoteTree)
            Result.failure()
        }
    }

    private suspend fun exportDataToCsv(
        context: Context,
        database: DatabaseHelper,
        batchSize: Int = 1000
    ) {
        val dao = database.dao
        val csvFileName = "${deviceIddd}_exported_data_${System.currentTimeMillis()}.csv"
        val csvFile = File(createAppDirectoryInDoc(context), csvFileName)
        try {
            csvFile.bufferedWriter().use { writer ->
                writer.appendLine("Id,Device ID,Name,HR ,RR, ECG, Time")
                var offset = 0
                var dataChunk: List<DataModelUpdateData>
                do {
                    Log.d("jfsljfjsd", "Device ID: $deviceIddd")
                    dataChunk = dao!!.getDataWithDeviceId(deviceIddd, offset, batchSize)
                    dataChunk.forEach { entity ->
                        Log.d("jfsljfjsd", "timestamp2 :  ${entity.timestamp2}")
                        writer.appendLine("${entity.id},${entity.deviceId},${entity.patientName},${entity.hr},${entity.rr},${entity.ecg},${entity.timestamp2}")
                    }
                    offset += batchSize
                } while (dataChunk.isNotEmpty())
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Data exported to $csvFileName", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("jfsljfjsd", "Error: $deviceIddd")
            withContext(Dispatchers.Main) {
                remoteTree.log(1,"CsvExportWorker Exception --->> \n ${e.message} stackTrace --> \n ${e.printStackTrace()}")
                Timber.plant(remoteTree)
                Toast.makeText(context, "Error exporting data", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
