package com.erdem.designexample.design

import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.erdem.designexample.adapter.TarihAdapter
import com.erdem.designexample.database.DatabaseHelper
import com.erdem.designexample.database.DatabaseOperations
import com.erdem.designexample.databinding.FragmentItemListDialogListDialogItemBinding
import com.erdem.designexample.databinding.FragmentItemListDialogListDialogBinding

// TODO: Customize parameter argument names
//Fragment'ı başlatırken kaç adet öğe gösterileceği newInstance(itemCount: Int) metodu ile belirlenir.
const val ARG_ITEM_COUNT = "item_count"

/**
 *
 * A fragment that shows a list of items as a modal bottom sheet.
 *
 * You can show this modal bottom sheet from your activity like this:
 * <pre>
 *    ItemListDialogFragment.newInstance(30).show(supportFragmentManager, "dialog")
 * </pre>
 */
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
        //Toast.makeText(requireContext(), data, Toast.LENGTH_SHORT).show()

        val fragment = birinciFragment()
        val bundle = Bundle()
        bundle.putString("tarih", data)
        fragment.arguments = bundle

        this.dismiss() //BottomSheetFragmentı kapatacak.
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.list.layoutManager = LinearLayoutManager(context)
        val helper = DatabaseHelper(requireContext())
        val bahceDataSet = DatabaseOperations().readYear(helper)

        bahceDataSet.add(0, "TÜMÜ")
        val customAdapter = TarihAdapter(bahceDataSet, this)


        binding.list.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.list.adapter = customAdapter

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}