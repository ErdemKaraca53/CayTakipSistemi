package com.erdem.designexample.design

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.erdem.designexample.R
import com.erdem.designexample.adapter.BahceRvAdapter
import com.erdem.designexample.database.DatabaseHelper
import com.erdem.designexample.database.DatabaseOperations
import com.erdem.designexample.databinding.FragmentBahceBinding


class BahceFragment : Fragment() {

    private var _binding: FragmentBahceBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBahceBinding.inflate(inflater, container, false)
        val view = binding.root

        val year = arguments?.getInt("yıl")
        val surum = arguments?.getInt("surum")

        val helper = DatabaseHelper(requireContext())

        val dataset = DatabaseOperations().GetInfoGarden(helper,year!!,surum!!)
        val customAdapter = BahceRvAdapter(dataset)
        binding.BahceRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        binding.BahceRecyclerview.adapter = customAdapter

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}