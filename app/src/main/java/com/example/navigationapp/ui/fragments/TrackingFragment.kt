package com.example.navigationapp.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.navigationapp.Model.Run
import com.example.navigationapp.R
import com.example.navigationapp.databinding.FragmentTrackingBinding
import com.example.navigationapp.other.Constants.ACTION_PAUSE_SERVICE
import com.example.navigationapp.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.navigationapp.other.Constants.ACTION_STOP_SERVICE
import com.example.navigationapp.other.Constants.CAMERA_ZOOM
import com.example.navigationapp.other.Constants.POLYLINE_COLOR
import com.example.navigationapp.other.Constants.POLYLINE_WIDTH
import com.example.navigationapp.other.TrackingUtitlity
import com.example.navigationapp.services.TrackingService
import com.example.navigationapp.services.polyline
import com.example.navigationapp.ui.viewmodels.MainViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.round

@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_tracking) {
    private val viewModel : MainViewModel by viewModels()
    private lateinit var binding: FragmentTrackingBinding
    private var isTracking =false
    private var pathPoints= mutableListOf<polyline>()
    private var map : GoogleMap?=null
    private var currTimeInMillis=0L
    private var menu:Menu?=null
    @set:Inject
    var weight=80f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding=FragmentTrackingBinding.bind(view)
        binding.mapView.getMapAsync {
            map=it
            addAllPolyLines()
        }
        binding.btnFinishRun.setOnClickListener {
            zoomToViewFullMap()
            endAndSaveRunToDb()
        }
        binding.mapView?.onCreate(savedInstanceState)
        binding.btnToggleRun.setOnClickListener {
            toggleRun()
        }
        subscribeToObservers()
    }
    private fun subscribeToObservers(){
        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
            updateTracking(it)
        })
        TrackingService.pathPoints.observe(viewLifecycleOwner, Observer {
            pathPoints=it
            addLatestPolyline()
            moveCameraToUser()
        })
        TrackingService.timeRunInMillis.observe(viewLifecycleOwner, Observer {
            currTimeInMillis=it
            val formattedTime=TrackingUtitlity.getFormattedTimeForStopWatch(currTimeInMillis,true)
            binding.tvTimer.text=formattedTime
        })
    }
    private fun toggleRun(){
        if(isTracking){
            menu?.getItem(0)?.isVisible=true
            sendCommandToService(ACTION_PAUSE_SERVICE)
        }else sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_tracking_menu, menu)
        this.menu = menu
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if(currTimeInMillis > 0L) {
            this.menu?.getItem(0)?.isVisible = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.miCancelTracking -> {
                showCancelTrackingDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateTracking(isTracking:Boolean){
        this.isTracking=isTracking
        if(!isTracking){
            binding.btnToggleRun.text="Start"
            binding.btnFinishRun.visibility=View.VISIBLE
        }
        else{
            menu?.getItem(0)?.isVisible=true
            binding.btnToggleRun.text="Stop"
            binding.btnFinishRun.visibility=View.GONE
        }
    }

    private fun moveCameraToUser(){
        if(pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()){
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    CAMERA_ZOOM
                )
            )
        }
    }
    private fun zoomToViewFullMap(){
        val bounds=LatLngBounds.builder()
        for(polyline in pathPoints){
            for(pos in polyline){
                bounds.include(pos)
            }
        }
        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                binding.mapView.width,
                binding.mapView.height,
                (binding.mapView.height*0.05f).toInt()
            )
        )
    }

    private fun endAndSaveRunToDb(){
        map?.snapshot {bmp->
            var totDist=0
            for(polyline in pathPoints){
                totDist+=TrackingUtitlity.calculatePolylineLength(polyline).toInt()
            }
            val avgSpeed= round((totDist/1000f)/(currTimeInMillis/1000f/3600) * 10)/10f
            val dateTimeStamp=Calendar.getInstance().timeInMillis
            val caloriesBurned=((totDist/1000f)*weight).toInt()
            val run= Run(bmp,dateTimeStamp,avgSpeed,totDist,currTimeInMillis,caloriesBurned)
            viewModel.insertRun(run)
            Snackbar.make(requireActivity().findViewById(R.id.rootView),"Run saved successfully",Snackbar.LENGTH_LONG).show()
            stopRun()
        }
    }
    private fun addAllPolyLines(){
        for(polyline in pathPoints ){
            val polyLineOption=PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)
            map?.addPolyline(polyLineOption)
        }
    }
    private fun addLatestPolyline(){
        if(pathPoints.isNotEmpty() && pathPoints.last().size > 1){
            val preLastPathPoint=pathPoints.last()[pathPoints.last().size-2]
            val lastPathPoint=pathPoints.last().last()
            val polyLineOption=PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(preLastPathPoint)
                .add(lastPathPoint)
            map?.addPolyline(polyLineOption)
        }
    }
    private fun sendCommandToService(action:String){
        Intent(requireContext(),TrackingService::class.java).also {
            it.action=action
            requireContext().startService(it)
        }
    }

    override fun onResume() {
        super.onResume()
        binding.mapView?.onResume()
    }

    override fun onStart() {
        super.onStart()
        binding.mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView?.onStop()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView?.onSaveInstanceState(outState)
    }


    private fun showCancelTrackingDialog(){
        val dialog=MaterialAlertDialogBuilder(requireContext(),R.style.AlertDialogTheme)
            .setTitle("Cancel Run")
            .setMessage("Are you sure you want to cancel the run and delete the data?")
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton("Yes"){_,_->
                stopRun()
            }
            .setNegativeButton("No"){ dialogInterface,_->
                dialogInterface.cancel()
            }
            .create()
        dialog.show()
    }

     private fun stopRun(){
         sendCommandToService(ACTION_STOP_SERVICE)
         findNavController().navigate(R.id.action_trackingFragment_to_runningFragment)
    }

}