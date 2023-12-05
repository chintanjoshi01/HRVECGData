package com.example.proctocam.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.polarecgdata.databinding.FragmentReportBinding


class ReportFragment : Fragment() {

    private lateinit var binding: FragmentReportBinding

    //    lateinit var viewModel: ReportViewModel
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

//        initRv()
    }


    /* override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
         inflater.inflate(R.menu.report_menu, menu)
         super.onCreateOptionsMenu(menu, inflater)
     }*/

    /*  private fun initRv() {

          viewModel = ViewModelProvider(this)[ReportViewModel::class.java]
          activity.let {
              if (it != null) {
                  viewModel.getPdfFilesLiveData(requireContext()).observe(it) { tasks ->
                      if (tasks.isEmpty()) {
                          binding.rvPatientList.visibility = View.GONE
                          binding.noDataLayout.visibility = View.VISIBLE
                      } else {
                          binding.rvPatientList.visibility = View.VISIBLE
                          binding.noDataLayout.visibility = View.GONE
                          for (i in tasks.indices) {
                              Log.e("dhkdhas", "task value --> ${tasks[i]}")
                          }
                          val adapter =
                              ReportsAdepter(childFragmentManager, tasks.reversed()) { patient ->
                                  val intent = Intent(activity, PDFViewActivity::class.java)
                                  intent.putExtra(KEY_PDF_FILE_PATH, patient.absolutePath)
                                  activity?.startActivity(intent)
                              }
                          binding.rvPatientList.layoutManager = LinearLayoutManager(requireContext())
                          binding.rvPatientList.adapter = adapter
                      }
                  }
              }
          }
      }*/


}