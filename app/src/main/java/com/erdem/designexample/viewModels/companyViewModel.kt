package com.erdem.designexample.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class companyViewModel: ViewModel() {

    private var _company = MutableLiveData<ArrayList<String>>()
    val company: LiveData<ArrayList<String>> = _company

    fun saveCompany(newCompany: ArrayList<String>) {
        _company.value = newCompany
    }


}