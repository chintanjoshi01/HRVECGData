package com.example.polarecgdata.adepters

import android.content.Context
import android.text.TextUtils
import android.util.Log
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.polarecgdata.OnItemClick
import com.example.polarecgdata.PolarBleApiSingleton
import com.example.polarecgdata.databinding.DataItemBinding
import com.example.polarecgdata.timestampToDateTime
import com.example.proctocam.Database.DataModel
import com.example.proctocam.Database.DataModelUpdateData
import com.example.proctocam.Database.DatabaseHelper
import com.polar.sdk.api.PolarBleApi
import com.polar.sdk.api.errors.PolarInvalidArgument
import com.polar.sdk.api.model.PolarDeviceInfo
import com.polar.sdk.api.model.PolarEcgData
import com.polar.sdk.api.model.PolarHrData
import com.polar.sdk.api.model.PolarSensorSetting
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import java.util.Calendar
import java.util.TimeZone
import java.util.UUID

class MainAdepter(
    private val context: Context,
    private val dataList: MutableList<DataModel>,
    private val api: PolarBleApi,
    private var selectedItems: SparseBooleanArray = SparseBooleanArray()
) : RecyclerView.Adapter<MainAdepter.MyViewHolder>() {
    private lateinit var binding: DataItemBinding
    private var ecgDisposable: Disposable? = null
    private var hrDisposable: Disposable? = null
    private var deDisposable: Disposable? = null
    private lateinit var database: DatabaseHelper

    private var selectedIndex = -1
    private lateinit var itemClick: OnItemClick

    fun setItemClick(itemClick: OnItemClick) {
        this.itemClick = itemClick
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainAdepter.MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        binding = DataItemBinding.inflate(inflater, parent, false)
        database = DatabaseHelper.getInstance(context)!!
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MainAdepter.MyViewHolder, position: Int) {
        holder.bind(dataList[position])
    }

    override fun getItemCount(): Int {
        return dataList.size
    }


    internal class ViewHolder(itemView: DataItemBinding) :
        RecyclerView.ViewHolder(itemView.getRoot()) {
        var bi: DataItemBinding

        init {
            bi = itemView
        }
    }

    inner class MyViewHolder(itemView: DataItemBinding) : RecyclerView.ViewHolder(itemView.root),
        com.polar.sdk.api.PolarBleApiCallbackProvider {
        private lateinit var task1: DataModel
        var itemBinding: DataItemBinding

        init {
            itemBinding = itemView
        }

        fun bind(task: DataModel) {
            task1 = task
            itemBinding.tvIdVal.text = task.deviceId
            itemBinding.tvNameVal.text = task.patientName
            lateinit var api: PolarBleApi
            itemBinding.btnConnect.setOnClickListener {
                task.deviceId?.let { it1 ->
                    try {
                        if (TextUtils.equals("Connect", itemBinding.btnConnect.text)) {
                            api = PolarBleApiSingleton.getInstance(context).getPolarBleApi(
                                this,
                                it1
                            )

                        } else {
                            api.disconnectFromDevice(it1)
                        }

                    } catch (a: PolarInvalidArgument) {
                        a.printStackTrace()
                        itemBinding.tvStatusVal.text = "Error"
                    }
                }
            }
            itemView.setOnClickListener { view ->
                itemClick.onItemClick(view, dataList[layoutPosition], layoutPosition)
            }

            itemView.setOnLongClickListener { view ->
                run {
                    itemClick.onLongPress(view, dataList[layoutPosition], layoutPosition)
                    true
                }
            }
            toggleIcon(itemBinding, position)
        }

        override fun batteryLevelReceived(identifier: String, level: Int) {
            Log.d("MyApp", "BATTERY LEVEL: $level")
            Log.d("MyApp", "Battery level $identifier $level%")
            itemBinding.tvBatteryVal.text = "$level%"
        }

        override fun blePowerStateChanged(powered: Boolean) {
            Log.d("MyApp", "BLE power: $powered")
        }

        override fun bleSdkFeatureReady(
            identifier: String,
            feature: PolarBleApi.PolarBleSdkFeature
        ) {
            when (feature) {
                PolarBleApi.PolarBleSdkFeature.FEATURE_HR -> {
                    Log.d("MyApp", "HR ready")
                    task1.deviceId?.let {
                        task1.patientName?.let { it1 ->
                            streamHR(
                                it,
                                it1, itemBinding
                            )
                        }
                    }
                }

                PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_ONLINE_STREAMING -> {
                    Log.d("MyApp", "FEATURE_POLAR_ONLINE_STREAMING ready")
                    task1.deviceId?.let {
                        task1.patientName?.let { it1 -> streamECG(it, it1, itemBinding) }
                    }

                }

                PolarBleApi.PolarBleSdkFeature.FEATURE_DEVICE_INFO -> {

                }

                else -> {}
            }

            Log.d("MyApp", "Polar BLE SDK feature $feature is ready")
        }

        override fun deviceConnected(polarDeviceInfo: PolarDeviceInfo) {
            Log.d("MyApp", "CONNECTED: ${polarDeviceInfo.deviceId}")
            itemBinding.tvStatusVal.text = "Connected"
            itemBinding.btnConnect.text = "Disconnect"
        }

        override fun deviceConnecting(polarDeviceInfo: PolarDeviceInfo) {
            Log.d("MyApp", "CONNECTING: ${polarDeviceInfo.deviceId}")
            itemBinding.tvStatusVal.text = "Connecting"
        }

        override fun deviceDisconnected(polarDeviceInfo: PolarDeviceInfo) {
            Log.d("MyApp", "DISCONNECTED: ${polarDeviceInfo.deviceId}")
            itemBinding.tvStatusVal.text = "Disconnected"
        }

        override fun disInformationReceived(identifier: String, uuid: UUID, value: String) {
            Log.d("MyApp", "DIS INFO uuid: $uuid value: $value")
            if (uuid == UUID.fromString("00002a28-0000-1000-8000-00805f9b34fb")) {
                val msg = "Firmware: " + value.trim { it <= ' ' }
                Log.d("MyApp", "Firmware: " + identifier + " " + value.trim { it <= ' ' })
                itemBinding.tvFamVal.text = value.trim { it <= ' ' }
                api.setLocalTime(identifier, Calendar.getInstance(TimeZone.getDefault()))
                val calendar = api.getLocalTime(identifier)
                calendar.blockingSubscribe {
                    itemBinding.tvTimeVal.text = timestampToDateTime(it.timeInMillis)
                }
            }
        }

        override fun hrFeatureReady(identifier: String) {

        }

        override fun hrNotificationReceived(identifier: String, data: PolarHrData.PolarHrSample) {

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

    fun getDeviceInfo(identifier: String) {
        val isDisposed = deDisposable?.isDisposed ?: true
        if (isDisposed) {

        }
    }

    fun streamHR(id: String, name: String, binding: DataItemBinding) {
        val isDisposed = hrDisposable?.isDisposed ?: true
        lateinit var rrText: String;
        if (isDisposed) {
            hrDisposable = api.startHrStreaming(id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { hrData: PolarHrData ->
                        for (sample in hrData.samples) {
                            Log.d("MyApp", "HR " + sample.hr)
                            if (sample.rrsMs.isNotEmpty()) {
                                rrText = "(${sample.rrsMs.joinToString(separator = "ms, ")}ms)"
//                                binding.tvHrVal.text = rrText
                            }
                            binding.tvHrVal.text = sample.hr.toString()
                            database.dao?.insert1(
                                DataModelUpdateData(
                                    id,
                                    name,
                                    binding.tvEcgVal.text.toString(),
                                    sample.hr.toString(),
                                    rrText,
                                    ""
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
            // NOTE stops streaming if it is "running"
            hrDisposable?.dispose()
            hrDisposable = null
        }
    }

    fun streamECG(id: String, name: String, binding: DataItemBinding) {
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
                            database.dao?.insert1(
                                DataModelUpdateData(
                                    id,
                                    name,
                                    "${((data.voltage.toFloat() / 1000.0).toFloat())}",
                                    binding.tvHrVal.text.toString(),
                                    "",
                                    timestampToDateTime(data.timeStamp)
                                )
                            )
                        }

                    },
                    { error: Throwable ->
                        Log.e("MyApp", "Ecg stream failed $error")
                        binding.tvEcgVal.text = "Ecg stream failed $error"
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


    private fun toggleIcon(bi: DataItemBinding, position: Int) {
        if (selectedItems[position, false]) {
            bi.tvEnterOPDetails.setVisibility(View.VISIBLE)
        } else {
            bi.tvEnterOPDetails.setVisibility(View.GONE)
        }
        if (selectedIndex == position) selectedIndex = -1
    }

    fun getSelectedItems(): List<Int> {
        val items: MutableList<Int> = ArrayList(selectedItems.size())
        for (i in 0 until selectedItems.size()) {
            items.add(selectedItems.keyAt(i))
        }
        return items
    }


    fun removeItems(position: Int) {
        database.dao?.delete(dataList[position])
        dataList.removeAt(position)

        selectedIndex = -1

    }


    fun clearSelection() {
        selectedItems.clear()
        notifyDataSetChanged()
    }


    fun toggleSelection(position: Int) {
        selectedIndex = position
        if (selectedItems[position, false]) {
            selectedItems.delete(position)
        } else {
            selectedItems.put(position, true)
        }
        notifyItemChanged(position)
    }

    fun selectedItemCount(): Int {
        return selectedItems.size()
    }

}