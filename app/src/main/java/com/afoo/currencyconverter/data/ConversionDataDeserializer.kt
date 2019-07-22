package com.afoo.currencyconverter.data

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class ConversionDataDeserializer : JsonDeserializer<Map<String,Double>> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ):Map<String,Double> {
        val rate = json!!.asJsonObject
        val map = mutableMapOf<String,Double>()
        rate.keySet().forEach {
            map[it] = rate[it].asDouble
        }
        return map
    }

}