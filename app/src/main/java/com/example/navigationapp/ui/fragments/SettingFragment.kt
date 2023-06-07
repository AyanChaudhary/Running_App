package com.example.navigationapp.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.navigationapp.R
import com.example.navigationapp.databinding.FragmentSettingBinding
import com.example.navigationapp.other.Constants.KEY_NAME
import com.example.navigationapp.other.Constants.KEY_WEIGHT
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingFragment : Fragment(R.layout.fragment_setting) {

    lateinit var binding:FragmentSettingBinding

    @Inject
    lateinit var sharedPref:SharedPreferences
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding=FragmentSettingBinding.bind(view)
        loadChangesFromSharedPref()
        binding.btnApplyChanges.setOnClickListener {
            val success=applyChangesToSharedPref()
            if(success){
                Snackbar.make(view,"Changes Saved successfully",Snackbar.LENGTH_SHORT).show()
            }else{
                Snackbar.make(requireView(),"Please enter all the fields",Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadChangesFromSharedPref(){
        val name=sharedPref.getString(KEY_NAME,"")
        val weight=sharedPref.getFloat(KEY_WEIGHT,80f)
        binding.etName.setText(name)
        binding.etWeight.setText(weight.toString())
    }

    fun applyChangesToSharedPref():Boolean{
        val name=binding.etName.text.toString()
        val weight=binding.etWeight.text.toString()
        if(name.isEmpty() || weight.isEmpty())return false

        sharedPref.edit()
            .putString(KEY_NAME,name)
            .putFloat(KEY_WEIGHT,weight.toFloat())
            .apply()
        val toolBarText="Let's Go ${name}!"
        requireActivity().findViewById<TextView>(R.id.tvToolbarTitle).text=toolBarText
        return true
    }
}