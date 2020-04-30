package com.wading.weather36

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_second.*

class SecondActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)
        val startTime = intent.getStringExtra(MainActivity.START_TIME)
        val endTime = intent.getStringExtra(MainActivity.END_TIME)
        val parameter = intent.getStringExtra(MainActivity.PARAMETER)

        starttime_txt.text = startTime
        endtime_txt.text = endTime
        parameter_txt.text = parameter
    }
}
