package com.example.polarecgdata

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.example.polarecgdata.adepters.MainAdepter
import com.example.polarecgdata.databinding.DataItemBinding
import com.example.proctocam.Database.DataModel
import com.example.proctocam.Database.DataModelUpdateData
import com.example.proctocam.Database.DatabaseHelper
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.polar.sdk.api.PolarBleApi
import com.polar.sdk.api.PolarBleApiDefaultImpl
import com.polar.sdk.api.model.PolarDeviceInfo
import com.polar.sdk.api.model.PolarEcgData
import com.polar.sdk.api.model.PolarHrData
import com.polar.sdk.api.model.PolarSensorSetting
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import java.util.Calendar
import java.util.TimeZone
import java.util.UUID

class PolarBleApiSingleton private constructor(
    context: Context, dataModel: DataModel
) : com.polar.sdk.api.PolarBleApiCallbackProvider {

    private var ecgDisposable: Disposable? = null
    private var hrDisposable: Disposable? = null
    private var deDisposable: Disposable? = null
    private var database = DatabaseHelper.getInstance(context)
    private val dataModel1 = dataModel
    private val id = dataModel.deviceId
    private val firebaseAnalytics = Firebase.analytics

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


    companion object {
        private var instance: PolarBleApiSingleton? = null
        fun getInstance(
            context: Context,
            dataModel: DataModel,
            itemBinding: DataItemBinding,
            adepter: MainAdepter,
            pos: Int

        ): PolarBleApiSingleton {

            return instance ?: synchronized(this) {
                instance ?: PolarBleApiSingleton(context, dataModel).also {
                    instance = it
                }
            }
        }
    }

    fun connect() {
        polarBleApi.setApiCallback(this)
        id?.let {
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
        polarBleApi.setApiCallback(this)
        return polarBleApi
    }

    private fun streamHR(id: String, name: String) {
        val isDisposed = hrDisposable?.isDisposed ?: true
        lateinit var rrText: String;
        if (isDisposed) {
            hrDisposable = polarBleApi.startHrStreaming(id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { hrData: PolarHrData ->
                        for (sample in hrData.samples) {
                            Log.d("MyApp", "HR " + sample.hr)
                            if (sample.rrsMs.isNotEmpty()) {
                                rrText = "(${sample.rrsMs.joinToString(separator = "ms, ")}ms)"
//                                binding.tvHrVal.text = rrText
                            }
//                            binding.tvHrVal.text = sample.hr.toString()
                            dataModel1.hr = sample.hr.toString()
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
                            }

                        },
                        { error: Throwable ->
                            Log.e("MyApp", "Ecg stream failed $error")
                            dataModel1.ecg = "Ecg stream failed $error"
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
                            ecgDisposable = null
                        },
                        {
                            Log.d("MyApp", "Ecg stream complete")
                        }
                    )
        } else {
            // NOTE stops streaming if it is "running"
            ecgDisposable?.dispose()
            ecgDisposable = null
        }
    }

    @SuppressLint("SetTextI18n")
    override fun batteryLevelReceived(identifier: String, level: Int) {
        Log.d("MyApp", "BATTERY LEVEL: $id --- $level")
        Log.d("MyApp", "Battery level $id --- $identifier $level%")
        dataModel1.battery = "$level%"
        database?.dao?.update(dataModel1)
//        itemBinding.tvBatteryVal.text = "$level%"
    }

    override fun blePowerStateChanged(powered: Boolean) {
        Log.d("MyApp", "BLE power: $id -- $powered")
    }

    override fun bleSdkFeatureReady(
        identifier: String,
        feature: PolarBleApi.PolarBleSdkFeature
    ) {
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
        Log.d("MyApp", "CONNECTED: $id -- ${polarDeviceInfo.deviceId}")
        dataModel1.status = "Connected"
        database?.dao?.update(dataModel1)
//        itemBinding.tvStatusVal.text = "Connected"
//        itemBinding.btnConnect.text = "Disconnect"
    }

    override fun deviceConnecting(polarDeviceInfo: PolarDeviceInfo) {
        Log.d("MyApp", "CONNECTING: $id --  ${polarDeviceInfo.deviceId}")
        dataModel1.status = "Connecting"
        database?.dao?.update(dataModel1)
//        itemBinding.tvStatusVal.text = "Connecting"
    }

    override fun deviceDisconnected(polarDeviceInfo: PolarDeviceInfo) {
        Log.d("MyApp", "DISCONNECTED: $id --  ${polarDeviceInfo.deviceId}")
        dataModel1.status = "Disconnected"
        database?.dao?.update(dataModel1)
//        itemBinding.tvStatusVal.text = "Disconnected"
    }


    override fun disInformationReceived(identifier: String, uuid: UUID, value: String) {
        Log.d("MyApp", "DIS INFO uuid: $id --  $uuid value: $value")
        if (uuid == UUID.fromString("00002a28-0000-1000-8000-00805f9b34fb")) {
            val msg = "Firmware: " + value.trim { it <= ' ' }
            Log.d("MyApp", "Firmware: " + identifier + " " + value.trim { it <= ' ' })
//            itemBinding.tvFamVal.text = value.trim { it <= ' ' }
            dataModel1.firmware =  value.trim { it <= ' ' }
            database?.dao?.update(dataModel1)
            polarBleApi.setLocalTime(
                identifier,
                Calendar.getInstance(TimeZone.getDefault())
            )
            /*try {
                val calendar = polarBleApi.getLocalTime(identifier)
                calendar.blockingSubscribe {
                    itemBinding.tvTimeVal.text = timestampToDateTime(it.timeInMillis)
                }
            } catch (e: Exception) {
                Log.d("MyApp", "Exception : --> ${e.message}" )
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                    param(FirebaseAnalytics.Param.ITEM_ID, System.currentTimeMillis())
                    param(FirebaseAnalytics.Param.ITEM_NAME, "Local Time Exceptions")
                    param(FirebaseAnalytics.Param.CONTENT_TYPE, "Exception : ${e.message} ")
                }
            }*/

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
}
