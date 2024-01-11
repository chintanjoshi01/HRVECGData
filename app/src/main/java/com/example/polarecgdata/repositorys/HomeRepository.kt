package com.example.polarecgdata.repositorys

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.polarecgdata.database.DataModel
import com.example.polarecgdata.database.DatabaseHelper
import com.example.polarecgdata.utils.DataReportModel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class HomeRepository(context: Context) {

    val dao = DatabaseHelper.getInstance(context)?.dao

    val allTasks: LiveData<List<DataModel>> = dao?.getAllPatient()!!
    val allReportTasks: LiveData<List<DataReportModel>> = dao?.getDistinctData()!!

    //    fun getDataWithId(id : String): LiveData<List<DataModel>> = dao?.getDataWithDeviceId()!!
    private val executor: ExecutorService = Executors.newFixedThreadPool(10)

    fun insert(task: DataModel) {
        executor.execute {
            dao?.insert(task)
            executor.shutdown()
        }

    }

    fun update(task: DataModel) {
        dao?.update(task)
    }

    fun delete(task: DataModel) {
        executor.execute {
            dao?.delete(task)
            executor.shutdown()
        }

    }


}