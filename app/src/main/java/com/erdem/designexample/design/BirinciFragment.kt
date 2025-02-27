package com.erdem.designexample.design

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.erdem.designexample.R
import com.erdem.designexample.adapter.RaporCardAdapter
import com.erdem.designexample.dataClass.PieChartData
import com.erdem.designexample.database.DatabaseHelper
import com.erdem.designexample.database.DatabaseOperations
import com.erdem.designexample.databinding.FragmentBirinciBinding
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import java.util.Calendar

class birinciFragment : Fragment() {

    private var _binding: FragmentBirinciBinding? = null
    private val binding get() = _binding!!
    private lateinit var dataSet: ArrayList<PieChartData>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBirinciBinding.inflate(inflater, container, false)
        val view = binding.root

        val helper = DatabaseHelper(requireContext())
        var textViewString: String

        val tarih = Calendar.getInstance().get(Calendar.YEAR)

        textViewString = "$tarih yılına ait genel satış raporu"
        dataSet = DatabaseOperations().getPieChartDataAllWithYear(
            helper,
            tarih.toInt()
        )
        setupPieChart()
        loadPieChartData("Tümü", dataSet)

        return view
    }


    // Pasta Grafiği Temel Ayarlarını Yapan Fonksiyon
    private fun setupPieChart() {
        binding.pieChart.apply {
            setUsePercentValues(false)  // Yüzde olarak gösterme
            description.isEnabled = false
            setExtraOffsets(40f, 20f, 40f, 20f)
            setDragDecelerationFrictionCoef(0.95f)
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            setTransparentCircleColor(Color.WHITE)
            setTransparentCircleAlpha(110)
            holeRadius = 20f
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
                for (data in pieData) {
                    entries.add(PieEntry(data.ToplamKg, "${data.season}. Sürgün"))
                }
            }

            "Özel" -> {
                for (data in pieData) {
                    entries.add(PieEntry(data.ToplamKg, "${data.season}. Sürgün"))
                }
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
                ContextCompat.getColor(requireContext(), R.color.pieChart),
                ContextCompat.getColor(requireContext(), R.color.pieChart2),
                ContextCompat.getColor(requireContext(), R.color.pieChart3),
                ContextCompat.getColor(requireContext(), R.color.pieChart4)
            )
            valueTextSize = 12f
            valueTextColor = Color.BLACK
            // **Çizgili Etiketler İçin Ayarlar**
            setDrawValues(true) // Değerleri göster
            xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE // Değerleri dışarı taşı
            yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE // Değerleri dışarı taşı

            // Çizgilerin uzunluğu ve görünümü
            valueLinePart1OffsetPercentage = 80f // Çizgi uzunluğu
            valueLinePart1Length = 0.7f // Çizgi ilk parça uzunluğu
            valueLinePart2Length = 0.7f // Çizgi ikinci parça uzunluğu
            valueLineColor = Color.BLACK // Çizgi rengi
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
        var recyclerViewData = ArrayList<PieChartData>()
        parentFragmentManager.setFragmentResultListener("requestKey", this) { _, bundle ->
            val tarih = bundle.getString("tarih")
            val bahce = bundle.getString("bahce")

            val helper = DatabaseHelper(requireContext())
            var textViewString: String

            textViewString = "$tarih yılına ait genel satış raporu"
            val pieChartData = DatabaseOperations().getPieChartDataAllWithGardenNameAndYear(
                helper,
                tarih!!.toInt(),
                bahce!!
            )
            recyclerViewData = pieChartData
            setupPieChart()
            loadPieChartData("Tümü", pieChartData)

            val customAdapter = RaporCardAdapter(pieChartData)

            //binding.RaporRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            //binding.RaporRecyclerView.adapter = customAdapter

        }
        setupGroupedBarChart(binding.barChart)

    }


    private fun setupGroupedBarChart(barChart: BarChart) {
        val years = listOf("1990", "1991", "1992", "1993", "1994", "1995", "1996", "1997", "1998", "1999", "")

        val companyA = listOf(30f, 40f, 80f, 20f, 45f, 10f, 15f, 30f, 10f, 5f, 0f)
        val companyB = listOf(40f, 5f, 55f, 80f, 70f, 50f, 60f, 45f, 5f, 40f, 0f)
        val companyC = listOf(5f, 55f, 30f, 100f, 85f, 60f, 35f, 40f, 55f, 10f, 0f)

        val entriesCompanyA = ArrayList<BarEntry>()
        val entriesCompanyB = ArrayList<BarEntry>()
        val entriesCompanyC = ArrayList<BarEntry>()
        for (i in years.indices) {
            entriesCompanyA.add(BarEntry(i.toFloat(), companyA[i]))
            entriesCompanyB.add(BarEntry(i.toFloat() + 0.33f, companyB[i]))
            entriesCompanyC.add(BarEntry(i.toFloat() + 0.66f, companyC[i]))
        }

        val dataSetA = BarDataSet(entriesCompanyA, "Company A").apply {
            color = Color.rgb(104, 241, 175) // Yeşil
        }

        val dataSetB = BarDataSet(entriesCompanyB, "Company B").apply {
            color = Color.rgb(164, 228, 251) // Mavi
        }

        val dataSetC = BarDataSet(entriesCompanyC, "Company C").apply {
            color = Color.rgb(255, 210, 140) // Sarı
        }

        val barData = BarData(dataSetA, dataSetB, dataSetC)

        // **Daha iyi grup ayrımı için boşluklar güncellendi**
        val groupSpace = 0.4f
        val barSpace = 0.02f
        val barWidth = 0.2f
        barData.barWidth = barWidth

        barChart.apply {
            data = barData
            description.isEnabled = false
            setFitBars(true)
            legend.isEnabled = true

            xAxis.apply {
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        val index = value.toInt()
                        return if (index in years.indices) years[index] else ""
                    }
                }
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setCenterAxisLabels(true)
            }

            axisLeft.apply {
                axisMinimum = 0f
            }

            axisRight.isEnabled = false

            // **X Ekseni Ayarları**
            xAxis.axisMinimum = 0f
            xAxis.axisMaximum = years.size.toFloat()

            // **Grup Çubuklarını Yerleştir**
            groupBars(0f, groupSpace, barSpace)

            // **Kaydırma Özelliği Aktif Edildi**
            setVisibleXRangeMaximum(5f)  // Aynı anda en fazla 5 yıl gözüksün
            moveViewToX(3f)  // Başlangıçta en sola hizalanmış başlasın

            invalidate()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



}
