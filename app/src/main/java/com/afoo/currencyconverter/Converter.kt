package com.afoo.currencyconverter

import android.text.SpannableString
import android.text.style.UnderlineSpan
import androidx.databinding.InverseMethod
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*

object Converter {
//    @JvmStatic
//    fun intToString(value:Int):String{
//        return if (value == 0) "" else value.toString()
//    }
//    @JvmStatic
//    @InverseMethod("intToString")
//    fun stringToInt(value: String):Int{
//        return if (value == "") 0 else value.toInt()
//    }

    @JvmStatic
    fun doubleToString(value: Double): String {
        return if (value == 0.0) "" else value.toString()
    }

    @JvmStatic
    @InverseMethod("doubleToString")
    fun stringToDouble(value: String): Double {
        return if (value == "") 0.0 else value.toDouble()
    }

    @JvmStatic
    fun timestampToString(value: Long):SpannableString{
        val date = Date(value*1000L)
        val df = SimpleDateFormat("HH:mm:ss")
        df.timeZone = TimeZone.getTimeZone("GMT")
        val info = "Mid-market exchange rate at ${df.format(date)} UTC"
        val spannableString = SpannableString(info)
        spannableString.setSpan(UnderlineSpan(),0,info.length,0)
        return spannableString
    }
}