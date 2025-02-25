package com.erdem.designexample.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.erdem.designexample.R
import com.erdem.designexample.dataClass.PieChartData

class RaporCardAdapter (private val dataSet: ArrayList<PieChartData>) :
    RecyclerView.Adapter<RaporCardAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val Title: TextView
        val rapor1: TextView
        val rapor2: TextView
        val rapor3: TextView
        init {
            // Define click listener for the ViewHolder's View
            Title = view.findViewById(R.id.RecyclerViewBaslik)
            rapor1 = view.findViewById(R.id.Surgun1Rapor)
            rapor2 = view.findViewById(R.id.Surgun2Rapor)
            rapor3 = view.findViewById(R.id.Surgun3Rapor)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.recycler_view_card, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        val season = dataSet[position].season
        val toplamKg = dataSet[position].ToplamKg.toInt()
        val toplamGelir = dataSet[position].ToplamGelir.toInt()

        val textViewString = "$toplamKg KG | $toplamGelir TL"
        viewHolder.rapor1.text = textViewString
        viewHolder.rapor2.text = textViewString
        viewHolder.rapor3.text = textViewString
    }

    override fun getItemCount() = dataSet.size
}