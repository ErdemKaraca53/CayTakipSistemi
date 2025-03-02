package com.erdem.designexample.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.erdem.designexample.R
import com.erdem.designexample.dataClass.paymentData

class OdemelerCardAdapter (val dataSet: ArrayList<paymentData>) :
    RecyclerView.Adapter<OdemelerCardAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val CompanyName: TextView
        val OdemeDurumu: TextView
        val OdemeMiktari: TextView
        val OdemeTarihi: TextView
        init {
            // Define click listener for the ViewHolder's View
            CompanyName = view.findViewById(R.id.CompanyName)
            OdemeDurumu = view.findViewById(R.id.OdemeDurumu)
            OdemeMiktari = view.findViewById(R.id.OdemeMiktari)
            OdemeTarihi = view.findViewById(R.id.OdemeTarihi)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.recycler_view_card, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        viewHolder.OdemeMiktari.text = dataSet[position].money.toString()
        viewHolder.CompanyName.text = dataSet[position].company
        viewHolder.OdemeTarihi.text = dataSet[position].paymentDate

    }

    override fun getItemCount() = dataSet.size
}