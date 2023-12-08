package com.example.polarecgdata

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.proctocam.Database.DataModelUpdateData
import com.example.proctocam.Database.DatabaseHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
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
            Result.failure()
        }
    }

    private suspend fun exportDataToCsv(
        context: Context,
        database: DatabaseHelper,
        batchSize: Int = 1000
    ) {
        val dao = database.dao

        val csvFileName = "${deviceIddd}_exported_data.csv"
        val csvFile = File(createAppDirectoryInDoc(context), csvFileName)

        try {
            csvFile.bufferedWriter().use { writer ->
                writer.appendLine("Id,Device ID,Name,HR , ECG, Time")
                var offset = 0
                var dataChunk: List<DataModelUpdateData>
                do {
                    Log.d("jfsljfjsd", "Device ID: " + deviceIddd)
                    dataChunk = dao!!.getDataWithDeviceId(deviceIddd, offset, batchSize)
                    dataChunk.forEach { entity ->
                        writer.appendLine("${entity.id},${entity.deviceId},${entity.patientName},${entity.hr},${entity.ecg},${entity.timestamp}")
                    }
                    offset += batchSize
                } while (dataChunk.isNotEmpty())
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Data exported to $csvFileName", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error exporting data", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
