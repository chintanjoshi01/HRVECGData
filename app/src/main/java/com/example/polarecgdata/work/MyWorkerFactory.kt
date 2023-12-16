package com.example.polarecgdata.work

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.example.polarecgdata.work.DeviceLiveUpdateWorker
import com.example.polarecgdata.database.DataModel

class MyWorkerFactory(private val repository: DataModel) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return DeviceLiveUpdateWorker(appContext, workerParameters)
    }
}