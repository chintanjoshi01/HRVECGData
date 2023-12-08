package com.example.polarecgdata

import com.polar.sdk.api.PolarBleApi
import com.polar.sdk.api.PolarBleApiCallback
import com.polar.sdk.api.model.PolarDeviceInfo
import com.polar.sdk.api.model.PolarHrData
import java.util.UUID

class CustomPolar :com.polar.sdk.api.PolarBleApiCallbackProvider {
    override fun batteryLevelReceived(identifier: String, level: Int) {

    }

    override fun blePowerStateChanged(powered: Boolean) {
        TODO("Not yet implemented")
    }

    override fun bleSdkFeatureReady(identifier: String, feature: PolarBleApi.PolarBleSdkFeature) {
        TODO("Not yet implemented")
    }

    override fun deviceConnected(polarDeviceInfo: PolarDeviceInfo) {
        TODO("Not yet implemented")
    }

    override fun deviceConnecting(polarDeviceInfo: PolarDeviceInfo) {
        TODO("Not yet implemented")
    }

    override fun deviceDisconnected(polarDeviceInfo: PolarDeviceInfo) {
        TODO("Not yet implemented")
    }

    override fun disInformationReceived(identifier: String, uuid: UUID, value: String) {
        TODO("Not yet implemented")
    }

    override fun hrFeatureReady(identifier: String) {
        TODO("Not yet implemented")
    }

    override fun hrNotificationReceived(identifier: String, data: PolarHrData.PolarHrSample) {
        TODO("Not yet implemented")
    }

    override fun polarFtpFeatureReady(identifier: String) {
        TODO("Not yet implemented")
    }

    override fun sdkModeFeatureAvailable(identifier: String) {
        TODO("Not yet implemented")
    }

    override fun streamingFeaturesReady(
        identifier: String,
        features: Set<PolarBleApi.PolarDeviceDataType>
    ) {
        TODO("Not yet implemented")
    }
}