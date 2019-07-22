package com.afoo.currencyconverter.viewmodels

import android.util.Log
import androidx.databinding.Bindable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.afoo.currencyconverter.data.ConversionData
import com.afoo.currencyconverter.data.ConversionDataRepository
import com.github.mikephil.charting.data.Entry
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CurrencyConverterViewModel(val conversionDataRepository: ConversionDataRepository) : ObservableViewModel() {
    var conversionData = conversionDataRepository.getConversionData()
    var currencyFrom: Double = 0.0
        @Bindable get() = field
        set(value) {
            if (field != value) field = value
        }

    val currencyTo = MutableLiveData(0.0)
    val conversionDataTimeStamp = MutableLiveData(conversionData?.timestamp ?: 0L)
    val loading = MutableLiveData(false)
    val is30Days = MutableLiveData(true)
    var conversionDataHistory = Transformations.switchMap(is30Days) {
        conversionDataRepository.getConversionDataHistory(it)
    }

    val chartEntries = MutableLiveData<List<Entry>>()

    var fromSymbol = "EUR"
        @Bindable get() = field
        set(value) {
            if (field != value) field = value
        }

    var toSymbol = "PLN"
        @Bindable get() = field
        set(value) {
            if (field != value) field = value
        }


    fun calculate() {
        if (currencyFrom == 0.0) return
        loading.value = true
        updateConversionData()
    }

    fun convert() {
        if (currencyFrom == 0.0) return
        val rate = conversionData!!.rates
        val toRate = rate[toSymbol]!!
        val fromRate = rate[fromSymbol]!!

        currencyTo.value = (toRate * currencyFrom) / fromRate
        loading.value = false
        setChartData()
    }

    fun day30Click() {
        is30Days.value = true
    }

    fun day60Click() {
        is30Days.value = false
    }

    private fun updateConversionData() {
        val callback = object : Callback<ConversionData> {
            override fun onFailure(call: Call<ConversionData>, t: Throwable) {
                Log.d("Error", t.message)
                if (conversionData != null) {
                    convert()
                }
                loading.value = false
            }

            override fun onResponse(
                call: Call<ConversionData>,
                response: Response<ConversionData>
            ) {
                if (response.isSuccessful) {
                    Log.d("Response", "success")
                    val cd = response.body()
                    if (cd != null) {
                        conversionData = cd
                        conversionDataTimeStamp.value = cd.timestamp
                        convert()
                        Log.d("Response", "Conversion data not null")
                        conversionDataRepository.saveConversionData(cd)
                    }
                }
            }
        }
        conversionDataRepository.downloadConversionData(callback)
    }

    fun setChartData() {
        val entries = mutableListOf<Entry>()
//        conversionDataHistory.forEachIndexed { index, conversionData ->
//            with(conversionData){
//                val data = rates[toSymbol]!! / rates[fromSymbol]!!
//                entries.add(Entry(index.toFloat(),data.toFloat()))
//            }
//        }
        for (i in 0..29) {
            val value = (Math.random() * 30 + 20).toFloat()
            entries.add(Entry(i.toFloat(), value))
        }
        chartEntries.value = entries
    }

    fun seedHistoricalData(){
        conversionDataRepository.seedHistoricalData()
    }
}