package com.example.proctocam

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.polarecgdata.HomeRepository
import com.example.polarecgdata.HomeViewModel
import com.example.polarecgdata.R
import com.example.polarecgdata.databinding.CustomAlertDialogBinding
import com.example.polarecgdata.databinding.FragmentHomeBinding
import com.example.proctocam.Database.DataModel


class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    lateinit var viewModel: HomeViewModel
    private var selectedCount = 0

    //    private lateinit var adapter: PatientAdepter
    private lateinit var actionModeCallback: ActionMode.Callback

    var actionMode: androidx.appcompat.view.ActionMode? = null


    //    val menuHost: MenuHost = requireActivity()
    lateinit var menuProvider: MenuProvider
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val factory = activity?.let { HomeRepository(it.applicationContext) }
            ?.let { MyViewModelFactory(it) }
        viewModel = factory?.let { ViewModelProvider(this, it) }?.get(HomeViewModel::class.java)!!
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
        initAppBar()
//        initRv()
        fabClick()
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
                    ""
                )
                viewModel.insert(data)
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

    /*private fun initRv() {
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
                        for (i in tasks.indices) {
                            Log.e("dhkdhas", "task value --> ${tasks[i]}")
                        }
                        val patientData: MutableList<DataModel> = mutableListOf()
                        for (p in tasks) {
                            patientData.add(PatientData(p))
                        }
                        adapter =
                            PatientAdepter(
                                childFragmentManager,
                                patientData.reversed(),
                                { patient ->
                                    val intent = Intent(activity, LiveViewActivity::class.java)
                                    intent.putExtra(KEY_PATIENT, patient.patientsModel)
                                    activity?.startActivity(intent)
                                }, { position ->
                                    if (!adapter.isSelectionMode) {
                                        binding.toolbar.startActionMode(actionModeCallback)

                                    }
                                    adapter.toggleItemSelection(position)
//
                                })
                        binding.rvPatientList.layoutManager = LinearLayoutManager(requireContext())
                        binding.rvPatientList.adapter = adapter
                    }
                }
            }
        }
    }
*/
    class MyViewModelFactory(val repository: HomeRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return modelClass.getConstructor(HomeRepository::class.java).newInstance(repository)
        }
    }


    private fun initAppBar() {



        }
    }

