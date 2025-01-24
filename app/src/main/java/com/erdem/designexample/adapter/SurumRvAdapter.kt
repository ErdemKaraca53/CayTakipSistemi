package com.erdem.designexample.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.erdem.designexample.R
import com.erdem.designexample.SurumRapor

class SurumRvAdapter(private val dataSet: ArrayList<SurumRapor>) : RecyclerView.Adapter<SurumRvAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val yıl: TextView
        val ToplamKg: TextView
        val ToplamGelir: TextView
        val card : CardView

        init {
            // Define click listener for the ViewHolder's View
            yıl = view.findViewById(R.id.SurumTextView)
            ToplamKg = view.findViewById(R.id.SurumToplamKgTextview)
            ToplamGelir = view.findViewById(R.id.SurumToplamGelirTextView)
            card = view.findViewById(R.id.SurumCardView)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.fragment_surum, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        viewHolder.yıl.text = dataSet[position].surum.toString()
        viewHolder.ToplamKg.text = dataSet[position].toplam_kg.toString() + " KG"
        viewHolder.ToplamGelir.text = "${dataSet[position].toplam_gelir} TL"

        //sürüm sayfasına geçiş yapılacak.
        viewHolder.card.setOnClickListener {
            //Navigation.findNavController(it).navigate(R.id.bottomSheetFragment)
        }
    }

    override fun getItemCount() = dataSet.size

}