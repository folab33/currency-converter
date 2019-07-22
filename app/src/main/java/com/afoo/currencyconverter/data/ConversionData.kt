package com.afoo.currencyconverter.data

import java.util.*

data class ConversionData(val timestamp:Long, val base:String, val date: Date, val rates: Map<String,Double>)
