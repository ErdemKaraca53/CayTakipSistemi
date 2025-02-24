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
import androidx.recyclerview.widget.LinearLayoutManager
import com.erdem.designexample.R
import com.erdem.designexample.adapter.RaporCardAdapter
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

        //!!!!
        //İLGİLİ VERİ TABANI KODU YAZILDIKTAN SONRA DÜZENLENECEK
        //!!!!

        /*val year = Calendar.getInstance().get(Calendar.YEAR)
        val textString = "$year Yılı Genel Rapor"
        binding.RaporEkranTextView.text = textString
        setupPieChart()

        val helper = DatabaseHelper(requireContext())

        val pieChartData = DatabaseOperations().getPieChartDataAllWithGardenName(helper, year.toInt(), bahce!!)
        setupPieChart()
        loadPieChartData("Tümü", pieChartData)
        Log.e("pieChart", pieChartData.toString())*/

        return view
    }


    // Pasta Grafiği Temel Ayarlarını Yapan Fonksiyon
    private fun setupPieChart() {
        binding.pieChart.apply {
            setUsePercentValues(false)  // Yüzde olarak gösterme
            description.isEnabled = false

            setExtraOffsets(0f, 0f, 0f, 0f)
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
            var textViewString: String

            textViewString = "$tarih yılına ait genel satış raporu"
            val pieChartData = DatabaseOperations().getPieChartDataAllWithGardenName(
                helper,
                tarih!!.toInt(),
                bahce!!
            )
            setupPieChart()
            loadPieChartData("Tümü", pieChartData)

            val customAdapter = RaporCardAdapter(pieChartData)

            binding.RaporRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            binding.RaporRecyclerView.adapter = customAdapter

        }

        var sonSecim = 0

        /*binding.buttonGroup.addOnButtonCheckedListener { buttonGroup, checkedId, isChecked ->

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

        }*/

    }

    private fun updateButtonColorsAndIcon(selectedId: Int) {
        val selectedColor = ContextCompat.getColor(requireContext(), R.color.toggleButton)
        val defaultColor = ContextCompat.getColor(requireContext(), R.color.white)
        val icon = ContextCompat.getDrawable(requireContext(), R.drawable.baseline_check_24)
        /*listOf(
            binding.OzelFiltreButton to R.id.OzelFiltreButton,
            binding.DevletFiltreButton to R.id.DevletFiltreButton,
            binding.TumuFiltreButton to R.id.TumuFiltreButton
        ).forEach { (button, id) ->
            button.setBackgroundColor(if (id == selectedId) selectedColor else defaultColor)
            button.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null)
        }*/
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



}
