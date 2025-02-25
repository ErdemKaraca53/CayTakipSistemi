package com.erdem.designexample.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.erdem.designexample.R
import androidx.core.view.isGone
import com.erdem.designexample.dataClass.PieChartData

class RaporCardAdapter (private val dataSet: ArrayList<PieChartData>) :
    RecyclerView.Adapter<RaporCardAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView
        init {
            // Define click listener for the ViewHolder's View
            textView = view.findViewById(R.id.RaporRecyclerViewTextView)
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

        val textViewString = "$toplamKg kg | $toplamGelir TL"
        viewHolder.textView.text = textViewString
        viewHolder.textView.setOnClickListener {
        }
    }

    override fun getItemCount() = dataSet.size
}