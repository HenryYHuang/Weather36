package com.wading.weather36.model

data class WeatherModel(var records: Records?)

data class Records(var location: ArrayList<WeatherLocation>?)

data class WeatherLocation(var weatherElement: ArrayList<WeatherElement>?)

data class WeatherElement(var time: ArrayList<WeatherTime>)

data class WeatherTime(var startTime: String?,
                  var endTime: String?,
                  var parameter: Parameter?
)

data class Parameter(var parameterName: String?,
                var parameterUnit: String?)

data class Weather(val startTime: String?,
                   val endTime: String?,
                   val parameterName: String?,
                   val parameterUnit: String?
)