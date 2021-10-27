package com.github.fruzelee.appscheduler.util

import java.text.DateFormat
import java.util.*

/**
 * @author fazle rabbi
 * github.com/fruzelee
 * web: fr.crevado.com
 */
object DateHelper {
    fun isValidSchedulingDateStart(j: Long, j2: Long): Boolean {
        return j2 > 0 && j2 > j
    }

    fun getCurrentDate(): Long {
        val instance = Calendar.getInstance()
        instance[13] = 0
        instance[14] = 0
        return instance.timeInMillis
    }

    fun getDateTime(j: Long, i: Int, i2: Int): String {
        return if (j <= 0) {
            ""
        } else try {
            val date = Date()
            date.time = j
            DateFormat.getDateTimeInstance(i, i2).format(date)
        } catch (unused: Exception) {
            ""
        }
    }

    fun getDate(j: Long, i: Int): String {
        return if (j <= 0) {
            ""
        } else try {
            val date = Date()
            date.time = j
            DateFormat.getDateInstance(i).format(date)
        } catch (unused: Exception) {
            ""
        }
    }

    fun getTime(j: Long, i: Int): String {
        return if (j <= 0) {
            ""
        } else try {
            val date = Date()
            date.time = j
            DateFormat.getTimeInstance(i).format(date)
        } catch (unused: Exception) {
            ""
        }
    }

    fun getYearsDifference(j: Long, j2: Long): Long {
        val calendar = getCalendar(j)
        val calendar2 = getCalendar(j2)
        var i = calendar2[1] - calendar[1]
        if (calendar[2] > calendar2[2] || calendar[2] == calendar2[2] && calendar[5] > calendar2[5] || calendar[2] == calendar2[2] && calendar[5] == calendar2[5] && calendar[11] > calendar2[11] || calendar[2] == calendar2[2] && calendar[5] == calendar2[5] && calendar[11] == calendar2[11] && calendar[12] > calendar2[12]) {
            i--
        }
        return i.toLong()
    }

    fun getCalendar(j: Long): Calendar {
        val instance = Calendar.getInstance()
        instance.timeInMillis = j
        return instance
    }
}
