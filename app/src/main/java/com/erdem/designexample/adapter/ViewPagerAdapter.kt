package com.erdem.designexample.adapter

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.erdem.designexample.design.DevletFragment
import com.erdem.designexample.design.OzelFragment
import com.erdem.designexample.design.bottomSheetFragment
import com.erdem.designexample.design.new_design

class ViewPagerAdapter(fragment: FragmentActivity) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when(position) {

            0 ->
                  DevletFragment()
            1 ->
                OzelFragment()

            else -> throw IllegalStateException("Invalid position: $position")
        }
    }
}