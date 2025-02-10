package com.erdem.designexample.design

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.erdem.designexample.adapter.BahceAdapter
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

        val dataset = ArrayList<String>()

        dataset.add("AAAAAA")
        dataset.add("BB")
        dataset.add("CCAA")
        dataset.add("AA")
        dataset.add("BB")
        dataset.add("CCAA")
        dataset.add("AA")
        dataset.add("BB")
        dataset.add("CC")

        val customAdapter = BahceAdapter(dataset)

        binding.bahceSecimRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.bahceSecimRecyclerView.adapter = customAdapter


        return view
    }

}



























