package com.github.fruzelee.appscheduler.receiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.github.fruzelee.appscheduler.dao.ScheduleTemplateDAO

/**
 * @author fazle rabbi
 * github.com/fruzelee
 * web: fr.crevado.com
 */
class BootReceiver : BroadcastReceiver() {
    private var mScheduleTemplateDao: ScheduleTemplateDAO? = null
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context?, intent: Intent?) {
        val templateDAO = ScheduleTemplateDAO(context)
        mScheduleTemplateDao = templateDAO
        templateDAO.setAlarms(context)
    }
}
