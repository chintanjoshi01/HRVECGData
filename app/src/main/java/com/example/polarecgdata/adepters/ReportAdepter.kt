package com.example.polarecgdata.adepters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.polarecgdata.CsvExportWorker
import com.example.polarecgdata.databinding.DataItemBinding
import com.example.proctocam.Database.DataModel
import com.example.proctocam.Database.DatabaseHelper

class ReportAdepter(
    private val context: Context,
    private val dataList: List<DataModel>,
) : RecyclerView.Adapter<ReportAdepter.MyViewHolder>() {

    private lateinit var binding: DataItemBinding
    private lateinit var database: DatabaseHelper
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportAdepter.MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        binding = DataItemBinding.inflate(inflater, parent, false)
        database = DatabaseHelper.getInstance(context)!!
        return MyViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ReportAdepter.MyViewHolder, position: Int) {
        holder.bind(dataList[position])
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private lateinit var task1: DataModel

        @SuppressLint("SetTextI18n")
        fun bind(task: DataModel) {
            task1 = task
            binding.tvIdVal.text = task.deviceId
            binding.tvNameVal.text = task.patientName
            binding.layout1.visibility = View.GONE
            binding.hrEcgLayout.visibility = View.GONE
            binding.hrBFLayout.visibility = View.GONE
            binding.btnConnect.text = "Generate CSV"
            binding.btnConnect.setOnClickListener {
                CsvExportWorker.deviceIddd = task.deviceId.toString()
                val workManager = WorkManager.getInstance(context)
                val pdfGenerationRequest = OneTimeWorkRequestBuilder<CsvExportWorker>()
                    .build()
                workManager.enqueue(pdfGenerationRequest)
            }

        }

    }
}