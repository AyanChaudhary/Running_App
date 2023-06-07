package com.example.navigationapp.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.navigationapp.R
import com.example.navigationapp.databinding.FragmentSetupBinding
import com.example.navigationapp.other.Constants.KEY_FIRST_TIME_TOGGLE
import com.example.navigationapp.other.Constants.KEY_NAME
import com.example.navigationapp.other.Constants.KEY_WEIGHT
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SetupFragment : Fragment(R.layout.fragment_setup) {
    private lateinit var binding: FragmentSetupBinding

    @Inject
    lateinit var sharedPref:SharedPreferences
    @set:Inject
    var isFirstAppOpen=true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        if(!isFirstAppOpen){
            val navOption=NavOptions.Builder()
                .setPopUpTo(R.id.setupFragment,true)
                .build()
            findNavController().navigate(R.id.action_setupFragment_to_runningFragment,savedInstanceState,navOption)
        }
        binding=FragmentSetupBinding.inflate(layoutInflater,container,false)

        binding.tvContinue.setOnClickListener{
            val success=writePersonalDataToSharedPref()
            if(success){
                findNavController().navigate(R.id.action_setupFragment_to_runningFragment)
            }else{
                Snackbar.make(requireView(),"Please enter all the fields",Snackbar.LENGTH_SHORT).show()
            }

        }
        return binding.root
    }

    private fun writePersonalDataToSharedPref():Boolean{
        val name=binding.etName.text.toString()
        val weight=binding.etWeight.text.toString()

        if(name.isEmpty() || weight.isEmpty()) return false
        sharedPref.edit()
            .putString(KEY_NAME,name)
            .putFloat(KEY_WEIGHT,weight.toFloat())
            .putBoolean(KEY_FIRST_TIME_TOGGLE,false)
            .apply()
        val toolBarText="Let's Go ${name}!"
        requireActivity().findViewById<TextView>(R.id.tvToolbarTitle).text=toolBarText
        return true
    }

}