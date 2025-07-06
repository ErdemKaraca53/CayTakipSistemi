package com.erdem.designexample.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.erdem.designexample.R
import com.erdem.designexample.dataClass.kayitRapor
import com.erdem.designexample.dataClass.paymentData


class KayitCardAdapter (val dataSet: ArrayList<kayitRapor>) :
    RecyclerView.Adapter<KayitCardAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val kayıtTarih: TextView
        val kayıtSurgun: TextView
        val kayıtFiyat: TextView
        val KayıtMiktar: TextView

        init {
            // Define click listener for the ViewHolder's View
            kayıtTarih = view.findViewById(R.id.kayit_tarih)
            kayıtSurgun = view.findViewById(R.id.kayit_surgun)
            kayıtFiyat = view.findViewById(R.id.kayit_fiyat)
            KayıtMiktar = view.findViewById(R.id.kayit_kg)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.recycler_view_kayit_card, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        val monthMap = mapOf(
            1 to "Ocak",
            2 to "Şubat",
            3 to "Mart",
            4 to "Nisan",
            5 to "Mayıs",
            6 to "Haziran",
            7 to "Temmuz",
            8 to "Ağustos",
            9 to "Eylül",
            10 to "Ekim",
            11 to "Kasım",
            12 to "Aralık"
        )

        val monthNumber =dataSet[position].ay
        val monthName = monthMap[monthNumber] ?: "Bilinmeyen Ay"

        val tarih = dataSet[position].gun.toString() + " " + monthName + " "  + dataSet[position].tarih.toString()
        val fiyat =dataSet[position].fiyat.toString() + " TL"
        val surgun = dataSet[position].surgun.toString() + ". Surgun"
        val miktar = dataSet[position].kg.toString() + " Kg"

        viewHolder.kayıtFiyat.text = fiyat
        viewHolder.kayıtTarih.text = tarih
        viewHolder.kayıtSurgun.text = surgun
        viewHolder.KayıtMiktar.text = dataSet[position].kg.toString()

    }

    override fun getItemCount() = dataSet.size

    fun updateData(newList: ArrayList<kayitRapor>) {
        dataSet.clear()
        dataSet.addAll(newList)
        notifyDataSetChanged()
    }
}