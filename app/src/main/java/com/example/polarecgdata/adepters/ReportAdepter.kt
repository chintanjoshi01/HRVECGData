package com.example.polarecgdata.adepters

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.otcam.Utils.PermissionUtils
import com.example.polarecgdata.database.DatabaseHelper
import com.example.polarecgdata.databinding.DataItemBinding
import com.example.polarecgdata.utils.DataReportModel
import com.example.polarecgdata.utils.OnItemClick
import com.example.polarecgdata.work.CsvExportWorker

class ReportAdepter(
    private val context: Activity,
) : RecyclerView.Adapter<ReportAdepter.MyViewHolder>() {

    private var dataList: List<DataReportModel> = mutableListOf()
    private lateinit var binding: DataItemBinding
    private lateinit var database: DatabaseHelper
    private var selectedIndex = -1
    private lateinit var itemClick: OnItemClick
    private lateinit var permissionUtils: PermissionUtils


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportAdepter.MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        binding = DataItemBinding.inflate(inflater, parent, false)
        database = DatabaseHelper.getInstance(context)!!
        permissionUtils = PermissionUtils(context)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReportAdepter.MyViewHolder, position: Int) {
        holder.bind(dataList[position])
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    fun updateItemAtPosition1(newItem: List<DataReportModel>) {
        dataList = newItem
        notifyDataSetChanged()
    }

    inner class MyViewHolder(itemView: DataItemBinding) : RecyclerView.ViewHolder(itemView.root) {
        private var binding: DataItemBinding

        init {
            binding = itemView
        }


        @SuppressLint("SetTextI18n")
        fun bind(task: DataReportModel) {
            binding.tvIdVal.text = task.id
            binding.tvNameVal.text = task.name
            binding.layout1.visibility = View.GONE
            binding.hrEcgLayout.visibility = View.GONE
            binding.hrBFLayout.visibility = View.GONE
            binding.hrTimeLayout.visibility = View.GONE
            binding.btnConnect.text = "Generate CSV"
            binding.btnConnect.setOnClickListener {

                permissionUtils.grantPermission(object : PermissionUtils.GrantPermissionCallBack {
                    override fun onGranted() {
                        CsvExportWorker.deviceIddd = task.id.toString()
                        val workManager = WorkManager.getInstance(context)
                        val pdfGenerationRequest = OneTimeWorkRequestBuilder<CsvExportWorker>()
                            .build()
                        workManager.enqueue(pdfGenerationRequest)
                        workManager.getWorkInfoByIdLiveData(pdfGenerationRequest.id)
                            .observeForever { workInfo ->
                                val progress = workInfo?.progress?.getInt("progress", 0) ?: 0
                                when (workInfo.state) {
                                    WorkInfo.State.ENQUEUED -> {
                                        binding.progressCircular.visibility = View.VISIBLE
                                        binding.btnConnect.visibility = View.GONE
                                    }

                                    WorkInfo.State.RUNNING -> {
                                        binding.progressCircular.visibility = View.VISIBLE
                                        binding.btnConnect.visibility = View.GONE
                                    }

                                    WorkInfo.State.SUCCEEDED -> {
                                        binding.progressCircular.visibility = View.GONE
                                        binding.btnConnect.visibility = View.VISIBLE

                                    }

                                    WorkInfo.State.FAILED -> {
                                        binding.progressCircular.visibility = View.GONE
                                        binding.btnConnect.visibility = View.VISIBLE
                                    }

                                    WorkInfo.State.CANCELLED -> {
                                        binding.progressCircular.visibility = View.GONE
                                        binding.btnConnect.visibility = View.VISIBLE
                                    }
                                    else -> {

                                    }
                                }
                            }
                    }

                    override fun onNotGranted() {

                    }

                })

            }

        }


    }
}