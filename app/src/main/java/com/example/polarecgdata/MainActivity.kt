package com.example.polarecgdata

import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.polarecgdata.databinding.ActivityMainBinding
import com.polar.sdk.api.PolarBleApi
import com.polar.sdk.api.PolarBleApiCallback
import com.polar.sdk.api.PolarBleApiDefaultImpl.defaultImplementation
import com.polar.sdk.api.errors.PolarInvalidArgument
import com.polar.sdk.api.model.PolarDeviceInfo
import com.polar.sdk.api.model.PolarEcgData
import com.polar.sdk.api.model.PolarHrData
import com.polar.sdk.api.model.PolarSensorSetting
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val PERMISSION_REQUEST_CODE: Int = 101
    private lateinit var sharedPreferencesUtils: SharedPreferencesUtils
    private lateinit var api: PolarBleApi
    private lateinit var id: String
    private var ecgDisposable: Disposable? = null
    private var hrDisposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        sharedPreferencesUtils = SharedPreferencesUtils(this)
        id = sharedPreferencesUtils.getString("DeviceID", "NA").toString()
        if (TextUtils.equals(id, "NA")) {
            binding.btnSetID.text = "Set ID"
        } else {
            binding.btnSetID.text = "Change ID"

            binding.etDeviceID.setText(id)
            binding.etDeviceID.isEnabled = false
        }
        binding.btnSetID.setOnClickListener {
            if (TextUtils.equals(binding.btnSetID.text, "Change ID")) {
                binding.etDeviceID.isEnabled = true
                binding.btnSetID.text = "Set ID"
            }
            if (!TextUtils.isEmpty(binding.etDeviceID.text)) {
                sharedPreferencesUtils.saveString("DeviceID", binding.etDeviceID.text.toString())
            } else {
                binding.etDeviceID.setError("Please Enter Device ID")
            }
        }
        setupAPI()
        binding.btnConnect.setOnClickListener {
            try {
                api.connectToDevice(id)
            } catch (a: PolarInvalidArgument) {
                a.printStackTrace()
            }
        }

        reqPermission()


    }


    private fun reqPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                requestPermissions(
                    arrayOf(
                        android.Manifest.permission.BLUETOOTH_SCAN,
                        android.Manifest.permission.BLUETOOTH_CONNECT
                    ), PERMISSION_REQUEST_CODE
                )
            } else {
                requestPermissions(
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSION_REQUEST_CODE
                )
            }
        } else {
            requestPermissions(
                arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun setupAPI() {
        api = defaultImplementation(
            applicationContext,
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

        api.setApiCallback(object : PolarBleApiCallback() {

            override fun blePowerStateChanged(powered: Boolean) {
                Log.d("MyApp", "BLE power: $powered")
            }

            override fun deviceConnected(polarDeviceInfo: PolarDeviceInfo) {
                Log.d("MyApp", "CONNECTED: ${polarDeviceInfo.deviceId}")
                binding.tvStatus.text = "Connected to ${polarDeviceInfo.deviceId}"
            }

            override fun deviceConnecting(polarDeviceInfo: PolarDeviceInfo) {
                Log.d("MyApp", "CONNECTING: ${polarDeviceInfo.deviceId}")
                binding.tvStatus.text = "Cnnecting to  ${polarDeviceInfo.deviceId}"
            }

            override fun deviceDisconnected(polarDeviceInfo: PolarDeviceInfo) {
                Log.d("MyApp", "DISCONNECTED: ${polarDeviceInfo.deviceId}")
                binding.tvStatus.text = "Disconnected from ${polarDeviceInfo.deviceId}"
            }

            override fun bleSdkFeatureReady(
                identifier: String,
                feature: PolarBleApi.PolarBleSdkFeature
            ) {
                when (feature) {
                    PolarBleApi.PolarBleSdkFeature.FEATURE_HR -> {
                        Log.d("MyApp", "HR ready")
//                        binding.tvShowData1.text = "HR : $feature"
                    }

                    PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_ONLINE_STREAMING -> {
                        Log.d("MyApp", "FEATURE_POLAR_ONLINE_STREAMING ready")
                        streamECG()
                        streamHR()
                    }

                    else -> {}
                }


                Log.d("MyApp", "Polar BLE SDK feature $feature is ready")
            }

            override fun disInformationReceived(identifier: String, uuid: UUID, value: String) {
                Log.d("MyApp", "DIS INFO uuid: $uuid value: $value")
                if (uuid == UUID.fromString("00002a28-0000-1000-8000-00805f9b34fb")) {
                    val msg = "Firmware: " + value.trim { it <= ' ' }
                    Log.d("MyApp", "Firmware: " + identifier + " " + value.trim { it <= ' ' })
                    binding.tvShowData1.text = msg.trimIndent()
                }
            }

            override fun batteryLevelReceived(identifier: String, level: Int) {
                Log.d("MyApp", "BATTERY LEVEL: $level")
                Log.d("MyApp", "Battery level $identifier $level%")
                val batteryLevelText = "Battery level: $level%"
                binding.tvShowData2.text = batteryLevelText
            }
        })
    }

    fun streamECG() {
        val isDisposed = ecgDisposable?.isDisposed ?: true
        if (isDisposed) {
            ecgDisposable = api.requestStreamSettings(id, PolarBleApi.PolarDeviceDataType.ECG)
                .toFlowable()
                .flatMap { sensorSetting: PolarSensorSetting -> api.startEcgStreaming(id, sensorSetting.maxSettings()) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { polarEcgData: PolarEcgData ->
                        Log.d("MyApp", "ecg update")
                        for (data in polarEcgData.samples) {
                            binding.tvShowData3.text = "ECG : ${((data.voltage.toFloat() / 1000.0).toFloat())}"
                        }
                    },
                    { error: Throwable ->
                        Log.e("MyApp", "Ecg stream failed $error")
                        binding.tvShowData3.text = "Ecg stream failed $error"
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


    fun streamHR() {
        val isDisposed = hrDisposable?.isDisposed ?: true
        if (isDisposed) {
            hrDisposable = api.startHrStreaming(id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { hrData: PolarHrData ->
                        for (sample in hrData.samples) {
                            Log.d("MyApp", "HR " + sample.hr)
                            if (sample.rrsMs.isNotEmpty()) {
                                val rrText = "(${sample.rrsMs.joinToString(separator = "ms, ")}ms)"
                                binding.tvShowData4.text = "HR : $rrText"
                            }

                            binding.tvShowData4.text = "HR : ${sample.hr.toString()}"

                        }
                    },
                    { error: Throwable ->
                        Log.e("MyApp", "HR stream failed. Reason $error")
                        hrDisposable = null
                    },
                    { Log.d("MyApp", "HR stream complete") }
                )
        } else {
            // NOTE stops streaming if it is "running"
            hrDisposable?.dispose()
            hrDisposable = null
        }
    }


}