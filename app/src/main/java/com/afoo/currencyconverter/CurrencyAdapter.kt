package com.afoo.currencyconverter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

class CurrencyAdapter(var context: Context, var flags: IntArray, var currency: Array<String>): BaseAdapter(){
    private var inflater:LayoutInflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = if(convertView == null) {inflater.inflate(R.layout.currency_row,parent,false)} else convertView
        view.apply {
            (findViewById<ImageView>(R.id.flag)).setImageResource(flags[position])
            (findViewById<TextView>(R.id.currency)).text = currency[position]
        }
        return view
    }

    override fun getItem(position: Int): Any {
        return currency[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return flags.size
    }
}