package com.erdem.designexample.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.erdem.designexample.R
import com.erdem.designexample.dataClass.PieChartData
import com.erdem.designexample.design.PieChartManager
import com.github.mikephil.charting.charts.PieChart

class GrafikAdapter(private val dataSet: List<List<PieChartData>>) : RecyclerView.Adapter<GrafikAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val grafik: PieChart = view.findViewById(R.id.grafik)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.recycler_view_grafik, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val pieChartManager = PieChartManager(viewHolder.itemView.context, viewHolder.grafik)
        pieChartManager.setupPieChart()
        pieChartManager.loadPieChartData("Tümü", ArrayList(dataSet[position]))
    }

    override fun getItemCount() = dataSet.size
}
