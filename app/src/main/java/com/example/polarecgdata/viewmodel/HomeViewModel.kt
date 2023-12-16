package com.example.polarecgdata.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.polarecgdata.repositorys.HomeRepository
import com.example.polarecgdata.database.DataModel
import com.example.polarecgdata.utils.DataReportModel
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: HomeRepository) : ViewModel() {
    val allTasks: LiveData<List<DataModel>> = repository.allTasks
    val allReportTasks: LiveData<List<DataReportModel>> = repository.allReportTasks
//    fun getDataWithId(id: String):LiveData<List<DataModel>> = repository.getDataWithId(id)

    fun insert(task: DataModel) {
        viewModelScope.launch {
            val result = viewModelScope.launch {
                repository.insert(task)
            }
        }

    }

    fun update(task: DataModel) {
        val result = viewModelScope.launch {
            repository.update(task)
        }
    }

    fun delete(task: DataModel) {
        val result = viewModelScope.launch {
            repository.delete(task)
        }
    }

}