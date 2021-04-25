package com.iotree.iot_app

data class DateObject(val timeObject:TimeObject, val dateString:String, val day:Int, val month:Int, val year:Int)

data class TimeObject(val bpm:Long, val temperature:Long, val hour:Int, val minute:Int, val second:Int)