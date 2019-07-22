package com.afoo.currencyconverter

import android.os.Bundle
import android.preference.PreferenceManager
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.afoo.currencyconverter.data.ConversionData
import com.afoo.currencyconverter.data.ConversionDataRepository
import com.afoo.currencyconverter.databinding.ActivityMainBinding
import com.afoo.currencyconverter.viewmodels.CurrencyConverterViewModel
import com.afoo.currencyconverter.viewmodels.CurrencyConverterViewModelFactory
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IFillFormatter
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import io.realm.Realm
import java.text.SimpleDateFormat


class MainActivity : AppCompatActivity() {
    //    private val conversionDataService by lazy { ConversionDataService.create() }
    lateinit var viewModel: CurrencyConverterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
//        setContentView(R.layout.activity_main)
//        setSupportActionBar(toolbar)

        View.VISIBLE
        val currencyAdapter = CurrencyAdapter(this, flags, currencyArray)

        Realm.init(this)
        val realm = Realm.getDefaultInstance()


        val factory = CurrencyConverterViewModelFactory(ConversionDataRepository.getInstance(realm))
        viewModel = ViewModelProviders.of(this, factory).get(CurrencyConverterViewModel::class.java)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("first_launch", true)) {
            viewModel.seedHistoricalData()
            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("first_launch", true).apply()
        }

        with(binding) {
            spTo.apply {
                adapter = currencyAdapter
                onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        tvTo.text = spTo.selectedItem.toString()
                    }
                }
                setSelection(4)
            }

            spFrom.apply {
                adapter = currencyAdapter
                onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        tvFrom.text = selectedItem.toString()
                    }
                }
            }

            chart.apply {
                //                setViewPortOffsets(0f, 0f, 0f, 15f)
                setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.darkBlue))
                description.isEnabled = false
                setTouchEnabled(true)

                isDragEnabled = true
                setScaleEnabled(true)

                setPinchZoom(false)

                setDrawGridBackground(false)
                maxHighlightDistance = 300f

                xAxis.apply {
                    isEnabled = true
                    position = XAxis.XAxisPosition.BOTTOM
                    axisMinimum = 0f
                    textColor = ContextCompat.getColor(this@MainActivity, android.R.color.white)
                    setDrawGridLines(false)
                    axisLineColor = ContextCompat.getColor(this@MainActivity, android.R.color.white)
                }

                axisLeft.apply {
                    setLabelCount(6, false)
                    textColor = ContextCompat.getColor(this@MainActivity, android.R.color.white)
                    setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
                    setDrawGridLines(false)
                    axisLineColor = ContextCompat.getColor(this@MainActivity, android.R.color.white)
                }

                axisRight.isEnabled = false
                legend.isEnabled = false

//                setOnChartValueSelectedListener(object : OnChartValueSelectedListener{
//                    override fun onNothingSelected() {
//                    }
//
//                    override fun onValueSelected(e: Entry?, h: Highlight?) {
//                        Toast.makeText(this@MainActivity,"${e?.x},${e?.y}",Toast.LENGTH_SHORT).show()
//                    }
//                })


                animateXY(2000, 2000)
                invalidate()
            }

            val text = getString(R.string.inbox_text)
            val spannableString = SpannableString(text)
            spannableString.setSpan(UnderlineSpan(), 0, text.length, 0)

            inboxText.setText(spannableString)

        }
        viewModel.conversionDataHistory.observe(this, Observer {
            setChartData(binding, it)
        })


    }

    private fun setChartData(
        binding: ActivityMainBinding,
        conversionDataHistory: List<ConversionData>
    ) {
        with(binding) {
            val simpleDateFormat = SimpleDateFormat("d MMM")
            val entries = mutableListOf<Entry>()
            val label = mutableListOf<String>()
            conversionDataHistory.forEachIndexed { index, conversionData ->
                with(conversionData) {
                    val data = rates[tvTo.text.toString()]!! / rates[tvFrom.text.toString()]!!
                    entries.add(Entry(index.toFloat(), data.toFloat()))
                    label.add(simpleDateFormat.format(date))
                }
            }
            chart.apply {
                marker = object : MarkerView(this@MainActivity, R.layout.marker_layout) {
                    override fun refreshContent(e: Entry, highlight: Highlight) {

                        findViewById<TextView>(R.id.date).text =
                            simpleDateFormat.format(conversionDataHistory[e.x.toInt()].date)
                        findViewById<TextView>(R.id.description).text = "1 ${tvFrom.text} = ${e.y}"
                        super.refreshContent(e, highlight)
                    }

                    private var mOffset: MPPointF? = null
                    override fun getOffset(): MPPointF {
                        return mOffset ?: MPPointF(-25f, -height.toFloat() + 25)
                    }
                }

                xAxis.valueFormatter = IndexAxisValueFormatter(label)
//                {
//                    override fun getFormattedValue(value: Float, axis: AxisBase?): String {
//                        return if (value % 6f == 0f) simpleDateFormat.format(conversionDataHistory[value.toInt()].date) else ""
//                    }
//                }

                if (data != null && data.dataSetCount > 0) {
                    val dataSet = data.getDataSetByIndex(0) as LineDataSet
                    dataSet.values = entries
                    data.notifyDataChanged()
                    notifyDataSetChanged()
                } else {
                    val dataSet = LineDataSet(entries, "Conversion Data")
                    dataSet.apply {
                        mode = LineDataSet.Mode.CUBIC_BEZIER
                        cubicIntensity = 0.2f
                        setDrawFilled(true)
                        setDrawCircles(false)
                        lineWidth = 1.8f
                        circleRadius = 4f
                        setCircleColor(ContextCompat.getColor(this@MainActivity, R.color.lightGreen))
                        color = ContextCompat.getColor(this@MainActivity, R.color.skyeBlue)
                        highLightColor = ContextCompat.getColor(this@MainActivity, R.color.lightGreen)
                        fillColor = ContextCompat.getColor(this@MainActivity, R.color.skyeBlue)
                        fillAlpha = 200
                        setDrawHorizontalHighlightIndicator(false)
                        setDrawVerticalHighlightIndicator(false)
                        fillFormatter = IFillFormatter { _, _ -> axisLeft.axisMinimum }
                    }
                    val lineData = LineData(dataSet)
                    //                    data.setValueTypeface(tfLight)
                    lineData.apply {
                        setValueTextSize(9f)
                        setDrawValues(false)
                    }
                    data = lineData
                }
                invalidate()
            }
        }
    }


//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        menuInflater.inflate(R.menu.menu_main, menu)
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        return when (item.itemId) {
//            R.id.action_settings -> true
//            else -> super.onOptionsItemSelected(item)
//        }
//    }
}
