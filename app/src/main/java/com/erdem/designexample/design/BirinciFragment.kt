package com.erdem.designexample.design

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.erdem.designexample.adapter.GrafikAdapter
import com.erdem.designexample.dataClass.PieChartData
import com.erdem.designexample.database.DatabaseHelper
import com.erdem.designexample.database.DatabaseOperations
import com.erdem.designexample.databinding.FragmentBirinciBinding

class birinciFragment : Fragment() {

    private var _binding: FragmentBirinciBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBirinciBinding.inflate(inflater, container, false)
        val view = binding.root

        val pieChartDataSet = getPieChartDataFirst()
        binding.GrafikRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.GrafikRecyclerView.adapter = GrafikAdapter(pieChartDataSet, "Tüm Bahçeler")

        return view
    }

    private fun getPieChartDataFirst(): List<List<PieChartData>> {
        val helper = DatabaseHelper(requireContext())
        val pieChartData = DatabaseOperations().getPieChartDataAll(helper).groupBy { it.year }
        helper.close()
        return pieChartData.values.map { it }
    }

    private fun getPieChartDataWithGardenName(gardenName: String): List<List<PieChartData>> {
        val helper = DatabaseHelper(requireContext())
        val pieChartData = DatabaseOperations().getPieChartDataAllWithGardenName(helper, gardenName).groupBy { it.year }
        helper.close()
        return pieChartData.values.map { it }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.FiltreleBottomSheetButon.setOnClickListener {
            ItemListDialogFragment.newInstance(300).show(parentFragmentManager, "dialog")
        }

        parentFragmentManager.setFragmentResultListener("requestKey", this) { _, bundle ->
            val bahce = bundle.getString("bahce")
            val pieChartDataSet = getPieChartDataWithGardenName(bahce!!)
            binding.GrafikRecyclerView.adapter = GrafikAdapter(pieChartDataSet, bahce)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
