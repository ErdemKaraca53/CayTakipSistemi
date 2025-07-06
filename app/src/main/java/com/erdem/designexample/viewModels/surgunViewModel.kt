package com.erdem.designexample.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class surgunViewModel: ViewModel() {

    private var _surgun = MutableLiveData<String>()
    val surgun: LiveData<String> = _surgun

    fun saveSurgun(newTimes: String) {
        _surgun.value = newTimes
    }
}