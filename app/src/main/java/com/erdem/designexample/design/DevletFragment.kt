package com.erdem.designexample.design

import android.app.DatePickerDialog
import android.content.DialogInterface
import android.graphics.Color
import android.icu.util.Calendar
import android.os.Bundle
import android.text.InputType
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import android.widget.Toast
import com.erdem.designexample.database.DatabaseHelper
import com.erdem.designexample.database.DatabaseOperations
import com.erdem.designexample.database.TeaGardens
import com.erdem.designexample.database.TeaHarverst
import com.erdem.designexample.databinding.FragmentDevletBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DevletFragment : Fragment() {

    private var _binding: FragmentDevletBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDevletBinding.inflate(inflater, container, false)
        val view = binding.root

        val dbHelper = DatabaseHelper(requireContext())
        val calendar = Calendar.getInstance()
        val gun = calendar.get(Calendar.DAY_OF_MONTH)
        val yil = calendar.get(Calendar.YEAR)
        val ay = calendar.get(Calendar.MONTH)

        val hasat = TeaHarverst(yil, 0, 0, 0, "", 0.0f, "DEVLET", 0.0f, "")

        binding.TarihEditText.text = "$gun/${(ay + 1) % 12}/$yil"
        binding.VadeTarihiEdit.text = "31/${(ay + 2) % 12}/$yil"

        binding.TarlaEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                val tarlalar = DatabaseOperations().readGardens(dbHelper, binding.TarlaEditText.text.toString()).toTypedArray()
                val adapter = ArrayAdapter(requireContext(), android.R.layout.select_dialog_item, tarlalar)
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
                binding.VadeTarihiEdit.text = "31/${(month + 2) % 12}/$year"
                hasat.apply {
                    this.year = year
                    this.month = month + 1
                    this.day = dayOfMonth
                }
            }, yil, ay, gun).apply {
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
                VadeTarihi = binding.VadeTarihiEdit.text.toString()
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
