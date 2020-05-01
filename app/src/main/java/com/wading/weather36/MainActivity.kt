package com.wading.weather36

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wading.weather36.model.Weather
import com.wading.weather36.model.WeatherModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.weather_row.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.net.URL

class MainActivity : AppCompatActivity() {
    companion object {
        val TAG = MainActivity::class.java.simpleName
        const val START_TIME = "startTime"
        const val END_TIME = "endTime"
        const val PARAMETER = "parameter"
        const val FIRST_LUNCH = "FIRST_LUNCH"
        const val WEATHER_APP = "WEATHER_APP"
    }

    private lateinit var adapter: WeatherAdapter
    var weathers =  mutableListOf<Weather>()
    val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getSharedPreferences(WEATHER_APP, Context.MODE_PRIVATE)
            .getString(FIRST_LUNCH, null)?.also {
                AlertDialog.Builder(this).setTitle("歡迎回來")
                    .setPositiveButton("OK", null)
                    .show()
            }

        getSharedPreferences(WEATHER_APP, Context.MODE_PRIVATE)
            .edit().putString(FIRST_LUNCH, FIRST_LUNCH).commit()

        recycler.setHasFixedSize(true)
        recycler.layoutManager = LinearLayoutManager(this)
        adapter = WeatherAdapter()
        recycler.adapter = adapter

        okhttpGetWeather()

//        originalGetWeather()
    }

    private fun okhttpGetWeather() {
        val request = Request.Builder()
            .url("https://opendata.cwb.gov.tw/api/v1/rest/datastore/F-C0032-001?Authorization=CWB-8E10EF77-A250-46E1-9532-8AFF5671A1F1&format=JSON&locationName=%E8%87%BA%E5%8C%97%E5%B8%82&elementName=MinT")
            .build()
        val callback = object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d(TAG, "MainActivity: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val str = response.body?.string()
                parseGson(str)
            }
        }
        OkHttpClient().newCall(request).enqueue(callback)
    }

    private fun parseGson(str: String?) {
        val weatherModel =
            gson.fromJson<WeatherModel>(str, object : TypeToken<WeatherModel>() {}.type)
        val weatherTime = weatherModel.records?.location?.get(0)?.weatherElement?.get(0)?.time
        weatherTime?.run {
            for (i in 0 until this.size) {
                val startTime = this[i].startTime
                val endTime = this[i].endTime
                val parameterName = this[i].parameter?.parameterName
                val parameterUnit = this[i].parameter?.parameterUnit
                weathers.add(Weather(startTime, endTime, parameterName, parameterUnit))
            }
            weathers.add(1, Weather(null, null, null, null))
            weathers.add(3, Weather(null, null, null, null))
            runOnUiThread {
//                recycler.adapter = WeatherAdapter()
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun originalGetWeather() {
        GlobalScope.launch {
            val url =
                URL("https://opendata.cwb.gov.tw/api/v1/rest/datastore/F-C0032-001?Authorization=CWB-8E10EF77-A250-46E1-9532-8AFF5671A1F1&format=JSON&locationName=%E8%87%BA%E5%8C%97%E5%B8%82&elementName=MinT")
            val str = url.readText()
            parseJson(str)

        }
    }

    private fun parseJson(str: String) {
        val json = JSONObject(str)
        val records = json.getJSONObject("records")
        val location = records.getJSONArray("location")
        val element = location.getJSONObject(0)
        val weatherElements = element.getJSONArray("weatherElement")
        val weatherElement = weatherElements.getJSONObject(0)
        val timeArray = weatherElement.getJSONArray("time")
        for (i in 0 until timeArray.length()) {
            val timeObject = timeArray.getJSONObject(i)
            val startTime = timeObject.getString(START_TIME)
            val endTime = timeObject.getString(END_TIME)
            val parameter = timeObject.getJSONObject(PARAMETER)
            val parameterName = parameter.getString("parameterName")
            val parameterUnit = parameter.getString("parameterUnit")
            val weather = Weather(startTime, endTime, parameterName, parameterUnit)
            weathers.add(weather)
        }
        weathers.add(1, Weather(null, null, null, null))
        weathers.add(3, Weather(null, null, null, null))
        runOnUiThread {
            adapter.notifyDataSetChanged()
        }
    }

    inner class WeatherAdapter : RecyclerView.Adapter<WeatherHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherHolder {
            val view = layoutInflater.inflate(R.layout.weather_row, parent, false)
            return WeatherHolder(view)
        }

        override fun getItemCount(): Int {
            return weathers.size
        }

        override fun onBindViewHolder(holder: WeatherHolder, position: Int) {
            if (position % 2 == 0) {
                val startTime = weathers[position].startTime
                val endTime = weathers[position].endTime
                val parameter = "${weathers[position].parameterName}${weathers[position].parameterUnit}"
                holder.bind(startTime, endTime, parameter)
                holder.image.visibility = View.GONE
                holder.itemView.setOnClickListener {
                    val intent = Intent(this@MainActivity, SecondActivity::class.java)
                    intent.putExtra(START_TIME, startTime)
                    intent.putExtra(END_TIME, endTime)
                    intent.putExtra(PARAMETER, parameter)
                    startActivity(intent)
                }
            } else {
                holder.startTime.visibility = View.GONE
                holder.endTime.visibility = View.GONE
                holder.parameter.visibility = View.GONE
                holder.image.visibility = View.VISIBLE
            }
        }

    }

    class WeatherHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(startTime: String?, endTime: String?, parameter: String) {
            this.startTime.text = startTime
            this.endTime.text = endTime
            this.parameter.text = parameter
        }

        val startTime = view.starttime_txt
        val endTime = view.endtime_txt
        val parameter = view.parameter_txt
        val image = view.image
    }
}
