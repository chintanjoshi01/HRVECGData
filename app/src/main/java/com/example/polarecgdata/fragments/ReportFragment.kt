package com.example.polarecgdata.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.polarecgdata.adepters.ReportAdepter
import com.example.polarecgdata.databinding.FragmentReportBinding
import com.example.polarecgdata.repositorys.HomeRepository
import com.example.polarecgdata.viewmodel.HomeViewModel
import com.example.polarecgdata.viewmodel.MyViewModelFactory


class ReportFragment : Fragment() {

    private lateinit var binding: FragmentReportBinding
    private lateinit var adapter: ReportAdepter


    lateinit var viewModel: HomeViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRv()
    }


    private fun initRv() {
        adapter = context?.let { it1 -> ReportAdepter(it1) }!!
        binding.rvPatientList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPatientList.adapter = adapter
        val factory = activity?.let { HomeRepository(it.applicationContext) }
            ?.let { MyViewModelFactory(it) }
        viewModel = factory?.let { ViewModelProvider(this, it) }?.get(HomeViewModel::class.java)!!
        activity.let { it2 ->
            activity
            if (it2 != null) {
                viewModel.allReportTasks.observe(it2) { tasks ->
                    if (tasks.isEmpty()) {
                        binding.rvPatientList.visibility = View.GONE
                        binding.noDataLayout.visibility = View.VISIBLE
                    } else {
                        binding.rvPatientList.visibility = View.VISIBLE
                        binding.noDataLayout.visibility = View.GONE
                        adapter.updateItemAtPosition1(tasks.reversed())
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.allReportTasks.removeObservers(this)
    }

    override fun onDetach() {
        super.onDetach()
        viewModel.allReportTasks.removeObservers(this)
    }

}