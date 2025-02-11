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
import com.github.mikephil.charting.components.Legend
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

        // Pasta grafiğinde yüzdelik değerlerin kullanılmasını sağlıyoruz.
        binding.pieChart.setUsePercentValues(true)
        // Grafik açıklamasını devre dışı bırakıyoruz (alt kısımda ekstra bilgi göstermemesi için).
        binding.pieChart.description.isEnabled = false

        // Grafiğin dış kenarlarına ekstra boşluk (padding) ekleyerek daha dengeli bir görünüm sağlıyoruz.
        // Sırasıyla: Sol, Üst, Sağ, Alt kenar boşlukları (pixel cinsinden float değerleri).
        binding.pieChart.setExtraOffsets(5f, 10f, 5f, 5f)

        // Pasta grafiğinde sürükleme (drag) eyleminin hızını yavaşlatan sürtünme katsayısını belirliyoruz.
        // 0 ile 1 arasında bir değer alır. 1'e yaklaştıkça grafik daha akıcı hareket eder, 0'a yaklaştıkça hızlı durur.
        binding.pieChart.setDragDecelerationFrictionCoef(0.95f)

        // on below line we are setting hole
        // and hole color for pie chart
        binding.pieChart.isDrawHoleEnabled = true
        binding.pieChart.setHoleColor(Color.WHITE)

        // Pasta grafiğinin merkezinde bulunan şeffaf dairenin rengini beyaz olarak ayarlıyoruz.
        binding.pieChart.setTransparentCircleColor(Color.WHITE)

        // Şeffaf dairenin opaklık (saydamlık) seviyesini belirliyoruz (0-255 arasında bir değer alır).
        // 0 tamamen şeffaf, 255 tamamen opak olur. 110 değeri orta seviyede bir şeffaflık sağlar.
        binding.pieChart.setTransparentCircleAlpha(110)


        // Pasta grafiğinin merkezindeki boşluk (delik) yarıçapını belirliyoruz (0-100 arası değer alır).
        // 0 değeri merkezi tamamen kapatır, 100 değeri grafiğin tamamını boş yapar.
        binding.pieChart.holeRadius = 10f

        // Şeffaf dairenin yarıçapını belirliyoruz (holeRadius'tan büyük olmalıdır).
        // Bu, merkezde delik etrafında hafif saydam bir halka oluşturur.
        binding.pieChart.transparentCircleRadius = 10f

        // Pasta grafiğinin merkezinde metin gösterilip gösterilmeyeceğini belirliyoruz.
        // true olarak ayarlandığında, merkezde belirlenen bir yazı gösterilir.
        binding.pieChart.setDrawCenterText(true)
        //binding.pieChart.centerText = "Grafik"


        // Pasta grafiğinin başlangıç açısını belirliyoruz (derece cinsinden).
        // 0 derece grafiği standart konumda başlatır, 150f ile biraz döndürerek başlatıyoruz.
        binding.pieChart.setRotationAngle(0f)
        // enable rotation of the pieChart by touch
        // Kullanıcının pasta grafiğini dokunarak döndürebilmesini sağlıyoruz.
        binding.pieChart.isRotationEnabled = true

        // Kullanıcı bir dilime dokunduğunda, o dilimi vurg
        binding.pieChart.isHighlightPerTapEnabled = true

        // Pasta grafiğine dikey eksende (Y ekseni) 1400 milisaniyelik bir animasyon ekliyoruz.
        // Easing.EaseInOutQuad ile animasyonu daha yumuşak bir geçişle başlatıp bitiriyoruz.
        binding.pieChart.animateY(1400, Easing.EaseInOutQuad)


        // on below line we are disabling our legend for pie chart
        binding.pieChart.legend.isEnabled = false
        binding.pieChart.setEntryLabelColor(Color.WHITE)
        binding.pieChart.setEntryLabelTextSize(12f)

        // Pasta grafiği için veri noktalarını tutacak bir ArrayList oluşturuyoruz.
        val entries: ArrayList<PieEntry> = ArrayList()

        // Grafikte gösterilecek dilimlere veri ekliyoruz.
        // Her PieEntry, grafikte bir dilimi temsil eder ve değeri yüzde oranını belirtir.
        entries.add(PieEntry(70f,"1. Sürgün")) // %70'lik dilim
        entries.add(PieEntry(20f, "2. Sürgün")) // %20'lik dilim
        entries.add(PieEntry(10f, "3. Sürgün")) // %10'luk dilim

        binding.pieChart.setEntryLabelColor(Color.BLACK)

        // on below line we are setting pie data set
        val dataSet = PieDataSet(entries, "")
        binding.pieChart.legend.isEnabled = true
        binding.pieChart.legend.orientation = Legend.LegendOrientation.HORIZONTAL
        binding.pieChart.legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        binding.pieChart.legend.textSize = 15f


        // on below line we are setting icons.
        dataSet.setDrawIcons(false)
        // on below line we are setting slice for pie
        // Pasta grafiğindeki dilimler arasına boşluk ekliyoruz (5f piksel).
        // Bu, dilimler arasındaki ayrımı daha belirgin hale getirir.
        dataSet.sliceSpace = 5f

        // Eğer ikonlar kullanılıyorsa, ikonların pozisyonunu (offset) ayarlıyoruz.
        // Burada ikonlar yukarıdan 40f birim aşağıya kaydırılıyor.
        dataSet.iconsOffset = MPPointF(0f, 40f)

        // Seçilen dilimi vurgulamak için büyüklüğünü 5f birim artırıyoruz.
        // Kullanıcı bir dilime tıkladığında, bu dilim diğerlerinden biraz dışarı taşar.
        dataSet.selectionShift = 2f

        // add a lot of colors to list
        val colors: ArrayList<Int> = ArrayList()
        colors.add(ContextCompat.getColor(requireContext(),R.color.circular_green))
        colors.add(ContextCompat.getColor(requireContext(),R.color.yellow))
        colors.add(ContextCompat.getColor(requireContext(),R.color.red))

        // Pasta grafiğinin dilimleri için renkleri ayarlıyoruz.
        // "colors" isimli liste daha önce tanımlanmış olmalı ve farklı renkler içermelidir.
        dataSet.colors = colors

        // PieData nesnesini oluşturuyoruz ve veri setimizi (dataSet) bağlıyoruz.
        // Bu, pasta grafiğine veri atamamızı sağlar.
        val data = PieData(dataSet)

        // Pasta grafiğindeki değerleri yüzdelik formatta göstermek için bir formatlayıcı ekliyoruz.
        data.setValueFormatter(PercentFormatter())

        // Dilimlerin üzerindeki yazıların boyutunu ayarlıyoruz (15sp).
        data.setValueTextSize(15f)

        // Dilim üzerindeki yazıları kalın hale getiriyoruz.
        data.setValueTypeface(Typeface.DEFAULT_BOLD)

        // Dilim üzerindeki yazıların rengini beyaz yapıyoruz.
        data.setValueTextColor(Color.BLACK)

        // Oluşturduğumuz veriyi pasta grafiğine atıyoruz.
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



























