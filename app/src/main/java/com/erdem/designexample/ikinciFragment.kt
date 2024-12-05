package com.erdem.designexample

import android.app.DatePickerDialog
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.widget.addTextChangedListener
import com.erdem.designexample.databinding.FragmentIkiniciBinding


class ikinciFragment : Fragment() {

    private var _binding: FragmentIkiniciBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentIkiniciBinding.inflate(inflater, container, false)
        val view = binding.root

        val tarla = TeaGardens("")
        val hasat = TeaHarverst(0,0,0,0,"",0)
        val dbHelper = DatabaseHelper(requireContext())
        /*val dbHelper = DatabaseHelper(requireContext())
        val gardens = DatabaseOperations().readGardens(dbHelper)

        val adapter = ArrayAdapter<String>(requireContext(), R.id.tarlaEditText,)

        dbHelper.close()*/
        /*
            TARİH GİRDİSİ ALINIYOR

            hasat değişkeninin tarih bilgisi burada düzenleniyor

         */

        binding.tarlaEditText.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //Toast.makeText(requireContext(), "asd", Toast.LENGTH_SHORT).show()
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //Toast.makeText(requireContext(), s.toString(), Toast.LENGTH_SHORT).show()
                //val dbHelper = DatabaseHelper(requireContext())
                val tarlalar = DatabaseOperations().readGardens(dbHelper, s.toString()).toTypedArray()
                val adapter = ArrayAdapter<String>(requireContext(), android.R.layout.select_dialog_item,tarlalar )
                binding.tarlaEditText.threshold = 1
                binding.tarlaEditText.setAdapter(adapter)
                Log.e("BAHCE", DatabaseOperations().readGardens(dbHelper, s.toString()).toString())
            }

            override fun afterTextChanged(s: Editable?) {
                //Toast.makeText(requireContext(), "asd", Toast.LENGTH_SHORT).show()
            }

        })


        binding.tarihEditText.setOnClickListener {

            val calendar = Calendar.getInstance()
            val yil = calendar.get(Calendar.YEAR)
            val ay = calendar.get(Calendar.MONTH)
            val gun = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(requireContext(),DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->

                val tarih = "$dayOfMonth/${month+1}/$year"
                binding.tarihEditText.text = tarih

                hasat.day = dayOfMonth
                hasat.month = month+1
                hasat.year = year

                //Toast.makeText(context, binding.textView6.text, Toast.LENGTH_SHORT).show()

            },yil, ay, gun)

            datePickerDialog.setTitle("Tarih seçiniz")
            datePickerDialog.setButton(DialogInterface.BUTTON_POSITIVE, "AYARLA", datePickerDialog)
            datePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "İPTAL", datePickerDialog)

            datePickerDialog.show()
            binding.tarihEditText.setTextColor(Color.GRAY)
        }



        binding.kaydetButton.setOnClickListener {

            val editTexts = listOf(binding.tarlaEditText, binding.tarihEditText, binding.kgEditText, binding.surumEditText)
            var allFieldsFilled = true

            if(binding.tarihEditText.text.toString().trim() == "Tarih seçiniz") {
                Log.e("HATA", "X")
                allFieldsFilled = false
                binding.tarihEditText.setTextColor(Color.RED)
            }
            Log.e("HATA", binding.tarihEditText.text.toString().trim())

            for (editText in editTexts) {
                if (editText.text.toString().trim().isEmpty()) {
                    // Eğer alan boşsa, text rengini kırmızı yap ve allFieldsFilled'ı false olarak ayarla
                    editText.setHintTextColor(Color.RED)
                    allFieldsFilled = false
                } else {
                    // Alan doluysa varsayılan renk ayarını yap
                    editText.setHintTextColor(Color.GRAY) // Varsayılan renge göre düzenleyin
                }
            }

            if (!allFieldsFilled) {
                Toast.makeText(context, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            tarla.gardenName = binding.tarlaEditText.text.toString().trim()
            hasat.season = binding.surumEditText.text.toString().toInt()
            hasat.weight_kg = binding.kgEditText.text.toString().toInt()



            DatabaseOperations().add(dbHelper,tarla,hasat,requireContext())



        }

        binding.silButton.setOnClickListener {
            /*val helper = DatabaseHelper(requireContext())
            DatabaseOperations().deleteData(helper,binding.tarlaEditText.text.toString())*/
        }

        /*
            NEDEN YAPTIĞIMI UNUTTUM ???
         */
        /*binding.kgEditText.addTextChangedListener {
            object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    TODO("Not yet implemented")
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    TODO("Not yet implemented")
                }

                override fun afterTextChanged(s: Editable?) {
                    binding.kaydetButton.isEnabled = true
                }

            }
        }*/


        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



}