package com.example.proctocam.Database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteQuery

@Dao
interface DaoAc {

    @Insert
     fun insert(task: DataModel): Long

    @Insert
    fun insert1(task: DataModelUpdateData): Long

    @Update
     fun update(task: DataModel): Int

    @Delete
     fun delete(task: DataModel): Int


    @Query("SELECT * FROM DataTable ORDER BY id ASC")
    fun getAllPatient(): LiveData<List<DataModel>>


    @Query("SELECT * FROM DataTable WHERE id LIKE :idd")
    fun getPatientbyId(idd: Long): DataModel

    @Query("SELECT * FROM DataTableUpdate WHERE deviceId LIKE :deviceId LIMIT :offset, :limit")
    fun getDataWithDeviceId(deviceId: String,offset: Int, limit: Int): List<DataModelUpdateData>

    @RawQuery(observedEntities = [DataModel::class])
    fun updateData(supportSQLiteQuery: SupportSQLiteQuery?): DataModel?
}
