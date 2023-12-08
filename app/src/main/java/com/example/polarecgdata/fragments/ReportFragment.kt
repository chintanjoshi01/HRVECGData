package com.example.proctocam.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.polarecgdata.HomeRepository
import com.example.polarecgdata.HomeViewModel
import com.example.polarecgdata.adepters.ReportAdepter
import com.example.polarecgdata.databinding.FragmentReportBinding


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
        val factory = activity?.let { HomeRepository(it.applicationContext) }
            ?.let { MyViewModelFactory(it) }
        viewModel = factory?.let { ViewModelProvider(this, it) }?.get(HomeViewModel::class.java)!!
        activity.let {
            if (it != null) {
                viewModel.allTasks.observe(it) { tasks ->
                    if (tasks.isEmpty()) {
                        binding.rvPatientList.visibility = View.GONE
                        binding.noDataLayout.visibility = View.VISIBLE
                    } else {
                        binding.rvPatientList.visibility = View.VISIBLE
                        binding.noDataLayout.visibility = View.GONE
                        adapter =
                            ReportAdepter(requireContext(), tasks.reversed())
                        binding.rvPatientList.layoutManager = LinearLayoutManager(requireContext())
                        binding.rvPatientList.adapter = adapter
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

}