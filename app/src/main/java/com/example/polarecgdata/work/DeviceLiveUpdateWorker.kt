package com.example.polarecgdata.work

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.polarecgdata.utils.getCurrentLocalDateTimeWithMillis
import com.example.polarecgdata.utils.timestampToDateTime
import com.example.polarecgdata.database.DataModel
import com.example.polarecgdata.database.DataModelUpdateData
import com.example.polarecgdata.database.DatabaseHelper
import com.example.polarecgdata.utils.ACTION_UPDATE_DATA
import com.example.polarecgdata.utils.BL_DATA_KEY
import com.example.polarecgdata.utils.ECG_DATA_KEY
import com.example.polarecgdata.utils.FR_DATA_KEY
import com.example.polarecgdata.utils.HR_DATA_KEY
import com.example.polarecgdata.utils.STATUS_DATA_KEY
import com.example.polarecgdata.utils.UpdateDataEvent

import com.polar.sdk.api.PolarBleApi
import com.polar.sdk.api.PolarBleApiDefaultImpl
import com.polar.sdk.api.model.PolarDeviceInfo
import com.polar.sdk.api.model.PolarEcgData
import com.polar.sdk.api.model.PolarHrData
import com.polar.sdk.api.model.PolarSensorSetting
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import java.io.IOException
import java.util.Calendar
import java.util.TimeZone
import java.util.UUID
import java.util.concurrent.Executors

