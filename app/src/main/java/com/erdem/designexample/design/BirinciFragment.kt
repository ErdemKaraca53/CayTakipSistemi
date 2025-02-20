package com.erdem.designexample.design

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.erdem.designexample.R
import com.erdem.designexample.dataClass.PieChartData
import com.erdem.designexample.database.DatabaseHelper
import com.erdem.designexample.database.DatabaseOperations
import com.erdem.designexample.databinding.FragmentBirinciBinding
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter

class birinciFragment : Fragment() {

    private var _binding: FragmentBirinciBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBirinciBinding.inflate(inflater, container, false)
        val view = binding.root

        // Veri tabanından verileri al
        /*val helper = DatabaseHelper(requireContext())
        val bahceDataSet = DatabaseOperations().readGardenName(helper)

        bahceDataSet.add(0, "TÜMÜ")
        val customAdapter = BahceAdapter(bahceDataSet)


        binding.bahceSecimRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.bahceSecimRecyclerView.adapter = customAdapter

        helper.close()

        // Pasta Grafiği Ayarları
        setupPieChart()

        // Varsayılan grafik verisini yükle
        loadPieChartData("Tümü")

        // RadioButton Seçimine Göre Grafik Verisini Güncelle
        binding.radioGroup2.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.DevletRadio -> loadPieChartData("Devlet")
                R.id.ÖzelRadio -> loadPieChartData("Özel")
                R.id.ToplamRadio -> loadPieChartData("Tümü")
            }
        }*/

        return view
    }


    // Pasta Grafiği Temel Ayarlarını Yapan Fonksiyon
    private fun setupPieChart() {
        binding.pieChart.apply {
            setUsePercentValues(false)  // Yüzde olarak gösterme
            description.isEnabled = false
            setExtraOffsets(5f, 10f, 5f, 5f)
            setDragDecelerationFrictionCoef(0.95f)
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            setTransparentCircleColor(Color.WHITE)
            setTransparentCircleAlpha(110)
            holeRadius = 10f
            transparentCircleRadius = 10f
            setDrawCenterText(true)
            setRotationAngle(0f)
            isRotationEnabled = true
            isHighlightPerTapEnabled = true
            animateY(1400, Easing.EaseInOutQuad)
            legend.apply {
                isEnabled = true
                orientation = Legend.LegendOrientation.HORIZONTAL
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                textSize = 15f
            }
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(12f)
        }
    }



    // Seçilen Kategoriye Göre Pasta Grafiğini Güncelleyen Fonksiyon
    private fun loadPieChartData(type: String, pieData: ArrayList<PieChartData>) {
        val entries = ArrayList<PieEntry>()

        when (type) {
            "Devlet" -> {
                entries.add(PieEntry(pieData.get(0).ToplamKg, "1. Sürgün"))
                entries.add(PieEntry(pieData.get(1).ToplamKg, "2. Sürgün"))
                entries.add(PieEntry(pieData.get(2).ToplamKg, "3. Sürgün"))
            }
            "Özel" -> {
                entries.add(PieEntry(pieData.get(0).ToplamKg, "1. Sürgün"))
                entries.add(PieEntry(pieData.get(1).ToplamKg, "2. Sürgün"))
                entries.add(PieEntry(pieData.get(2).ToplamKg, "3. Sürgün"))
            }
            "Tümü" -> {

                for (data in pieData) {
                    entries.add(PieEntry(data.ToplamKg, "${data.season}. Sürgün"))
                }
            }
        }

        val dataSet = PieDataSet(entries, "").apply {
            setDrawIcons(false)
            sliceSpace = 5f
            selectionShift = 2f
            colors = arrayListOf(
                ContextCompat.getColor(requireContext(), R.color.circular_green),
                ContextCompat.getColor(requireContext(), R.color.yellow),
                ContextCompat.getColor(requireContext(), R.color.red),
                ContextCompat.getColor(requireContext(), R.color.radio_gruop)
            )
        }

        val data = PieData(dataSet).apply {
            setValueFormatter(PercentFormatter())
            setValueTextSize(15f)
            setValueTypeface(Typeface.DEFAULT_BOLD)
            setValueTextColor(Color.BLACK)
        }

        binding.pieChart.data = data
        binding.pieChart.highlightValues(null)  // Önceki seçimleri temizle
        binding.pieChart.invalidate()  // Grafiği yeniden çiz
        binding.pieChart.animateY(1400, Easing.EaseInOutQuad)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.YilBottomSheetButon.setOnClickListener {
            ItemListDialogFragment.newInstance(300).show(parentFragmentManager, "dialog")
            // Pasta Grafiği Ayarları

            // Varsayılan grafik verisini yükle
            //loadPieChartData("Tümü", pieChartData)
        }

        parentFragmentManager.setFragmentResultListener("requestKey", this) { _ , bundle ->
            val tarih = bundle.getString("tarih")
            val bahce = bundle.getString("bahce")

            val helper = DatabaseHelper(requireContext())
            val pieChartData = DatabaseOperations().getPieChartData(helper, tarih!!.toInt(), bahce!!)
            setupPieChart()
            loadPieChartData("Tümü", pieChartData)
            Log.e("pieChart", pieChartData.toString())
        }

        var sonSecim = 0

        binding.buttonGroup.addOnButtonCheckedListener { buttonGroup, checkedId, isChecked ->

            if (isChecked) {
                when (checkedId) {
                    R.id.OzelFiltreButton -> {
                        sonSecim = checkedId
                        updateButtonColorsAndIcon(checkedId)
                        Toast.makeText(requireContext(), "özel", Toast.LENGTH_SHORT).show()
                    }
                    R.id.DevletFiltreButton -> {
                        sonSecim = checkedId
                        updateButtonColorsAndIcon(checkedId)
                    }
                    R.id.TumuFiltreButton -> {
                        sonSecim = checkedId
                        updateButtonColorsAndIcon(checkedId)
                    }
                }
            } else {
                if (buttonGroup.checkedButtonId == View.NO_ID) {
                    buttonGroup.check(sonSecim)
                }
            }

        }

    }

    private fun updateButtonColorsAndIcon(selectedId: Int) {
        val selectedColor = ContextCompat.getColor(requireContext(), R.color.toggleButton)
        val defaultColor = ContextCompat.getColor(requireContext(), R.color.white)
        val icon = ContextCompat.getDrawable(requireContext(), R.drawable.baseline_check_24)
        listOf(
            binding.OzelFiltreButton to R.id.OzelFiltreButton,
            binding.DevletFiltreButton to R.id.DevletFiltreButton,
            binding.TumuFiltreButton to R.id.TumuFiltreButton
        ).forEach { (button, id) ->
            button.setBackgroundColor(if (id == selectedId) selectedColor else defaultColor)
            button.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



}
