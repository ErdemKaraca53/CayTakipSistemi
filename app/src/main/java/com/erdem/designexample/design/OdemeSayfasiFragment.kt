package com.erdem.designexample.design

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.erdem.designexample.R
import com.erdem.designexample.adapter.OdemelerCardAdapter
import com.erdem.designexample.database.DatabaseHelper
import com.erdem.designexample.database.DatabaseOperations
import com.erdem.designexample.databinding.FragmentOdemeSayfasiBinding

class OdemeSayfasiFragment : Fragment() {

    private var _binding: FragmentOdemeSayfasiBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentOdemeSayfasiBinding.inflate(inflater, container, false)
        val view = binding.root

        val helper = DatabaseHelper(requireContext())
        DatabaseOperations().getPaymentData(helper)

        val customAdapter = OdemelerCardAdapter()

        binding.OdemeRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.OdemeRecyclerView.adapter = customAdapter


        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}