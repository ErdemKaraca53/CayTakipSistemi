package com.erdem.designexample.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.erdem.designexample.R
import com.erdem.designexample.dataClass.paymentData
import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.concurrent.TimeUnit

class OdemelerCardAdapter (val dataSet: ArrayList<paymentData>) :
    RecyclerView.Adapter<OdemelerCardAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val CompanyName: TextView
        val OdemeDurumu: TextView
        val OdemeMiktari: TextView
        val OdemeTarihi: TextView
        val KalanTarih: TextView
        val Kg: TextView
        val Fiyat: TextView
        init {
            // Define click listener for the ViewHolder's View
            CompanyName = view.findViewById(R.id.CompanyName)
            OdemeDurumu = view.findViewById(R.id.OdemeDurumu)
            OdemeMiktari = view.findViewById(R.id.OdemeMiktari)
            OdemeTarihi = view.findViewById(R.id.OdemeTarihi)
            KalanTarih = view.findViewById(R.id.kalanTarih)
            Kg = view.findViewById(R.id.KgMiktari)
            Fiyat = view.findViewById(R.id.SatisFiyati)
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

        val kgText = "${dataSet[position].Kg} Kg"
        val fiyatText = "${dataSet[position].satisFiyati} TL"

        viewHolder.Kg.text = kgText
        viewHolder.Fiyat.text = fiyatText
        val today = LocalDate.now()
        val vadeTarihi = dataSet[position].paymentDate

        val kalanGun =  ChronoUnit.DAYS.between(today, vadeTarihi)

        val kalanTarih = "Kalan gün: $kalanGun"

        if (kalanGun > 0) {
            viewHolder.KalanTarih.text = kalanTarih
            viewHolder.OdemeDurumu.text = "Odeme tarihi gelmedi"
        } else {
            val context = viewHolder.itemView.context
            viewHolder.OdemeDurumu.text = "Odeme $vadeTarihi'de yapılmış"
            viewHolder.OdemeDurumu.setTextColor(ContextCompat.getColor(context, R.color.purple_500))
        }

    }

    override fun getItemCount() = dataSet.size
}