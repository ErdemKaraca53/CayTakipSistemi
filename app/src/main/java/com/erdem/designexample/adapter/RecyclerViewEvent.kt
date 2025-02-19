package com.erdem.designexample.adapter

import com.erdem.designexample.design.ItemType

enum class ItemType {
    BAHCE, TARIH
}

interface RecyclerViewEvent {
    fun onItemClick(data: String, type: ItemType)
}