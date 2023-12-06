package com.example.polarecgdata

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.proctocam.Database.DataModel
import com.example.proctocam.Database.DatabaseHelper


class HomeRepository(context: Context) {

    val dao = DatabaseHelper.getInstance(context)?.dao

    val allTasks: LiveData<List<DataModel>> = dao?.getAllPatient()!!


    fun insert(task: DataModel) {
        dao?.insert(task)
    }

    fun update(task: DataModel) {
        dao?.update(task)
    }

    fun delete(task: DataModel) {
        dao?.delete(task)
    }

}