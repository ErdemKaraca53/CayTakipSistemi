package com.erdem.designexample

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.erdem.designexample.databinding.FragmentBirinciBinding


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

        val helper = DatabaseHelper(requireContext())

        val bundle: birinciFragmentArgs by navArgs()
        val year = bundle.year
        val season = bundle.season

        Log.e("HATA", "year: $year, season:$season")

        val dataset = DatabaseOperations().readData(helper,year, season)
        val customAdapter = RvAdapter(dataset)
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = customAdapter

        binding.filterActionButton.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.bottomSheetFragment)
        }

        return view
    }

}