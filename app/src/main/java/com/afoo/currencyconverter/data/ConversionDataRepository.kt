package com.afoo.currencyconverter.data

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.afoo.currencyconverter.accessKey
import com.afoo.currencyconverter.currencyArray
import com.afoo.currencyconverter.simpleDateFormat
import io.realm.Realm
import io.realm.RealmList
import io.realm.Sort
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class ConversionDataRepository private constructor(
    private val realm: Realm
) {
    private val conversionDataService by lazy { ConversionDataService.create() }
    fun getConversionData(): ConversionData? {
        val conversionData = realm.where(ConversionDataDB::class.java).sort("date", Sort.DESCENDING).findFirst()
        if (conversionData != null) {
            with(conversionData) {
                val map = mutableMapOf<String, Double>()
                rates.forEach {
                    map[it.symbol] = it.rate
                }
                return ConversionData(timestamp, base, date, map)
            }
        }
        return null
    }

    fun downloadConversionData(callback: Callback<ConversionData>) {
        val call = conversionDataService.getLatestConversionData(accessKey, currencyArray.joinToString(separator = ","))
        call.enqueue(callback)
    }

    fun saveConversionData(conversionData: ConversionData) {
        with(conversionData) {
            val ratesDB: RealmList<RateDB> = RealmList()
            rates.forEach {
                ratesDB.add(RateDB(it.key, it.value))
            }
            val conversionDataDB = ConversionDataDB(simpleDateFormat.format(date), timestamp, base, date, ratesDB)
            realm.beginTransaction()
            realm.copyToRealmOrUpdate(conversionDataDB)
            realm.commitTransaction()
        }
    }

    fun getConversionDataHistory(is30Days: Boolean): LiveData<List<ConversionData>> {
        val conversionDataHistory = mutableListOf<ConversionData>()
        val c = Calendar.getInstance()
        val toDate = c.time
        val fromDate = if (is30Days) {
            c.add(Calendar.DATE, -30)
            c.time
        } else {
            c.add(Calendar.DATE, -60)
            c.time
        }
        val conversionData = realm.where(ConversionDataDB::class.java).between("date", fromDate, toDate).sort("date", Sort.ASCENDING).findAll()
        conversionData.forEach{
            with(it) {
                val map = mutableMapOf<String, Double>()
                rates.forEach {
                    map[it.symbol] = it.rate
                }
                conversionDataHistory.add(ConversionData(timestamp, base, date, map))
            }
        }
        return MutableLiveData(conversionDataHistory)
    }

    fun seedHistoricalData() {
        val c = Calendar.getInstance()
        for (i in 0 until 60) {
            c.add(Calendar.DATE, -1)
            val date = c.time
            val rates = RealmList<RateDB>()

            rates.add(RateDB("EUR",1.0))
            rates.add(RateDB("USD",1.0 + Math.random()))
            rates.add(RateDB("AUD",1.0 + Math.random()))
            rates.add(RateDB("CAD",1.0 + Math.random()))
            rates.add(RateDB("PLN",4.0 + Math.random()))
            rates.add(RateDB("MXN",21.0 + Math.random()))

            val conversionDataDB = ConversionDataDB(simpleDateFormat.format(date), date.time / 1000, "USD", date, rates)
            realm.beginTransaction()
            realm.copyToRealmOrUpdate(conversionDataDB)
            realm.commitTransaction()
        }
    }

    companion object {
        // For Singleton instantiation
        @Volatile
        private var instance: ConversionDataRepository? = null

        fun getInstance(realm: Realm) =
            instance ?: synchronized(this) {
                instance ?: ConversionDataRepository(realm).also { instance = it }
            }
    }
}