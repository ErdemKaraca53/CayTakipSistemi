package com.erdem.designexample.design

import android.os.Bundle
import android.util.Log
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import com.erdem.designexample.R
import com.erdem.designexample.database.DatabaseHelper
import com.erdem.designexample.database.DatabaseOperations
import com.erdem.designexample.databinding.FragmentKayitFilterDialogListDialogBinding
import com.google.android.material.chip.Chip


class KayitFilterDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentKayitFilterDialogListDialogBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentKayitFilterDialogListDialogBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        activity?.findViewById<RecyclerView>(R.id.list)?.layoutManager =
            LinearLayoutManager(context)

        val helper = DatabaseHelper(requireContext())
        val years = DatabaseOperations().readYear(helper)
        Log.e("years", years.toString())
        if(years.isNotEmpty()) {
            years.forEach { topic ->

                val chip = LayoutInflater.from(context).inflate(R.layout.chip, binding.yilChipGroup, false) as Chip
                chip.id = View.generateViewId()
                chip.text = topic
                chip.isCheckable = true
                chip.isClickable = true

                chip.setOnClickListener {
                    for(i in 0 until binding.yilChipGroup.childCount) {

                        val tmp = binding.yilChipGroup.getChildAt(i)
                        if(tmp != chip && tmp is Chip) {
                            tmp.isChecked = false
                        }
                        if(tmp == chip && tmp is Chip) {
                            tmp.isChecked = true
                        }
                    }
                }

                binding.yilChipGroup.addView(chip)
            }

            val sürgün = ArrayList<String>()
            sürgün.add("1")
            sürgün.add("2")
            sürgün.add("3")
            sürgün.add("4")

            sürgün.forEach { topic ->

                val chip = LayoutInflater.from(context).inflate(R.layout.chip, binding.surgunChipGroup, false) as Chip
                chip.id = View.generateViewId()
                chip.text = topic
                chip.isCheckable = true
                chip.isChecked = topic == "1"
                chip.setOnClickListener {
                    for(i in 0 until binding.surgunChipGroup.childCount) {

                        val tmp = binding.surgunChipGroup.getChildAt(i)
                        if(tmp != chip && tmp is Chip) {
                            tmp.isChecked = false
                        }
                        if(tmp == chip && tmp is Chip) {
                            tmp.isChecked = true
                        }
                    }
                }

                binding.surgunChipGroup.addView(chip)
            }

        }

        helper.close()
    }



    companion object {

        // TODO: Customize parameters
        const val TAG = "ModalBottomSheet"
        fun newInstance(itemCount: Int): KayitFilterDialogFragment =
            KayitFilterDialogFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_ITEM_COUNT, itemCount)
                }
            }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}