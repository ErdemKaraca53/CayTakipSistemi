package com.erdem.designexample.design

import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.erdem.designexample.R
import com.erdem.designexample.databinding.FragmentSatisYeriFiltrelemeListDialogItemBinding
import com.erdem.designexample.databinding.FragmentSatisYeriFiltrelemeListDialogBinding

const val ARG_ITEM_COUNT2 = "item_count"

/**
 *
 * A fragment that shows a list of items as a modal bottom sheet.
 *
 * You can show this modal bottom sheet from your activity like this:
 * <pre>
 *    SatisYeriFiltreleme.newInstance(30).show(supportFragmentManager, "dialog")
 * </pre>
 */
class SatisYeriFiltreleme : BottomSheetDialogFragment() {

    private var _binding: FragmentSatisYeriFiltrelemeListDialogBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSatisYeriFiltrelemeListDialogBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        activity?.findViewById<RecyclerView>(R.id.List)?.layoutManager =
            LinearLayoutManager(context)
        activity?.findViewById<RecyclerView>(R.id.TarihBahceList)?.adapter =
            arguments?.getInt(ARG_ITEM_COUNT2)?.let { ItemAdapter(it) }
    }

    private inner class ViewHolder internal constructor(binding: FragmentSatisYeriFiltrelemeListDialogItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        internal val text: TextView = binding.text
    }

    private inner class ItemAdapter internal constructor(private val mItemCount: Int) :
        RecyclerView.Adapter<ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

            return ViewHolder(
                FragmentSatisYeriFiltrelemeListDialogItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
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

        fun newInstance(itemCount: Int): SatisYeriFiltreleme =
            SatisYeriFiltreleme().apply {
                arguments = Bundle().apply {
                    putInt(ARG_ITEM_COUNT2, itemCount)
                }
            }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}