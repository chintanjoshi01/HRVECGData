package com.example.polarecgdata

import android.content.Context
import com.polar.sdk.api.PolarBleApi
import com.polar.sdk.api.PolarBleApiDefaultImpl

class PolarBleApiSingleton private constructor(context: Context) {

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

        fun getInstance(context: Context): PolarBleApiSingleton {
            return instance ?: synchronized(this) {
                instance ?: PolarBleApiSingleton(context).also { instance = it }
            }
        }
    }

    // Other methods can be added based on your use case

    fun getPolarBleApi(callback: com.polar.sdk.api.PolarBleApiCallbackProvider, id : String): PolarBleApi {
        polarBleApi.connectToDevice(id)
        polarBleApi.setApiCallback(callback)
        return polarBleApi
    }
}
