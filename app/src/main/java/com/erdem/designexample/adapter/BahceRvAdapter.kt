package com.erdem.designexample.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.erdem.designexample.R
import com.erdem.designexample.dataClass.BahceRapor

class BahceRvAdapter(val dataSet: ArrayList<BahceRapor>) : RecyclerView.Adapter<BahceRvAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val BahceIsmi: TextView
        val ToplamKg: TextView
        val ToplamGelir: TextView
        val card : CardView
        init {
            // Define click listener for the ViewHolder's View
            BahceIsmi = view.findViewById(R.id.BahceIsmiTextView)
            ToplamKg = view.findViewById(R.id.BahceToplamKgTextView)
            ToplamGelir = view.findViewById(R.id.BahceToplamGelirTextView)
            card = view.findViewById(R.id.BahceCardView)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.bahce_card_view, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        viewHolder.BahceIsmi.text = dataSet[position].bahceIsmi.toString()
        viewHolder.ToplamKg.text = dataSet[position].ToplamKg.toString() + " KG"
        viewHolder.ToplamGelir.text = "${dataSet[position].ToplamGelir} TL"

        if (position == dataSet.lastIndex) {
            viewHolder.itemView.visibility = GONE
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}