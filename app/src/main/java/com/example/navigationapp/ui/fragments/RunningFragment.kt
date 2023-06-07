package com.example.navigationapp.ui.fragments

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.example.navigationapp.Adapters.RunAdapter
import com.example.navigationapp.R
import com.example.navigationapp.databinding.FragmentRunningBinding
import com.example.navigationapp.other.SortType
import com.example.navigationapp.other.TrackingUtitlity
import com.example.navigationapp.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

@AndroidEntryPoint
class RunningFragment : Fragment(R.layout.fragment_running), EasyPermissions.PermissionCallbacks {

    private val viewModel : MainViewModel by viewModels()
    lateinit var runAdapter: RunAdapter
    private lateinit var binding: FragmentRunningBinding
    override fun onCreateView(inflater: LayoutInflater,container: ViewGroup?,savedInstanceState: Bundle?): View? {
        binding=FragmentRunningBinding.inflate(layoutInflater,container,false)
        requestPermissions()
        setUpRecyclerView()
        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_runningFragment_to_trackingFragment)
        }

        when(viewModel.sortType){
            SortType.DATE->binding.spFilter.setSelection(0)
            SortType.RUNNING_TIME->binding.spFilter.setSelection(1)
            SortType.DISTANCE->binding.spFilter.setSelection(2)
            SortType.AVG_SPEED->binding.spFilter.setSelection(3)
            SortType.CALORIES_BURNED->binding.spFilter.setSelection(4)
        }

        binding.spFilter.onItemSelectedListener=object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when(position){
                    0->viewModel.sortRuns(SortType.DATE)
                    1->viewModel.sortRuns(SortType.RUNNING_TIME)
                    2->viewModel.sortRuns(SortType.DISTANCE)
                    3->viewModel.sortRuns(SortType.AVG_SPEED)
                    4->viewModel.sortRuns(SortType.CALORIES_BURNED)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
        viewModel.runs.observe(viewLifecycleOwner, Observer {
            runAdapter.submitList(it)
        })

        return binding.root
    }

    fun setUpRecyclerView()=binding.rvRuns.apply {
        runAdapter= RunAdapter()
        adapter=runAdapter
        layoutManager=LinearLayoutManager(requireContext())
    }

    private fun requestPermissions(){
        if(TrackingUtitlity.hasLocationPermissions(requireContext())) return

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
            EasyPermissions.requestPermissions(
                this,
                "Please accept location permissions to run the app",
                0,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }
        else{
            EasyPermissions.requestPermissions(
                this,
                "Please accept location permissions to run the app",
                0,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        TODO("Not yet implemented")
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if(EasyPermissions.somePermissionPermanentlyDenied(this,perms)){
            AppSettingsDialog.Builder(this).build().show()
        }
        else requestPermissions()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,this)
    }
}