package com.example.polarecgdata.database

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey


@Entity(tableName = "DataTableUpdate")
data class DataModelUpdateData(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    var deviceId: String?,
    var patientName: String?,
    var ecg: String?,
    var hr: String?,
    var rr: String?,
    var timestamp: String?,
    var timestamp2: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
    ) {
    }

    @Ignore
    constructor(
        deviceID: String,
        name: String,
        ecg: String,
        hr: String,
        rr: String,
        timeStamp: String,
        timeStamp2: String,
    ) : this(0, deviceID, name, ecg, hr, rr, timeStamp, timeStamp2)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(deviceId)
        parcel.writeString(patientName)
        parcel.writeString(ecg)
        parcel.writeString(hr)
        parcel.writeString(rr)
        parcel.writeString(timestamp)
        parcel.writeString(timestamp2)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DataModelUpdateData> {
        override fun createFromParcel(parcel: Parcel): DataModelUpdateData {
            return DataModelUpdateData(parcel)
        }

        override fun newArray(size: Int): Array<DataModelUpdateData?> {
            return arrayOfNulls(size)
        }
    }
}