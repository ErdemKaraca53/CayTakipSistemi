package com.erdem.designexample.design.input

import android.R
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.erdem.designexample.database.DatabaseHelper
import com.erdem.designexample.database.DatabaseOperations
import com.erdem.designexample.database.FirebaseSyncHelper
import com.erdem.designexample.database.TeaGardens
import com.erdem.designexample.database.TeaHarverst
import com.erdem.designexample.databinding.FragmentDevletBinding
import com.erdem.designexample.design.clearTextFields
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

class DevletFragment : Fragment() {

    private var _binding: FragmentDevletBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDevletBinding.inflate(inflater, container, false)
        val view = binding.root

        auth = FirebaseAuth.getInstance()

        val dbHelper = DatabaseHelper(requireContext())
        val time = LocalDate.now()
        val gun = time.dayOfMonth
        val yil = time.year
        val ay = time.monthValue

        binding.TarihEditText.text = "$gun/0${(ay)}/$yil"

        var vadeTarihi = LocalDate.now()
        val nextMonth = vadeTarihi.plusMonths(1).monthValue  // Sonraki ayı al
        val year = vadeTarihi.plusMonths(1).year  // Yılı güncelle
        val lastDayOfNextMonth = YearMonth.of(year, nextMonth).atEndOfMonth()
        vadeTarihi = vadeTarihi.withYear(year).withMonth(nextMonth).withDayOfMonth(lastDayOfNextMonth.dayOfMonth)
        val hasat = TeaHarverst(yil, ay, gun, 0, "", 0.0f, "ÇAYKUR", 0.0f, vadeTarihi)

        binding.VadeTarihiEdit.text = "${lastDayOfNextMonth.dayOfMonth}/0${(nextMonth)}/${year}"
        //hasat.VadeTarihi =

        binding.TarlaEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                val tarlalar = DatabaseOperations().readGardens(dbHelper, binding.TarlaEditText.text.toString()).toTypedArray()
                val adapter = ArrayAdapter(requireContext(), R.layout.select_dialog_item, tarlalar)
                binding.TarlaEditText.setAdapter(adapter)
            }
        }

        binding.SurumEditText.setOnClickListener {
            val popupMenu = PopupMenu(requireContext(), binding.SurumEditText)
            (1..4).forEach { popupMenu.menu.add(it.toString()) }
            popupMenu.setOnMenuItemClickListener {
                binding.SurumEditText.setText(it.title)
                true
            }
            popupMenu.show()
        }

        binding.TarihEditText.setOnClickListener {
            DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
                binding.TarihEditText.text = "$dayOfMonth/${month + 1}/$year"

                var vadeTarihi = LocalDate.of(year, month + 1, dayOfMonth)
                val nextMonth = vadeTarihi.plusMonths(1).monthValue  // Sonraki ayı al
                val year = vadeTarihi.plusMonths(1).year  // Yılı güncelle
                val lastDayOfNextMonth = YearMonth.of(year, nextMonth).atEndOfMonth()
                vadeTarihi = vadeTarihi.withYear(year).withMonth(nextMonth)
                    .withDayOfMonth(lastDayOfNextMonth.dayOfMonth)
                Log.e("vadeTarihi", vadeTarihi.toString())

                binding.VadeTarihiEdit.text =
                    "${lastDayOfNextMonth.dayOfMonth}/${(nextMonth)}/${year}"
                hasat.apply {
                    this.year = year
                    this.month = month
                    this.day = dayOfMonth
                    this.VadeTarihi = vadeTarihi
                }
            }, yil, ay - 1, gun).apply {
                setTitle("Tarih seçiniz")
                setButton(DialogInterface.BUTTON_POSITIVE, "AYARLA", this)
                setButton(DialogInterface.BUTTON_NEGATIVE, "İPTAL", this)
                show()
            }
        }

        binding.KaydetButton.setOnClickListener {
            val editTexts = listOf(
                binding.TarlaEditText, binding.KgEditText,
                binding.SurumEditText, binding.FiyatEdit
            )

            if (editTexts.any { it.text.toString().trim().isEmpty() }) {
                Toast.makeText(context, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val tarla = TeaGardens(binding.TarlaEditText.text.toString().trim())
            hasat.apply {
                season = binding.SurumEditText.text.toString().toInt()
                weight_kg = binding.KgEditText.text.toString().toFloat()
                //VadeTarihi = binding.VadeTarihiEdit.text.toString()
                SatisFiyati = binding.FiyatEdit.text.toString().toFloat()
            }

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Son kayıt kontrolü")
                .setMessage(
                    "Yıl: ${hasat.year}\nBahçe: ${tarla.gardenName}\n" +
                            "Sürüm: ${hasat.season}\nKg: ${hasat.weight_kg}\n" +
                            "Satış Fiyatı: ${hasat.SatisFiyati}\nVade Tarihi: ${hasat.VadeTarihi}"
                )
                .setNegativeButton("Reddet") { _, _ ->
                    Toast.makeText(requireContext(), "Reddedildi", Toast.LENGTH_SHORT).show()
                }
                .setPositiveButton("Kabul et") { _, _ ->
                    DatabaseOperations().add(dbHelper, tarla, hasat, requireContext())
                    clearTextFields(view)
                    binding.SurumEditText.text = ""
                    Toast.makeText(requireContext(), "Kaydedildi", Toast.LENGTH_SHORT).show()

                    val currentUser = auth.currentUser
                    if (currentUser != null) {
                        val syncHelper = FirebaseSyncHelper(requireContext())
                        // ✅ Coroutine ile Firestore'dan SQLite'a veri çekme işlemini arka planda çalıştır
                        CoroutineScope(Dispatchers.IO).launch {
                            syncHelper.backupSQLiteToFirestore()
                        }
                    }

                }
                .show()



        }
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}