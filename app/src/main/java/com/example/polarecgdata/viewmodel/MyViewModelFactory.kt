package com.example.polarecgdata.viewmodel

import androidx.annotation.Keep
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.polarecgdata.repositorys.HomeRepository


@Keep
class MyViewModelFactory(val repository: HomeRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(HomeRepository::class.java).newInstance(repository)
    }
}