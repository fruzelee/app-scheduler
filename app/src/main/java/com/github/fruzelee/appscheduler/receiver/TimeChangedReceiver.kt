package com.github.fruzelee.appscheduler.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.github.fruzelee.appscheduler.dao.ScheduleTemplateDAO

/**
 * @author fazle rabbi
 * github.com/fruzelee
 * web: fr.crevado.com
 */
class TimeChangedReceiver : BroadcastReceiver() {
    private var mScheduleTemplateDao: ScheduleTemplateDAO? = null
    override fun onReceive(context: Context?, intent: Intent) {
        mScheduleTemplateDao = ScheduleTemplateDAO(context)
        if (intent.action == null) {
            return
        }
        if (intent.action == "android.intent.action.TIME_SET" || intent.action == "android.intent.action.TIMEZONE_CHANGED") {
            mScheduleTemplateDao!!.setAlarms(context)
        }
    }

    companion object {
        const val TAG = "TimeChangedReceiver"
    }
}
