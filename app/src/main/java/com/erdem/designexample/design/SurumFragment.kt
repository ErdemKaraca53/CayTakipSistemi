package com.erdem.designexample.design

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.erdem.designexample.R
import com.erdem.designexample.adapter.SurumRvAdapter
import com.erdem.designexample.adapter.YearRvAdapter
import com.erdem.designexample.database.DatabaseHelper
import com.erdem.designexample.database.DatabaseOperations
import com.erdem.designexample.databinding.FragmentSurumBinding

class SurumFragment : Fragment() {

    private var _binding: FragmentSurumBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSurumBinding.inflate(inflater, container, false)
        val view = binding.root

        val helper = DatabaseHelper(requireContext())
        val year = arguments?.getInt("yıl")



        val dataset = DatabaseOperations().GetInfoSeason(helper, year!!)

        val customAdapter = SurumRvAdapter(dataset)
        val recyclerView: RecyclerView = view.findViewById(R.id.SurumRecyclerview)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = customAdapter




        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}