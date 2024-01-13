package com.example.polarecgdata.fragments

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.otcam.Utils.PermissionUtils
import com.example.polarecgdata.R
import com.example.polarecgdata.adepters.MainAdepter
import com.example.polarecgdata.database.DataModel
import com.example.polarecgdata.database.DatabaseHelper
import com.example.polarecgdata.databinding.CustomAlertDialogBinding
import com.example.polarecgdata.databinding.FragmentHomeBinding
import com.example.polarecgdata.repositorys.HomeRepository
import com.example.polarecgdata.utils.ActionCallback
import com.example.polarecgdata.utils.ActionCallbackclick
import com.example.polarecgdata.utils.ID
import com.example.polarecgdata.utils.NAME
import com.example.polarecgdata.utils.OnItemClick
import com.example.polarecgdata.utils.createAppDirectoryInDoc
import com.example.polarecgdata.utils.dataModel
import com.example.polarecgdata.utils.toggleStatusBarColor
import com.example.polarecgdata.utils.updateProcedureEmpty
import com.example.polarecgdata.viewmodel.HomeViewModel
import com.example.polarecgdata.viewmodel.MyViewModelFactory
import com.polar.sdk.api.PolarBleApi
import com.polar.sdk.api.PolarBleApiDefaultImpl
import io.reactivex.rxjava3.disposables.Disposable
import java.util.concurrent.Executors


class HomeFragment : Fragment() {

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
    private lateinit var dataList: List<DataModel>
    private lateinit var permissionUtils: PermissionUtils

    //    private val contentResolver: ContentResolver = requireContext().contentResolver
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
//        polarBleApi.setApiCallback(this@HomeFragment)
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
        permissionUtils = PermissionUtils(requireActivity())
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
            .create()

        if (TextUtils.equals("Edit Device", this.binding.extendedFab.text)) {
            dialog.setTitle("Edit Device")
            binding.etName.setText(NAME)
            binding.etId.setText(ID)
            binding.btnAdd.text = "Change"
        } else {
            dialog.setTitle("Add Device")
        }

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
                val executor = Executors.newSingleThreadExecutor()
                executor.execute {
                    if (TextUtils.equals("Edit Device", this.binding.extendedFab.text)) {
                        viewModel.delete(dataModel)
                    }
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
                    executor.shutdown()
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
            context?.let { it1 ->
                permissionUtils.grantPermission(object : PermissionUtils.GrantPermissionCallBack {
                    override fun onGranted() {
                        createAppDirectoryInDoc(it1)
                        showAddDialog(it1)
                    }

                    override fun onNotGranted() {

                    }
                })

            }
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
                dataModel: DataModel?,
                position: Int
            ) {
                if (adapter.selectedItemCount() > 0) {
                    toggleActionBar(position)
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
                        binding.extendedFab.text = "Edit Device"
                        dataModel = tasks[0]
                        binding.rvPatientList.visibility = View.VISIBLE
                        binding.noDataLayout.visibility = View.GONE
                        dataList = tasks
                        adapter.updateItemAtPosition1(tasks.reversed().toMutableList())
                    }
                }

            }
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


    override fun onDestroy() {
        updateProcedureEmpty(ID, database)
        super.onDestroy()
    }

}


