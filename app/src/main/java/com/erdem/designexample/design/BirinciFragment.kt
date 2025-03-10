package com.erdem.designexample.design

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.erdem.designexample.R
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBirinciBinding.inflate(inflater, container, false)
        val view = binding.root

        val pieChartDataSet = getPieChartFirstData()

        setupPieChart()
        loadPieChartData("Tümü", pieChartDataSet)

        val barChartDataSet = getBarChartFirstData()

        var years = ArrayList<String>()
        val sezon1 = ArrayList<Float>()
        val sezon2 = ArrayList<Float>()
        val sezon3 = ArrayList<Float>()
        val sezon4 = ArrayList<Float>()

        for (i in 0..4) {
            sezon1.add(0f)
            sezon2.add(0f)
            sezon3.add(0f)
            sezon4.add(0f)
        }

        var sayac = 0
        /**map olduğu için for döngüsü ile erişiyoruz. **/
        Log.e("pieChart", "BarChart: $barChartDataSet")
        for ((year, seasonList) in barChartDataSet) {
            Log.e("pieChart","Yıl: $year")
            years.add("$year")
            for (seasonData in seasonList) {
                Log.e("pieChart","Yıl: ${seasonData.season}")
                when(seasonData.season) {
                    1-> sezon1.add(sayac,seasonData.ToplamKg)
                    2-> sezon2.add(sayac,seasonData.ToplamKg)
                    3-> sezon3.add(sayac,seasonData.ToplamKg)
                    4-> sezon4.add(sayac,seasonData.ToplamKg)
                }
            }
            sayac++
        }
        val tarih = Calendar.getInstance().get(Calendar.YEAR)
        val GenelRaporText = "Tüm yıllar / Tüm bahçeler"
        val YilRaporText = "$tarih / Tüm bahçeler"
        binding.GenelRaporBilgiTextView.text = GenelRaporText
        binding.YilRaporTextView.text = YilRaporText
        setupGroupedBarChart(binding.barChart, years, sezon1, sezon2, sezon3, sezon4)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.FiltreleBottomSheetButon.setOnClickListener {
            ItemListDialogFragment.newInstance(300).show(parentFragmentManager, "dialog")
            // Pasta Grafiği Ayarları

            // Varsayılan grafik verisini yükle
            //loadPieChartData("Tümü", pieChartData)
        }

        parentFragmentManager.setFragmentResultListener("requestKey", this) { _, bundle ->
            val tarih = bundle.getString("tarih")
            val bahce = bundle.getString("bahce")

            val RaporText = "Tüm yıllar / $bahce"
            val YilRaporText = "$tarih / $bahce"
            binding.GenelRaporBilgiTextView.text = RaporText
            binding.YilRaporTextView.text = YilRaporText
            setUpPieChartDataWithYearAndGardenName(tarih, bahce)
            setUpBarChartDataWitGardenName(tarih, bahce)
        }



    }

    fun setUpPieChartDataWithYearAndGardenName(tarih: String?, bahce: String?){

        val helper = DatabaseHelper(requireContext())
        var textViewString: String

        textViewString = "$tarih yılına ait genel satış raporu"
        val pieChartData = DatabaseOperations().getPieChartDataAllWithGardenNameAndYear(
            helper,
            tarih!!.toInt(),
            bahce!!
        )
        helper.close()
        setupPieChart()
        loadPieChartData("Tümü", pieChartData)
    }

    fun setUpBarChartDataWitGardenName(tarih: String?, bahce: String?){

        val helper = DatabaseHelper(requireContext())
        var textViewString: String

        textViewString = "$tarih yılına ait genel satış raporu"
        val barChartData = DatabaseOperations().getPieChartDataAllWithGardenName(
            helper,
            bahce!!
        )
        helper.close()
        val barChartDataSet = barChartData.groupBy { it.year }
        var years = ArrayList<String>()
        val sezon1 = ArrayList<Float>()
        val sezon2 = ArrayList<Float>()
        val sezon3 = ArrayList<Float>()
        val sezon4 = ArrayList<Float>()

        for (i in 0..3) {
            sezon1.add(0f)
            sezon2.add(0f)
            sezon3.add(0f)
            sezon4.add(0f)
        }

        var sayac = 0
        /**map olduğu için for döngüsü ile erişiyoruz. **/
        Log.e("pieChart", "BarChart1: $barChartDataSet")
        for ((year, seasonList) in barChartDataSet) {
            Log.e("pieChart","Yıl: $year")
            years.add("$year")
            for (seasonData in seasonList) {
                Log.e("pieChart","Yıl: ${seasonData.season}")
                when(seasonData.season) {
                    1-> sezon1.add(sayac,seasonData.ToplamKg)
                    2-> sezon2.add(sayac,seasonData.ToplamKg)
                    3-> sezon3.add(sayac,seasonData.ToplamKg)
                    4-> sezon4.add(sayac,seasonData.ToplamKg)
                }
            }
            sayac++
        }

        setupGroupedBarChart(binding.barChart, years, sezon1, sezon2, sezon3, sezon4)
    }


    fun getPieChartFirstData() :  ArrayList<PieChartData>{
        val helper = DatabaseHelper(requireContext())
        var textViewString: String

        val tarih = Calendar.getInstance().get(Calendar.YEAR)

        textViewString = "$tarih yılına ait genel satış raporu"
        val dataSet = DatabaseOperations().getPieChartDataAllWithYear(
            helper,
            tarih.toInt()
        )
        helper.close()
        return dataSet
    }

    fun getBarChartFirstData(): Map<Int, List<PieChartData>> {
        val helper = DatabaseHelper(requireContext())
        var textViewString: String

        val tarih = Calendar.getInstance().get(Calendar.YEAR)

        textViewString = "$tarih yılına ait genel satış raporu"
        val dataSet = DatabaseOperations().getPieChartDataAll(
            helper,
        )

        helper.close()
        return dataSet.groupBy { it.year }
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

    private fun setupGroupedBarChart(
        barChart: BarChart,
        years: List<String>,
        sezon1: List<Float>,
        sezon2: List<Float>,
        sezon3: List<Float>,
        sezon4: List<Float>
    ) {
        val entriesSezon1 = createEntries(sezon1, 0f)    // İlk çubuk
        val entriesSezon2 = createEntries(sezon2, 0.2f)  // Hafif sağa kaydır
        val entriesSezon3 = createEntries(sezon3, 0.4f)  // Daha sağa kaydır
        val entriesSezon4 = createEntries(sezon4, 0.6f)  // En sağa kaydır

        val dataSetA = createBarDataSet(entriesSezon1, "Sezon 1", Color.rgb(104, 241, 175))
        val dataSetB = createBarDataSet(entriesSezon2, "Sezon 2", Color.rgb(164, 228, 251))
        val dataSetC = createBarDataSet(entriesSezon3, "Sezon 3", Color.rgb(255, 210, 140))
        val dataSetD = createBarDataSet(entriesSezon4, "Sezon 4", Color.rgb(100, 0, 140))

        val barData = BarData(dataSetA, dataSetB, dataSetC, dataSetD)

        // **0 olan değerlere yazı yazılmasını engelleme**
        barData.setValueFormatter(object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return if (value == 0f) "0" else value.toString() // 0 olanları boş göster
            }
        })

        // **Grup Çubukları için Boşluk Ayarları**
        val groupSpace = 0.04f   // Gruplar arası boşluk
        val barSpace = 0.03f    // Çubuklar arası boşluk
        val barWidth = 0.21f    // Çubuk genişliği (daha iyi hizalama için küçültüldü)
        barData.barWidth = barWidth

        configureBarChart(barChart, barData, years, groupSpace, barSpace)
    }



    /** Çubuk verilerini oluşturur **/
    private fun createEntries(data: List<Float>, offset: Float): ArrayList<BarEntry> {
        return ArrayList<BarEntry>().apply {
            for (i in data.indices) {
                //if(data[i] != 0f) {
                    add(BarEntry(i.toFloat() + offset, data[i])) // Çubukları kaydır
                //}
            }
        }
    }

    /** BarDataSet oluşturur **/
    private fun createBarDataSet(entries: List<BarEntry>, label: String, color: Int): BarDataSet {
        return BarDataSet(entries, label).apply {
            this.color = color
            valueTextSize = 12f
            valueTextColor = Color.BLACK
        }
    }

    /** BarChart konfigürasyonu **/
    private fun configureBarChart(barChart: BarChart, barData: BarData, years: List<String>, groupSpace: Float, barSpace: Float) {
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
                position = XAxis.XAxisPosition.TOP
                granularity = 1f
                setCenterAxisLabels(true) // X ekseni için daha iyi hizalama
            }

            axisLeft.axisMinimum = 0f
            axisRight.isEnabled = false

            // **X Ekseni Ayarları**
            xAxis.axisMinimum = 0f
            xAxis.axisMaximum = years.size.toFloat() + groupSpace // X eksenini genişlet

            // **Grupları Düzenli Bir Şekilde Yerleştir**
            groupBars(0f, groupSpace, barSpace)

            // **Kaydırma Özelliği Aktif Edildi**
            setVisibleXRangeMaximum(2f)  // Aynı anda en fazla 5 yıl gözüksün
            moveViewToX(0f) // Başlangıçta en soldan başlamasını sağla

            invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



}
