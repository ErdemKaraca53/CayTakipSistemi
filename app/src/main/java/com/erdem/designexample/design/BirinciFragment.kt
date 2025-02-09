package com.erdem.designexample.design

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.erdem.designexample.databinding.FragmentBirinciBinding
import com.google.android.material.textfield.MaterialAutoCompleteTextView


class birinciFragment : Fragment() {

    private var _binding: FragmentBirinciBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentBirinciBinding.inflate(inflater, container, false)
        val view = binding.root

        val items = arrayOf("2020", "2020", "2020", "2020")
        (binding.YilSecimMenu.editText as? MaterialAutoCompleteTextView)?.setSimpleItems(items)


        return view
    }

}