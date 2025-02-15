package com.erdem.designexample.design

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.erdem.designexample.R
import com.erdem.designexample.adapter.BahceAdapter
import com.erdem.designexample.adapter.TarihAdapter
import com.erdem.designexample.database.DatabaseHelper
import com.erdem.designexample.database.DatabaseOperations
import com.erdem.designexample.databinding.FragmentItemListDialogListDialogBinding
import com.erdem.designexample.databinding.FragmentItemListDialogListDialogItemBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

// TODO: Customize parameter argument names
//Fragment'ı başlatırken kaç adet öğe gösterileceği newInstance(itemCount: Int) metodu ile belirlenir.
const val ARG_ITEM_COUNT = "item_count"

class ItemListDialogFragment : BottomSheetDialogFragment(), TarihAdapter.RecyclerViewEvent {

    private var _binding: FragmentItemListDialogListDialogBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentItemListDialogListDialogBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onItemClick(data: String) {
        val result = Bundle()
        result.putString("tarih", data)
        parentFragmentManager.setFragmentResult("requestKey", result)

        this.dismiss() //BottomSheetFragmentı kapatacak.
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val helper = DatabaseHelper(requireContext())
        //Bu iki satır kod sayesinde bottomSheet içerisindeki recyclerviewler kaydırılabiliyor.
        //Bu satırlar olmadan önce sadece bir tanesi kaydırılabiliyordu
        binding.BahceSecimRecyclerView.isNestedScrollingEnabled = false
        binding.TarihSecimRecyclerView.isNestedScrollingEnabled = false

        val TarihDataSet = DatabaseOperations().readYear(helper)

        val TarihAdapter = TarihAdapter(TarihDataSet, this)
        binding.TarihSecimRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.TarihSecimRecyclerView.adapter = TarihAdapter

        val BahceDataSet = DatabaseOperations().readGardenName(helper)

        val BahceAdapter = BahceAdapter(BahceDataSet)
        binding.BahceSecimRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.BahceSecimRecyclerView.adapter = BahceAdapter

        helper.close()

    //binding.list.adapter = arguments?.getInt(ARG_ITEM_COUNT)?.let { ItemAdapter(it) }

    }

    private inner class ViewHolder internal constructor(binding: FragmentItemListDialogListDialogItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val text: TextView = binding.recyclerViewTextView
    }

    private inner class ItemAdapter(private val mItemCount: Int) :
        RecyclerView.Adapter<ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

            return ViewHolder(
                FragmentItemListDialogListDialogItemBinding.inflate(
                    LayoutInflater.from(
                        parent.context
                    ), parent, false
                )
            )

        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.text.text = position.toString()
        }

        override fun getItemCount(): Int {
            return mItemCount
        }
    }

    companion object {

        // TODO: Customize parameters
        fun newInstance(itemCount: Int): ItemListDialogFragment =
            ItemListDialogFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_ITEM_COUNT, itemCount)
                }
            }

    }

    override fun onStart() {
        super.onStart()
        dialog?.let {
            val bottomSheet = dialog?.findViewById<View>(R.id.list)
            bottomSheet?.layoutParams?.height = 800  // İstediğin yükseklik (px cinsinden)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}