package com.erdem.designexample.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.erdem.designexample.R
import com.erdem.designexample.dataClass.SurumRapor

class SurumRvAdapter(private val dataSet: ArrayList<SurumRapor>) : RecyclerView.Adapter<SurumRvAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val Surum: TextView
        val ToplamKg: TextView
        val ToplamGelir: TextView
        val card : CardView
        val DetayButton : Button
        init {
            // Define click listener for the ViewHolder's View
            Surum = view.findViewById(R.id.SurumTextView)
            ToplamKg = view.findViewById(R.id.SurumToplamKgTextview)
            ToplamGelir = view.findViewById(R.id.SurumToplamGelirTextView)
            card = view.findViewById(R.id.SurumCardView)
            DetayButton = view.findViewById(R.id.SurumDetayButton)
        }
    }



    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.surum_card_view, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        viewHolder.Surum.text = "${dataSet[position].surum}. sürgün"
        viewHolder.ToplamKg.text = dataSet[position].toplam_kg.toString() + " KG"
        viewHolder.ToplamGelir.text = "${dataSet[position].toplam_gelir} TL"

        if (position == dataSet.lastIndex) {
            viewHolder.itemView.visibility = GONE
        }

        //sürüm sayfasına geçiş yapılacak.
        viewHolder.card.setOnClickListener {
            val bundle = Bundle()
            bundle.putInt("yıl", dataSet[position].year)
            bundle.putInt("surum", dataSet[position].surum)
            //Navigation.findNavController(it).navigate(R.id.action_surumFragment_to_bahceFragment, bundle)
        }

        viewHolder.DetayButton.setOnClickListener {
            val bundle = Bundle()
            bundle.putInt("yıl", dataSet[position].year)
            bundle.putInt("surum", dataSet[position].surum)
            //Navigation.findNavController(it).navigate(R.id.action_surumFragment_to_bahceFragment, bundle)
        }

    }

    override fun getItemCount() = dataSet.size

}