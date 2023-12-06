package com.example.polarecgdata

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proctocam.Database.DataModel
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: HomeRepository) : ViewModel() {
    val allTasks: LiveData<List<DataModel>> = repository.allTasks

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