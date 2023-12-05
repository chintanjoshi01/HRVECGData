package com.example.proctocam.Database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface DaoAc {

    @Insert
     fun insert(task: DataModel): Long

    @Update
     fun update(task: DataModel): Int

    @Delete
     fun delete(task: DataModel): Int


    /*@Query("SELECT * FROM Patient ORDER BY id ASC")
    fun getAllPatient(): LiveData<List<DataModel>>


    @Query("SELECT * FROM Patient WHERE id LIKE :idd")
    fun getPatientbyId(idd: Long): DataModel*/

}
