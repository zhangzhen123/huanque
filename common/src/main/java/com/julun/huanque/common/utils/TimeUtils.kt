package com.julun.huanque.common.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by dong on 2018/3/16.
 */
object TimeUtils {
    val TIMEFORMAT1 : String = "HH:mm:ss"
    val TIMEFORMAT2 : String = "mm:ss"
    fun formatTime( date : Long) : String{
        return SimpleDateFormat(TIMEFORMAT2).format( Date(date))
    }
}