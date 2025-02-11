package com.erdem.designexample.design

import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.erdem.designexample.R
import com.erdem.designexample.adapter.BahceAdapter
import com.erdem.designexample.database.DatabaseHelper
import com.erdem.designexample.database.DatabaseOperations
import com.erdem.designexample.databinding.FragmentBirinciBinding
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.MPPointF
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


        // on below line we are setting user percent value,
        // setting description as enabled and offset for pie chart
        binding.pieChart.setUsePercentValues(true)
        binding.pieChart.description.isEnabled = false
        binding.pieChart.setExtraOffsets(5f, 10f, 5f, 5f)

        // on below line we are setting drag for our pie chart
        binding.pieChart.setDragDecelerationFrictionCoef(0.95f)

        // on below line we are setting hole
        // and hole color for pie chart
        binding.pieChart.isDrawHoleEnabled = true
        binding.pieChart.setHoleColor(Color.WHITE)

        // on below line we are setting circle color and alpha
        binding.pieChart.setTransparentCircleColor(Color.WHITE)
        binding.pieChart.setTransparentCircleAlpha(110)

        // on  below line we are setting hole radius
        binding.pieChart.holeRadius = 58f
        binding.pieChart.transparentCircleRadius = 61f

        // on below line we are setting center text
        binding.pieChart.setDrawCenterText(true)

        // on below line we are setting
        // rotation for our pie chart
        binding.pieChart.setRotationAngle(0f)

        // enable rotation of the pieChart by touch
        binding.pieChart.isRotationEnabled = true
        binding.pieChart.isHighlightPerTapEnabled = true

        // on below line we are setting animation for our pie chart
        binding.pieChart.animateY(1400, Easing.EaseInOutQuad)

        // on below line we are disabling our legend for pie chart
        binding.pieChart.legend.isEnabled = false
        binding.pieChart.setEntryLabelColor(Color.WHITE)
        binding.pieChart.setEntryLabelTextSize(12f)

        // on below line we are creating array list and
        // adding data to it to display in pie chart
        val entries: ArrayList<PieEntry> = ArrayList()
        entries.add(PieEntry(70f))
        entries.add(PieEntry(20f))
        entries.add(PieEntry(10f))

        // on below line we are setting pie data set
        val dataSet = PieDataSet(entries, "Mobile OS")

        // on below line we are setting icons.
        dataSet.setDrawIcons(false)

        // on below line we are setting slice for pie
        dataSet.sliceSpace = 3f
        dataSet.iconsOffset = MPPointF(0f, 40f)
        dataSet.selectionShift = 5f

        // add a lot of colors to list
        val colors: ArrayList<Int> = ArrayList()
        colors.add(ContextCompat.getColor(requireContext(),R.color.purple_200))
        colors.add(ContextCompat.getColor(requireContext(),R.color.yellow))
        colors.add(ContextCompat.getColor(requireContext(),R.color.red))

        // on below line we are setting colors.
        dataSet.colors = colors

        // on below line we are setting pie data set
        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter())
        data.setValueTextSize(15f)
        data.setValueTypeface(Typeface.DEFAULT_BOLD)
        data.setValueTextColor(Color.WHITE)
        binding.pieChart.setData(data)

        // undo all highlights
        binding.pieChart.highlightValues(null)

        // loading chart
        binding.pieChart.invalidate()



        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}



























