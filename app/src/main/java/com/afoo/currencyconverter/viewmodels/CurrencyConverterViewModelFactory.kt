package com.afoo.currencyconverter.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.afoo.currencyconverter.data.ConversionDataRepository

class CurrencyConverterViewModelFactory(
    private val repository: ConversionDataRepository
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CurrencyConverterViewModel(repository) as T
    }
}