package com.example.proctocam

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.sqlite.db.SimpleSQLiteQuery
import com.example.polarecgdata.ActionCallback
import com.example.polarecgdata.ActionCallbackclick
import com.example.polarecgdata.HomeRepository
import com.example.polarecgdata.HomeViewModel
import com.example.polarecgdata.OnItemClick
import com.example.polarecgdata.R
import com.example.polarecgdata.adepters.MainAdepter
import com.example.polarecgdata.databinding.CustomAlertDialogBinding
import com.example.polarecgdata.databinding.FragmentHomeBinding
import com.example.polarecgdata.getCurrentLocalDateTimeWithMillis
import com.example.polarecgdata.timestampToDateTime
import com.example.polarecgdata.toggleStatusBarColor
import com.example.proctocam.Database.DataModel
import com.example.proctocam.Database.DataModelUpdateData
import com.example.proctocam.Database.DatabaseHelper
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
import java.util.concurrent.Executors


class HomeFragment : Fragment(), com.polar.sdk.api.PolarBleApiCallbackProvider {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var viewModel: HomeViewModel
    private lateinit var adapter: MainAdepter
    private lateinit var polarBleApi: PolarBleApi
    private var actionMode: androidx.appcompat.view.ActionMode? = null
    private var actionCallback: ActionCallback? = null
    private var ecgDisposable: Disposable? = null
    private var hrDisposable: Disposable? = null
    private var deDisposable: Disposable? = null
    private lateinit var database: DatabaseHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val factory = activity?.let { HomeRepository(it.applicationContext) }
            ?.let { MyViewModelFactory(it) }
        viewModel = factory?.let { ViewModelProvider(this, it) }?.get(HomeViewModel::class.java)!!
        polarBleApi = PolarBleApiDefaultImpl.defaultImplementation(
            requireActivity().applicationContext,
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
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        database = DatabaseHelper.getInstance(context)!!
        initRv()
        fabClick()
        actionCallback = ActionCallback(requireActivity(), object : ActionCallbackclick {
            override fun onActionItemClickedCallback() {
                deleteInbox()
            }

            override fun onDestroyActionModeCallback() {
                adapter.clearSelection()
                actionMode = null
                toggleStatusBarColor(requireActivity())
            }
        })
    }


    private fun showAddDialog(context: Context) {
        val binding = CustomAlertDialogBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(context)
            .setView(binding.root)
            .setTitle("Add Device") // Replace with your title
            .create()

        binding.btnAdd.setOnClickListener {
            if (TextUtils.isEmpty(binding.etId.text) && TextUtils.isEmpty(
                    binding.etName.text
                )
            ) {
                binding.etId.error = "Enter Device ID"
                binding.etName.error = "Enter Name"
            } else if (TextUtils.equals(
                    "",
                    binding.etName.getText()
                ) && TextUtils.isEmpty(binding.etName.getText())
            ) {
                binding.tlName.error = "Enter Name"
            } else if (TextUtils.equals(
                    "",
                    binding.etId.getText()
                ) && TextUtils.isEmpty(binding.etId.getText())
            ) {
                binding.etId.error = "Enter Id"
            } else {
                val data = DataModel(
                    binding.etId.text.toString(),
                    binding.etName.text.toString(),
                    "",
                    "",
                    "",
                    "",
                    "",
                    ""
                )
                viewModel.insert(data)
                try {
                    adapter.notifyDataSetChanged()
                } catch (ex: Exception) {

                }
                Toast.makeText(context, "Device Added", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }


        }
        dialog.window?.decorView?.setBackgroundResource(R.drawable.dialog_background) // setting the background
        dialog.show()
    }

    private fun fabClick() {
        binding.extendedFab.setOnClickListener {
            context?.let { it1 -> showAddDialog(it1) }
        }
    }

    private fun initRv() {
        val factory = activity?.let { HomeRepository(it.applicationContext) }
            ?.let { MyViewModelFactory(it) }
        viewModel = factory?.let { ViewModelProvider(this, it) }?.get(HomeViewModel::class.java)!!

        binding.rvPatientList.layoutManager = LinearLayoutManager(requireContext())

        adapter = MainAdepter(requireContext(), polarBleApi)
        binding.rvPatientList.adapter = adapter
        adapter.setItemClick(object : OnItemClick {
            override fun onItemClick(
                view: View?,
                inbox: DataModel?,
                position: Int
            ) {
                if (adapter.selectedItemCount() > 0) {
                    toggleActionBar(position)
                } else {
                    try {
                        inbox?.deviceId?.let { polarBleApi.connectToDevice(it) }
                    } catch (e :Exception) {

                    }

                }

            }

            override fun onLongPress(
                view: View?,
                inbox: DataModel?,
                position: Int
            ) {
                toggleActionBar(position)
            }

        })
        activity.let {
            if (it != null) {
                viewModel.allTasks.observe(it) { tasks ->
                    if (tasks.isEmpty()) {
                        binding.rvPatientList.visibility = View.GONE
                        binding.noDataLayout.visibility = View.VISIBLE
                    } else {
                        binding.rvPatientList.visibility = View.VISIBLE
                        binding.noDataLayout.visibility = View.GONE
                        adapter.updateItemAtPosition1(tasks.reversed().toMutableList())
                    }
                }

            }
        }
    }

    class MyViewModelFactory(val repository: HomeRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return modelClass.getConstructor(HomeRepository::class.java).newInstance(repository)
        }
    }

