package com.erdem.designexample.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.erdem.designexample.R
import com.erdem.designexample.dataClass.PieChartData

class OdemelerCardAdapter () :
    RecyclerView.Adapter<OdemelerCardAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val CompanyName: TextView
        val OdemeDurumu: TextView
        val OdemeMiktari: TextView
        val KalanGunSayisi: TextView
        init {
            // Define click listener for the ViewHolder's View
            CompanyName = view.findViewById(R.id.CompanyName)
            OdemeDurumu = view.findViewById(R.id.OdemeDurumu)
            OdemeMiktari = view.findViewById(R.id.OdemeMiktari)
            KalanGunSayisi = view.findViewById(R.id.KalanGunSayisi)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.recycler_view_card, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {


    }

    override fun getItemCount() = 5
}