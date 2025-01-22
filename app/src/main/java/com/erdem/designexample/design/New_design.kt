package com.erdem.designexample.design

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.erdem.designexample.adapter.ViewPagerAdapter
import com.erdem.designexample.databinding.FragmentNewDesignBinding
import com.google.android.material.tabs.TabLayoutMediator

class new_design : Fragment() {

    private var _binding: FragmentNewDesignBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNewDesignBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.viewPager.adapter = ViewPagerAdapter(requireActivity())
        TabLayoutMediator(binding.tabLayout, binding.viewPager) {tab, position ->
            when(position) {
                0 -> tab.text = "DEVLET"
                1 -> tab.text = "OZEL"
            }
        }.attach()

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}