package com.erdem.designexample.design

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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

        binding.YilBottomSheetButon.setOnClickListener {
            ItemListDialogFragment.newInstance(30).show(parentFragmentManager, "dialog")
        }


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

/*
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
    private fun loadPieChartData(type: String) {
        val entries = ArrayList<PieEntry>()

        when (type) {
            "Devlet" -> {
                entries.add(PieEntry(500f, "1. Sürgün"))
                entries.add(PieEntry(400f, "2. Sürgün"))
                entries.add(PieEntry(300f, "3. Sürgün"))
            }
            "Özel" -> {
                entries.add(PieEntry(300f, "1. Sürgün"))
                entries.add(PieEntry(500f, "2. Sürgün"))
                entries.add(PieEntry(700f, "3. Sürgün"))
            }
            "Tümü" -> {
                entries.add(PieEntry(856f, "1. Sürgün"))
                entries.add(PieEntry(739f, "2. Sürgün"))
                entries.add(PieEntry(793f, "3. Sürgün"))
            }
        }

        val dataSet = PieDataSet(entries, "").apply {
            setDrawIcons(false)
            sliceSpace = 5f
            selectionShift = 2f
            colors = arrayListOf(
                ContextCompat.getColor(requireContext(), R.color.circular_green),
                ContextCompat.getColor(requireContext(), R.color.yellow),
                ContextCompat.getColor(requireContext(), R.color.red)
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

 */

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        parentFragmentManager.setFragmentResultListener("requestKey", viewLifecycleOwner) { _, bundle ->
            val tarih = bundle.getString("tarih")
            binding.YilBottomSheetButon.text = tarih ?: "Varsayılan Değer"
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



}
