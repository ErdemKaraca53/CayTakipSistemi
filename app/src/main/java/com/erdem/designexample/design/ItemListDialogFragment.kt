package com.erdem.designexample.design

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.erdem.designexample.R
import com.erdem.designexample.adapter.BahceAdapter
import com.erdem.designexample.adapter.TarihAdapter
import com.erdem.designexample.database.DatabaseHelper
import com.erdem.designexample.database.DatabaseOperations
import com.erdem.designexample.databinding.FragmentItemListDialogListDialogBinding
import com.erdem.designexample.databinding.FragmentItemListDialogListDialogItemBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
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
        TarihDataSet.add("222")
        TarihDataSet.add("222")

        val TarihAdapter = TarihAdapter(TarihDataSet, this)
        binding.TarihSecimRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.TarihSecimRecyclerView.adapter = TarihAdapter
        val BahceDataSet = DatabaseOperations().readGardenName(helper)
        BahceDataSet.add("asdasd")
        BahceDataSet.add("asdasd")
        BahceDataSet.add("asdasd")
        BahceDataSet.add("asdasd")
        BahceDataSet.add("asdasd")
        BahceDataSet.add("asdasd")
        BahceDataSet.add("asdasd")
        BahceDataSet.add("asdasd")
        BahceDataSet.add("asdasd")
        BahceDataSet.add("asdasd")
        BahceDataSet.add("asdasd")
        BahceDataSet.add("asdasd")
        val BahceAdapter = BahceAdapter(BahceDataSet)
        binding.BahceSecimRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.BahceSecimRecyclerView.adapter = BahceAdapter

        helper.close()
    }


    companion object {

        // TODO: Customize parameters
        const val TAG = "ModalBottomSheet"
        fun newInstance(itemCount: Int): ItemListDialogFragment =
            ItemListDialogFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_ITEM_COUNT, itemCount)
                }
            }

    }

    override fun onStart() {
        super.onStart()

        //!! burada dialog nedir tam öğren !!
        dialog?.let {
            val bottomSheet = dialog?.findViewById<View>(R.id.list)
            bottomSheet?.layoutParams?.height = getWindowHeight()
        }
    }

    private fun getWindowHeight() = resources.displayMetrics.heightPixels

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}