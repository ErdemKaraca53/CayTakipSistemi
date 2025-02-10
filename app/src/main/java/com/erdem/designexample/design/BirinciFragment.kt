package com.erdem.designexample.design

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.erdem.designexample.adapter.BahceAdapter
import com.erdem.designexample.database.DatabaseHelper
import com.erdem.designexample.database.DatabaseOperations
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

        val helper = DatabaseHelper(requireContext())
        val bahceDataSet = DatabaseOperations().readGardenName(helper)

        bahceDataSet.add(0,"TÜMÜ")
        val customAdapter = BahceAdapter(bahceDataSet)

        binding.bahceSecimRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.bahceSecimRecyclerView.adapter = customAdapter

        helper.close()


        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}



























