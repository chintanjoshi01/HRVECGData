package com.example.polarecgdata.database

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey


@Entity(tableName = "DataTable")
data class DataModel(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    var deviceId: String?,
    var patientName: String?,
    var ecg: String?,
    var hr: String?,
    var timestamp: String?,
    var battery : String?,
    var firmware : String?,
    var status : String?,

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
        parcel.readString()
    ) {
    }

    @Ignore
    constructor(
        deviceID: String,
        name: String,
        ecg: String,
        hr: String,
        timeStamp: String,
        battery: String,
        firmware: String,
        status: String,
    ) : this(0, deviceID, name, ecg, hr, timeStamp,battery,firmware, status)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(deviceId)
        parcel.writeString(patientName)
        parcel.writeString(ecg)
        parcel.writeString(hr)
        parcel.writeString(timestamp)
        parcel.writeString(battery)
        parcel.writeString(firmware)
        parcel.writeString(status)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DataModel> {
        override fun createFromParcel(parcel: Parcel): DataModel {
            return DataModel(parcel)
        }

        override fun newArray(size: Int): Array<DataModel?> {
            return arrayOfNulls(size)
        }
    }
}