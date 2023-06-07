package com.example.navigationapp.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.navigationapp.R
import com.example.navigationapp.databinding.ActivityMainBinding
import com.example.navigationapp.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        navigateToTrackingFragmentIfNeeded(intent)
        val navFragment=supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController=navFragment.navController
        binding.bottomNavView.setupWithNavController(navController)
        binding.bottomNavView.setOnNavigationItemReselectedListener { /**/ }

        navController.addOnDestinationChangedListener{ _ , destination , _ ->
            when(destination.id){
                R.id.runningFragment,R.id.statisticsFragment,R.id.settingFragment ->{
                    binding.bottomNavView.visibility= View.VISIBLE
                }
                else ->{
                    binding.bottomNavView.visibility= View.GONE
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToTrackingFragmentIfNeeded(intent)
    }
    private fun navigateToTrackingFragmentIfNeeded(intent: Intent?){
        if(intent?.action==ACTION_SHOW_TRACKING_FRAGMENT){
            val navFragment=supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
            val navController=navFragment.navController
            navController.navigate(R.id.action_global_trackingFragment)
        }
    }
}