    private fun toggleActionBar(position: Int) {
        if (actionMode == null) {
            actionMode =
                (requireActivity() as AppCompatActivity).startSupportActionMode(actionCallback!!)
        }
        toggleSelection(position)
    }


    private fun toggleSelection(position: Int) {
        adapter.toggleSelection(position)
        val count: Int = adapter.selectedItemCount()
        if (count == 0) {
            actionMode!!.finish()
        } else {
            actionMode!!.title = count.toString()
            actionMode!!.invalidate()
        }
    }


    private fun deleteInbox() {
        val selectedItemPositions: List<Int> = adapter.getSelectedItems()
        for (i in selectedItemPositions.indices.reversed()) {
            adapter.removeItems(selectedItemPositions[i])
        }
        Toast.makeText(
            context,
            "${adapter.getSelectedItems().size} Devices Deleted",
            Toast.LENGTH_SHORT
        ).show()
        adapter.notifyDataSetChanged()
    }

    @SuppressLint("SetTextI18n")
    override fun batteryLevelReceived(identifier: String, level: Int) {
        Log.d("MyApp", "BATTERY LEVEL: $id --- $level")
        Log.d("MyApp", "Battery level $id --- $identifier $level%")
        updateProcedure("battery", "$level%", identifier)

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
                streamHR(identifier)
            }


            PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_ONLINE_STREAMING -> {
                Log.d("MyApp", "FEATURE_POLAR_ONLINE_STREAMING ready")
                streamECG(identifier)

            }

            PolarBleApi.PolarBleSdkFeature.FEATURE_DEVICE_INFO -> {

            }

            else -> {}
        }

        Log.d("MyApp", "Polar BLE SDK feature $feature is ready")
    }

    override fun deviceConnected(polarDeviceInfo: PolarDeviceInfo) {
        Log.d("MyApp", "CONNECTED: $id -- ${polarDeviceInfo.deviceId}")
        updateProcedure("status","Connected", polarDeviceInfo.deviceId)
    }

    override fun deviceConnecting(polarDeviceInfo: PolarDeviceInfo) {
        Log.d("MyApp", "CONNECTING: $id --  ${polarDeviceInfo.deviceId}")
        updateProcedure("status","Connecting", polarDeviceInfo.deviceId)
    }

    override fun deviceDisconnected(polarDeviceInfo: PolarDeviceInfo) {
        Log.d("MyApp", "DISCONNECTED: $id --  ${polarDeviceInfo.deviceId}")
        updateProcedure("status","Disconnected", polarDeviceInfo.deviceId)
    }


    override fun disInformationReceived(identifier: String, uuid: UUID, value: String) {
        Log.d("MyApp", "DIS INFO uuid: $id --  $uuid value: $value")
        if (uuid == UUID.fromString("00002a28-0000-1000-8000-00805f9b34fb")) {
            val msg = "Firmware: " + value.trim { it <= ' ' }
            Log.d("MyApp", "Firmware: " + identifier + " " + value.trim { it <= ' ' })
            updateProcedure("firmware","${value.trim { it <= ' ' }}", identifier)
            polarBleApi.setLocalTime(
                identifier,
                Calendar.getInstance(TimeZone.getDefault()))

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

    private fun streamHR(identifier: String) {
        val isDisposed = hrDisposable?.isDisposed ?: true
        var rrText: String = ""
        if (isDisposed) {
            hrDisposable = polarBleApi.startHrStreaming(identifier)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { hrData: PolarHrData ->
                        for (sample in hrData.samples) {
                            Log.d("MyApp", "HR " + sample.hr)
                            if (sample.rrsMs.isNotEmpty()) {
                                rrText = "(${sample.rrsMs.joinToString(separator = "ms, ")}ms)"
//                                binding.tvHrVal.text = rrText
                            }
                            updateProcedure("hr",sample.hr.toString(), identifier)
                            val executor = Executors.newSingleThreadExecutor()
                            executor.execute {
                                database.dao?.insert1(
                                    DataModelUpdateData(
                                        identifier,
                                        "name",
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

    private fun streamECG(identifier: String) {
        val isDisposed = ecgDisposable?.isDisposed ?: true
        if (isDisposed) {
            ecgDisposable =
                polarBleApi.requestStreamSettings(identifier, PolarBleApi.PolarDeviceDataType.ECG)
                    .toFlowable()
                    .flatMap { sensorSetting: PolarSensorSetting ->
                        polarBleApi.startEcgStreaming(
                            identifier,
                            sensorSetting.maxSettings()
                        )
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ polarEcgData: PolarEcgData ->
                            Log.d("MyApp", "ecg update")
                            for (data in polarEcgData.samples) {
                                updateProcedure("ecg","${((data.voltage.toFloat() / 1000.0).toFloat())}", identifier)
                                val executor = Executors.newSingleThreadExecutor()
                                executor.execute {
                                    database.dao?.insert1(
                                        DataModelUpdateData(
                                            identifier,
                                            "name",
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
                            updateProcedure("ecg","$error", identifier)
                            val executor = Executors.newSingleThreadExecutor()
                            executor.execute {
                                database.dao?.insert1(
                                    DataModelUpdateData(
                                        identifier,
                                        "name",
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

    fun updateProcedure(colName: String, whereVal: String?, id: String?) {
        val s = "UPDATE DataTable SET $colName=? WHERE deviceId LIKE ?"
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            database.dao
                ?.updateData(SimpleSQLiteQuery(s, arrayOf<Any?>(whereVal, id)))
            executor.shutdown()
        }
    }

}


