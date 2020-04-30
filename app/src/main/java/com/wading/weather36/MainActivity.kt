package com.wading.weather36

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wading.weather36.model.Weather
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.weather_row.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URL

class MainActivity : AppCompatActivity() {
    companion object {
        const val START_TIME = "startTime"
        const val END_TIME = "endTime"
        const val PARAMETER = "parameter"
        const val FIRSTLUNCH = "FIRSTLUNCH"
    }

    var weathers =  mutableListOf<Weather>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getSharedPreferences("Weather", Context.MODE_PRIVATE)
            .getString(FIRSTLUNCH, null)?.also {
                AlertDialog.Builder(this).setTitle("歡迎回來")
                    .setPositiveButton("OK", null)
                    .show()
            }

        getSharedPreferences("Weather", Context.MODE_PRIVATE)
            .edit().putString(FIRSTLUNCH, FIRSTLUNCH).commit()

        recycler.setHasFixedSize(true)
        recycler.layoutManager = LinearLayoutManager(this)

        GlobalScope.launch {
            val url = URL("https://opendata.cwb.gov.tw/api/v1/rest/datastore/F-C0032-001?Authorization=CWB-8E10EF77-A250-46E1-9532-8AFF5671A1F1&format=JSON&locationName=%E8%87%BA%E5%8C%97%E5%B8%82&elementName=MinT")
            val str = url.readText()
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
            weathers.add(1, Weather(null,null,null,null))
            weathers.add(3, Weather(null,null,null,null))

            runOnUiThread {
                recycler.adapter = WeatherAdapter()
            }
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
            if (position%2 == 0) {
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
