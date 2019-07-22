package com.afoo.currencyconverter.data

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class ConversionDataDB(
    @PrimaryKey var primaryKey: String = "", var timestamp: Long = 0,
    var base: String = "",
    var date: Date = Date(),
    var rates: RealmList<RateDB> = RealmList()
) : RealmObject()

open class RateDB(var symbol: String = "", var rate: Double = 0.0) : RealmObject()