class DeviceLiveUpdateWorker(
    private val context: Context,
    workerParams: WorkerParameters,
) : Worker(context, workerParams) {

    private var ecgDisposable: Disposable? = null
    private var hrDisposable: Disposable? = null
    private var deDisposable: Disposable? = null
    private var database = DatabaseHelper.getInstance(context)
    private lateinit var dataModel1: DataModel
    private lateinit var id: String
    private val intent =  Intent(ACTION_UPDATE_DATA)
    private val local =    LocalBroadcastManager.getInstance(applicationContext)
    private val polarBleApi: PolarBleApi = PolarBleApiDefaultImpl.defaultImplementation(
        context.applicationContext,
        setOf(
            PolarBleApi.PolarBleSdkFeature.FEATURE_HR,
            PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_SDK_MODE,
            PolarBleApi.PolarBleSdkFeature.FEATURE_BATTERY_INFO,
            PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_H10_EXERCISE_RECORDING,
            PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_OFFLINE_RECORDING,
            PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_ONLINE_STREAMING,
            PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_DEVICE_TIME_SETUP,
            PolarBleApi.PolarBleSdkFeature.FEATURE_DEVICE_INFO
        )
    )

    override fun doWork(): Result {
        return try {
            connect()
            Result.success()
        } catch (e: IOException) {
            Result.failure()
        }
    }

    private fun connect() {
        val firstValue = inputData.getLong("FIRST_KEY", 0)
        dataModel1 = database?.dao?.getPatientbyId(firstValue)!!
        id = dataModel1.deviceId!!
        polarBleApi.setApiCallback(object : com.polar.sdk.api.PolarBleApiCallbackProvider {
            @SuppressLint("SetTextI18n")
            override fun batteryLevelReceived(identifier: String, level: Int) {
                Log.d("MyApp", "BATTERY LEVEL: $id --- $level")
                Log.d("MyApp", "Battery level $id --- $identifier $level%")
                dataModel1.battery = "$level%"
                intent.putExtra(BL_DATA_KEY, "$level%")
                local.sendBroadcast(intent)
                val executor = Executors.newSingleThreadExecutor()
                executor.execute {
                    database?.dao?.update(dataModel1)
                    executor.shutdown()
                }
            }

            override fun blePowerStateChanged(powered: Boolean) {
                Log.d("MyApp", "BLE power: $id -- $powered")
            }

            override fun bleSdkFeatureReady(
                identifier: String,
                feature: PolarBleApi.PolarBleSdkFeature
            ) {
                Log.d("hdhshkdk", "identifier:  -- $identifier")
                when (feature) {
                    PolarBleApi.PolarBleSdkFeature.FEATURE_HR -> {
                        Log.d("MyApp", "HR ready")
                        dataModel1.deviceId?.let {
                            dataModel1.patientName?.let { it1 ->
                                streamHR(
                                    it,
                                    it1
                                )
                            }
                        }
                    }
                    PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_ONLINE_STREAMING -> {
                        Log.d("MyApp", "FEATURE_POLAR_ONLINE_STREAMING ready")
                        dataModel1.deviceId?.let {
                            dataModel1.patientName?.let { it1 -> streamECG(it, it1) }
                        }
                    }

                    PolarBleApi.PolarBleSdkFeature.FEATURE_DEVICE_INFO -> {

                    }

                    else -> {}
                }

                Log.d("MyApp", "Polar BLE SDK feature $feature is ready")
            }

            override fun deviceConnected(polarDeviceInfo: PolarDeviceInfo) {
                Log.d("MyApp", "CONNECTED: -- ${polarDeviceInfo.deviceId}")
                Log.d("hdhshkdk", "address:  -- ${polarDeviceInfo.address}")
                Log.d("hdhshkdk", "rssi:-- ${polarDeviceInfo.rssi}")
                Log.d("hdhshkdk", "name:  -- ${polarDeviceInfo.name}")
                dataModel1.status = "Connected"
                intent.putExtra(STATUS_DATA_KEY, "Connected")
                local.sendBroadcast(intent)
                val executor = Executors.newSingleThreadExecutor()
                executor.execute {
                    database?.dao?.update(dataModel1)
                    executor.shutdown()
                }
            }

            override fun deviceConnecting(polarDeviceInfo: PolarDeviceInfo) {
                Log.d("MyApp", "CONNECTING: $id --  ${polarDeviceInfo.deviceId}")
                dataModel1.status = "Connecting"
                intent.putExtra(STATUS_DATA_KEY, "Connecting")
                local.sendBroadcast(intent)
                val executor = Executors.newSingleThreadExecutor()
                executor.execute {
                    database?.dao?.update(dataModel1)
                    executor.shutdown()
                }
            }

            override fun deviceDisconnected(polarDeviceInfo: PolarDeviceInfo) {
                Log.d("MyApp", "DISCONNECTED: $id --  ${polarDeviceInfo.deviceId}")
                dataModel1.status = "Disconnected"
                intent.putExtra(STATUS_DATA_KEY, "Disconnected")
                local.sendBroadcast(intent)
                val executor = Executors.newSingleThreadExecutor()
                executor.execute {
                    database?.dao?.update(dataModel1)
                    executor.shutdown()
                }
            }


            override fun disInformationReceived(identifier: String, uuid: UUID, value: String) {
                Log.d("MyApp", "DIS INFO uuid: $id --  $uuid value: $value")
                if (uuid == UUID.fromString("00002a28-0000-1000-8000-00805f9b34fb")) {
                    val msg = "Firmware: " + value.trim { it <= ' ' }
                    Log.d("MyApp", "Firmware: " + identifier + " " + value.trim { it <= ' ' })
                    dataModel1.firmware = value.trim { it <= ' ' }
                    intent.putExtra(FR_DATA_KEY, value.trim { it <= ' ' })
                    local.sendBroadcast(intent)
                    val executor = Executors.newSingleThreadExecutor()
                    executor.execute {
                        database?.dao?.update(dataModel1)
                        executor.shutdown()
                    }

                    polarBleApi.setLocalTime(
                        identifier,
                        Calendar.getInstance(TimeZone.getDefault())
                    )

                }
            }

            override fun hrFeatureReady(identifier: String) {

            }

            override fun hrNotificationReceived(
                identifier: String,
                data: PolarHrData.PolarHrSample
            ) {

            }

            override fun polarFtpFeatureReady(identifier: String) {

            }

            override fun sdkModeFeatureAvailable(identifier: String) {

            }

            override fun streamingFeaturesReady(
                identifier: String,
                features: Set<PolarBleApi.PolarDeviceDataType>
            ) {

            }
        })
        id.let {
            polarBleApi.connectToDevice(it)
            Log.d("MyApp", "ID --> $it")

        }
    }

    fun disconnect() {
        dataModel1.deviceId?.let { polarBleApi.disconnectFromDevice(it) }
    }


    fun getPolarBleApi(
        callback: com.polar.sdk.api.PolarBleApiCallbackProvider,
        id: String
    ): PolarBleApi {
        polarBleApi.connectToDevice(id)
        polarBleApi.setApiCallback(callback)
        return polarBleApi
    }

    fun getPolarBleApi(
    ): PolarBleApi {
        return polarBleApi
    }

    private fun streamHR(id: String, name: String) {
        val isDisposed = hrDisposable?.isDisposed ?: true
        var rrText: String = ""
        if (isDisposed) {
            hrDisposable = polarBleApi.startHrStreaming(id)
                    .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .subscribe(
                    { hrData: PolarHrData ->
                        for (sample in hrData.samples) {
                            Log.d("MyApp", "HR " + sample.hr)
                            if (sample.rrsMs.isNotEmpty()) {
                                rrText = "(${sample.rrsMs} ms"
                            }
                            dataModel1.hr = sample.hr.toString()
                            intent.putExtra(HR_DATA_KEY, sample.hr.toString())
                            local.sendBroadcast(intent)
                            val executor = Executors.newSingleThreadExecutor()
                            executor.execute {
                                database?.dao?.update(dataModel1)
                                database?.dao?.insert1(
                                    DataModelUpdateData(
                                        id,
                                        name,
                                        sample.hr.toString(),
                                        sample.hr.toString(),
                                        rrText,
                                        "",
                                        getCurrentLocalDateTimeWithMillis()
                                    )
                                )
                                executor.shutdown()
                            }


                        }
                    },
                    { error: Throwable ->
                        Log.e("MyApp", "HR stream failed. Reason $error")
                        hrDisposable = null
                    },
                    {

                        Log.d("MyApp", "HR stream complete")
                    }
                )
        } else {
            hrDisposable?.dispose()
            hrDisposable = null
        }
    }

    private fun streamECG(id: String, name: String) {
        val isDisposed = ecgDisposable?.isDisposed ?: true
        if (isDisposed) {
            ecgDisposable =
                polarBleApi.requestStreamSettings(id, PolarBleApi.PolarDeviceDataType.ECG)
                    .toFlowable()
                    .flatMap { sensorSetting: PolarSensorSetting ->
                        polarBleApi.startEcgStreaming(
                            id,
                            sensorSetting.maxSettings()
                        )
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { polarEcgData: PolarEcgData ->
                            Log.d("MyApp", "ecg update")
                            for (data in polarEcgData.samples) {
                                dataModel1.ecg = "${((data.voltage.toFloat() / 1000.0).toFloat())}"
                                intent.putExtra(ECG_DATA_KEY,  "${((data.voltage.toFloat() / 1000.0).toFloat())}")
                                local.sendBroadcast(intent)
                                val executor = Executors.newSingleThreadExecutor()
                                executor.execute {
                                    database?.dao?.update(dataModel1)
                                    database?.dao?.insert1(
                                        DataModelUpdateData(
                                            id,
                                            name,
                                            "${((data.voltage.toFloat() / 1000.0).toFloat())}",
                                            "",
                                            "",
                                            timestampToDateTime(data.timeStamp),
                                            getCurrentLocalDateTimeWithMillis()
                                        )
                                    )
                                    executor.shutdown()
                                }

                            }

                        },
                        { error: Throwable ->
                            Log.e("MyApp", "Ecg stream failed $error")
                            dataModel1.ecg = "Ecg stream failed $error"
                            val executor = Executors.newSingleThreadExecutor()
                            executor.execute {
                                database?.dao?.update(dataModel1)
                                database?.dao?.insert1(
                                    DataModelUpdateData(
                                        id,
                                        name,
                                        "Ecg stream failed $error",
                                        "",
                                        "",
                                        "",
                                        getCurrentLocalDateTimeWithMillis()
                                    )
                                )
                                executor.shutdown()
                            }
                            ecgDisposable = null
                        },
                        {
                            Log.d("MyApp", "Ecg stream complete")
                        }
                    )
        } else {
            ecgDisposable?.dispose()
            ecgDisposable = null
        }
    }


}