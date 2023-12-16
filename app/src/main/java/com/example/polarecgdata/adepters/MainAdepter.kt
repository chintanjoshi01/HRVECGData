package com.example.polarecgdata.adepters

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.polarecgdata.database.DataModel
import com.example.polarecgdata.database.DatabaseHelper
import com.example.polarecgdata.databinding.DataItemBinding
import com.example.polarecgdata.utils.ID
import com.example.polarecgdata.utils.NAME
import com.example.polarecgdata.utils.OnItemClick
import com.example.polarecgdata.utils.updateProcedureEmpty
import com.example.polarecgdata.work.DeviceLiveUpdateWorker
import com.example.polarecgdata.work.PolarBleApiSingleton
import com.polar.sdk.api.PolarBleApi
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainAdepter(
    private val context: Context,
    private val api: PolarBleApi,
    private var selectedItems: SparseBooleanArray = SparseBooleanArray()
) : RecyclerView.Adapter<MainAdepter.MyViewHolder>() {
    private lateinit var binding: DataItemBinding
    private lateinit var database: DatabaseHelper
    private var selectedIndex = -1
    private lateinit var itemClick: OnItemClick

    var dataList: MutableList<DataModel> = mutableListOf()
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

    fun updateItemAtPosition(position: Int, newItem: DataModel) {
        dataList[position] = newItem
        notifyItemChanged(position)
    }

    fun updateItemAtPosition1(newItem: MutableList<DataModel>) {
        dataList = newItem
        notifyDataSetChanged()
    }


    inner class MyViewHolder(itemView: DataItemBinding) : RecyclerView.ViewHolder(itemView.root) {
        private lateinit var task1: DataModel
        private var itemBinding: DataItemBinding

        init {
            itemBinding = itemView
        }


        @SuppressLint("SetTextI18n")
        fun bind(task: DataModel) {
            task1 = task
            itemBinding.tvIdVal.text = task.deviceId
            itemBinding.tvNameVal.text = task.patientName
            itemBinding.tvEcgVal.text = task.ecg
            itemBinding.tvBatteryVal.text = task.battery
            itemBinding.tvFamVal.text = task.firmware
            itemBinding.tvHrVal.text = task.hr
            itemBinding.tvStatusVal.text = task.status

            NAME =  itemBinding.tvNameVal.text.toString()
            ID =  itemBinding.tvIdVal.text.toString()
            if (TextUtils.equals("Connected", task.status)) {
                itemBinding.btnConnect.text = "Disconnect"
            }
            itemBinding.btnConnect.setOnClickListener {
                if (TextUtils.equals("Connect", itemBinding.btnConnect.text)) {
                    val workManager = WorkManager.getInstance(context)
                    val data = Data.Builder()
                        .putLong("FIRST_KEY", task.id)
                        .build()
                    val pdfGenerationRequest =
                        OneTimeWorkRequest.Builder(DeviceLiveUpdateWorker::class.java)
                            .setInputData(data)
                            .build()
                    workManager.enqueue(pdfGenerationRequest)
                } else if (TextUtils.equals("Disconnect", itemBinding.btnConnect.text)) {
                    itemBinding.btnConnect.text = "Connect"
                    task.deviceId?.let { it1 -> api.disconnectFromDevice(it1) }
                    updateProcedureEmpty(task.deviceId.toString(),database)
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
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            database.dao?.delete(dataList[position])
            dataList.removeAt(position)
            selectedIndex = -1
            executor.shutdown()
        }

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