package com.example.proctocam.Database

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
    var timestamp: String?,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
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
        timeStamp: String,
    ) : this(0, deviceID, name, ecg, hr, timeStamp)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(deviceId)
        parcel.writeString(patientName)
        parcel.writeString(ecg)
        parcel.writeString(hr)
        parcel.writeString(timestamp)
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