package com.erdem.designexample.design

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import androidx.core.content.ContextCompat
import com.erdem.designexample.R
import com.erdem.designexample.dataClass.PieChartData
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter

class PieChartManager(private val context: Context, private val pieChart: PieChart) {

    fun setupPieChart() {
        pieChart.apply {
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

    fun loadPieChartData(pieData: ArrayList<PieChartData>) {
        val entries = ArrayList<PieEntry>()

        for (data in pieData) {
            entries.add(PieEntry(data.ToplamKg, "${data.season}. Sürgün"))
        }

        val dataSet = PieDataSet(entries, "").apply {
            setDrawIcons(false)
            sliceSpace = 5f
            selectionShift = 2f
            colors = arrayListOf(
                ContextCompat.getColor(context, R.color.pieChart),
                ContextCompat.getColor(context, R.color.pieChart2),
                ContextCompat.getColor(context, R.color.pieChart3),
                ContextCompat.getColor(context, R.color.pieChart4)
            )
            valueTextSize = 12f
            valueTextColor = Color.BLACK
            setDrawValues(true)
            xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
            yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
            valueLinePart1OffsetPercentage = 80f
            valueLinePart1Length = 0.7f
            valueLinePart2Length = 0.7f
            valueLineColor = Color.BLACK
        }

        val data = PieData(dataSet).apply {
            setValueFormatter(PercentFormatter())
            setValueTextSize(15f)
            setValueTypeface(Typeface.DEFAULT_BOLD)
            setValueTextColor(Color.BLACK)
        }

        pieChart.data = data
        pieChart.highlightValues(null)
        pieChart.invalidate()
        pieChart.animateY(1400, Easing.EaseInOutQuad)
    }
}