package com.example.polarecgdata.adepters

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.polarecgdata.databinding.DataItemBinding
import com.example.proctocam.Database.DataModel
import com.example.proctocam.Database.DataModelUpdateData
import com.example.proctocam.Database.DatabaseHelper
import com.polar.sdk.api.PolarBleApi
import com.polar.sdk.api.PolarBleApiCallback
import com.polar.sdk.api.errors.PolarInvalidArgument
import com.polar.sdk.api.model.PolarDeviceInfo
import com.polar.sdk.api.model.PolarEcgData
import com.polar.sdk.api.model.PolarHrData
import com.polar.sdk.api.model.PolarSensorSetting
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import java.util.UUID

class MainAdepter(
    private val context: Context,
    private val dataList: List<DataModel>,
    private val api: PolarBleApi
) :
    RecyclerView.Adapter<MainAdepter.MyViewHolder>() {

    private lateinit var binding: DataItemBinding
    private var ecgDisposable: Disposable? = null
    private var hrDisposable: Disposable? = null
    private lateinit var database: DatabaseHelper
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainAdepter.MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        binding = DataItemBinding.inflate(inflater, parent, false)
        database = DatabaseHelper.getInstance(context)!!
        return MyViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: MainAdepter.MyViewHolder, position: Int) {
        holder.bind(dataList[position])
    }

    override fun getItemCount(): Int {
        return dataList.size
    }


    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @SuppressLint("SetTextI18n")
        fun bind(task: DataModel) {
            binding.tvIdVal.text = task.deviceId
            binding.tvNameVal.text = task.patientName
            binding.btnConnect.setOnClickListener {
                task.deviceId?.let { it1 ->    try {
                    api.connectToDevice(it1)
                } catch (a: PolarInvalidArgument) {
                    a.printStackTrace()
                } }
            }

            api.setApiCallback(object : PolarBleApiCallback() {
                override fun blePowerStateChanged(powered: Boolean) {
                    Log.d("MyApp", "BLE power: $powered")
                }

                override fun deviceConnected(polarDeviceInfo: PolarDeviceInfo) {
                    Log.d("MyApp", "CONNECTED: ${polarDeviceInfo.deviceId}")
                    binding.tvStatus.text = "Connected to ${polarDeviceInfo.deviceId}"
                    binding.btnConnect.text = "Connected"

                }

                override fun deviceConnecting(polarDeviceInfo: PolarDeviceInfo) {
                    Log.d("MyApp", "CONNECTING: ${polarDeviceInfo.deviceId}")
                    binding.tvStatus.text = "Connecting to  ${polarDeviceInfo.deviceId}"
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
                            task.deviceId?.let {
                                task.patientName?.let { it1 -> streamECG(it, it1) }
                            }
                            task.deviceId?.let {
                                task.patientName?.let { it1 ->
                                    streamHR(
                                        it,
                                        it1
                                    )
                                }
                            }
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

                    }
                }

                override fun batteryLevelReceived(identifier: String, level: Int) {
                    Log.d("MyApp", "BATTERY LEVEL: $level")
                    Log.d("MyApp", "Battery level $identifier $level%")
                    val batteryLevelText = "Battery level: $level%"
//                    binding.tvShowData2.text = batteryLevelText
                }
            })

        }


    }

    fun streamHR(id: String, name: String) {
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
                                binding.tvHR.text = rrText
                            }

                            binding.tvHR.text = sample.hr.toString()

                        }
                    },
                    { error: Throwable ->
                        Log.e("MyApp", "HR stream failed. Reason $error")
                        hrDisposable = null
                    },
                    {
                        database.dao?.insert1(
                            DataModelUpdateData(
                                id,
                                name,
                                binding.tvEcgVal.text.toString(),
                                binding.tvHR.text.toString(),
                                ""
                            )
                        )
                        Log.d("MyApp", "HR stream complete")
                    }
                )
        } else {
            // NOTE stops streaming if it is "running"
            hrDisposable?.dispose()
            hrDisposable = null
        }
    }

    fun streamECG(id: String, name: String) {
        val isDisposed = ecgDisposable?.isDisposed ?: true
        if (isDisposed) {
            ecgDisposable = api.requestStreamSettings(id, PolarBleApi.PolarDeviceDataType.ECG)
                .toFlowable()
                .flatMap { sensorSetting: PolarSensorSetting ->
                    api.startEcgStreaming(
                        id,
                        sensorSetting.maxSettings()
                    )
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { polarEcgData: PolarEcgData ->
                        Log.d("MyApp", "ecg update")
                        for (data in polarEcgData.samples) {
                            binding.tvEcgVal.text =
                                "${((data.voltage.toFloat() / 1000.0).toFloat())}"
                        }
                    },
                    { error: Throwable ->
                        Log.e("MyApp", "Ecg stream failed $error")
                        binding.tvEcgVal.text = "Ecg stream failed $error"
                        ecgDisposable = null
                    },
                    {
                        database.dao?.insert1(
                            DataModelUpdateData(
                                id,
                                name,
                                binding.tvEcgVal.text.toString(),
                                binding.tvHR.text.toString(),
                                ""
                            )
                        )
                        Log.d("MyApp", "Ecg stream complete")
                    }
                )
        } else {
            // NOTE stops streaming if it is "running"
            ecgDisposable?.dispose()
            ecgDisposable = null
        }
    }
}