package com.erdem.designexample.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class tarihViewModel: ViewModel() {

    private var _times = MutableLiveData<ArrayList<String>>()
    val times: LiveData<ArrayList<String>> = _times

    fun saveTimes(newTimes: ArrayList<String>) {
        _times.value = newTimes
    }

}