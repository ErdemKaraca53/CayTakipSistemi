package com.erdem.designexample.adapter

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.erdem.designexample.R
import com.erdem.designexample.dataClass.kayitRapor
import com.erdem.designexample.database.DatabaseHelper
import com.erdem.designexample.database.DatabaseOperations
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class KayitCardAdapter (val dataSet: ArrayList<kayitRapor>,
                        private val helper: DatabaseHelper,private val context: Context) :
    RecyclerView.Adapter<KayitCardAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val kayıtTarih: TextView
        val kayıtSurgun: TextView
        val kayıtFiyat: TextView
        val kayıtMiktar: TextView
        val silButon: TextView

        init {
            // Define click listener for the ViewHolder's View
            kayıtTarih = view.findViewById(R.id.kayit_tarih)
            kayıtSurgun = view.findViewById(R.id.kayit_surgun)
            kayıtFiyat = view.findViewById(R.id.kayit_fiyat)
            kayıtMiktar = view.findViewById(R.id.kayit_kg)
            silButon = view.findViewById(R.id.KayitSil)
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

        val tarih = dataSet[position].gun.toString() + " " + monthName + " "  + dataSet[position].yil.toString()
        val fiyat =dataSet[position].fiyat.toString() + " TL"
        val surgun = dataSet[position].surgun.toString() + ". Surgun"
        val miktar = dataSet[position].kg.toString() + " Kg"

        viewHolder.kayıtFiyat.text = fiyat
        viewHolder.kayıtTarih.text = tarih
        viewHolder.kayıtSurgun.text = surgun
        viewHolder.kayıtMiktar.text = dataSet[position].kg.toString()
        viewHolder.silButon.setOnClickListener {
            val id = DatabaseOperations().getId(helper, dataSet[position])
            if (id != -1) {
                val builder =MaterialAlertDialogBuilder(viewHolder.itemView.context)
                builder.setTitle("Kayıt Silme")
                builder.setMessage("Bu kaydı silmek istediğinize eminmisiniz?")

                builder.setPositiveButton("Evet") { dialog, _ ->
                    DatabaseOperations().deleteData(helper, id)

                    // Listeyi güncelle
                    dataSet.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, dataSet.size)

                    Toast.makeText(viewHolder.itemView.context, "Kayıt Silindi", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }

                builder.setNegativeButton("Hayır") { dialog, _ ->
                    dialog.dismiss() // sadece kapat
                }

                builder.create().show()
            } else {
                Log.e("cardAdapter", "Kayıt bulunamadı")
            }
        }

    }

    fun showSyncConfirmationDialog(context: Context, helper: DatabaseHelper, id: Int) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Senkron Durumu")
        builder.setMessage("Bu kaydın senkronizasyon durumunu 2 yapmak istiyor musunuz?")

        builder.setPositiveButton("Evet") { dialog, _ ->
            DatabaseOperations().deleteData(helper, id)
            Toast.makeText(context, "Durum güncellendi", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        builder.setNegativeButton("Hayır") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }


    override fun getItemCount() = dataSet.size

    fun updateData(newList: ArrayList<kayitRapor>) {
        dataSet.clear()
        dataSet.addAll(newList)
        notifyDataSetChanged()
    }
}