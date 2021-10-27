package com.github.fruzelee.appscheduler.receiver

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.*
import android.content.res.Resources
import android.os.PowerManager
import com.github.fruzelee.appscheduler.dao.DBHelper
import com.github.fruzelee.appscheduler.dao.ScheduleTemplateDAO
import com.github.fruzelee.appscheduler.model.ScheduleTemplateModel
import com.github.fruzelee.appscheduler.util.Constants
import com.github.fruzelee.appscheduler.util.DateHelper.getCurrentDate
import com.github.fruzelee.appscheduler.util.DateHelper.getYearsDifference
import com.github.fruzelee.appscheduler.util.Globals.openApp
import com.github.fruzelee.appscheduler.util.Globals.sendGenericBroadcast
import java.lang.Exception
import java.util.*

/**
 * @author fazle rabbi
 * github.com/fruzelee
 * web: fr.crevado.com
 */
class AlarmReceiver : BroadcastReceiver() {
    private var mContext: Context? = null
    private var mScheduleTemplateDao: ScheduleTemplateDAO? = null
    private var mode = 0
    private var packageName: String? = null
    var res: Resources? = null
    private var schedulingConfirmTask = 0
    private var schedulingDateEnd: Long = 0
    private var schedulingDateStart: Long = 0
    private var schedulingId = 0
    private var schedulingInterval = 0
    private var schedulingIntervalMultiplier: Long = 0
    private var schedulingRepeat = 0
    private var scheduleTemplateModel: ScheduleTemplateModel? = null
    override fun onReceive(context: Context, intent: Intent) {
        mContext = context
        mScheduleTemplateDao = ScheduleTemplateDAO(context)
        res = context.resources
        val newWakeLock =
            (context.getSystemService(Context.POWER_SERVICE) as PowerManager).newWakeLock(
                1,
                "com.github.fruzelee.appscheduler:mywakelocktag"
            )
        try {
            newWakeLock!!.acquire(10 * 60 * 1000L /*10 minutes*/)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val extras = intent.extras
        if (extras != null) {
            mode = extras.getInt("MODE", 0)
            val id = extras.getLong("_id", -1)
            templateId = id
            val templateModel2 = mScheduleTemplateDao!!.getTemplate(id)
            scheduleTemplateModel = templateModel2
            if (templateModel2 != null) {
                packageName = templateModel2.getPackageName()
                schedulingDateStart = scheduleTemplateModel!!.getSchedulingDateStart()
                schedulingDateEnd = scheduleTemplateModel!!.getSchedulingDateEnd()
                schedulingInterval = scheduleTemplateModel!!.getSchedulingInterval()
                schedulingIntervalMultiplier = scheduleTemplateModel!!.getSchedulingIntervalMultiplier()
                schedulingConfirmTask = scheduleTemplateModel!!.getSchedulingConfirmTask()
                schedulingRepeat = scheduleTemplateModel!!.getSchedulingRepeat()
                schedulingId = scheduleTemplateModel!!.getSchedulingId()
            }
        }
        when {
            mode == 1 -> {
                cancelAlarm(context, 1)
                mScheduleTemplateDao!!.setAlarms(context)
            }
            scheduleTemplateModel == null -> {
                cancelAlarm(context, schedulingId)
            }
            else -> {
                val i = schedulingRepeat
                if (i == 0) {
                    cancelAlarm(context, schedulingId)
                    mScheduleTemplateDao!!.updateTemplate(
                        0,
                        templateId,
                        DBHelper.COLUMN_TEMPLATE_SCHEDULING_STATUS
                    )
                } else if (i == 1) {
                    val futureDate = getFutureDate(
                        getCurrentDate(),
                        schedulingDateStart, schedulingInterval.toLong(), schedulingIntervalMultiplier
                    )
                    mScheduleTemplateDao!!.updateTemplate(
                        futureDate,
                        templateId,
                        DBHelper.COLUMN_TEMPLATE_SCHEDULING_DATE_START
                    )
                    setAlarm(
                        mContext,
                        futureDate,
                        scheduleTemplateModel!!.getSchedulingDateEnd(),
                        scheduleTemplateModel!!.getSchedulingInterval(),
                        scheduleTemplateModel!!.getSchedulingIntervalMultiplier(),
                        scheduleTemplateModel!!.getSchedulingConfirmTask(),
                        scheduleTemplateModel!!.getSchedulingRepeat(),
                        scheduleTemplateModel!!.getSchedulingId(),
                        scheduleTemplateModel!!.getId()
                    )
                }
                openApp(context, packageName)
                sendGenericBroadcast(context, Constants.BROADCAST_ACTION_SERVICE_RUNNING, null)
            }
        }
        if (newWakeLock != null) {
            try {
                if (newWakeLock.isHeld) {
                    newWakeLock.release()
                }
            } catch (e2: Exception) {
                e2.printStackTrace()
            }
        }
    }

    companion object {
        const val TAG = "AlarmReceiver"
        var templateId: Long = -1
        private fun getAlarmInterval(j: Long, j2: Long): Long {
            return j * j2
        }

        fun getFutureDate(currentDate: Long, schedulingDateStart: Long, schedulingInterval: Long, schedulingIntervalMultiplier: Long): Long {
            val mSchedulingDateStart = if (schedulingDateStart == -1L) 0 else schedulingDateStart
            if (mSchedulingDateStart > currentDate) {
                return mSchedulingDateStart
            }
            val alarmInterval = getAlarmInterval(schedulingInterval, schedulingIntervalMultiplier)
            val instance = Calendar.getInstance()
            instance.timeInMillis = mSchedulingDateStart
            var z = true
            return if (schedulingIntervalMultiplier == Constants.ALARM_MANAGER_INTERVAL_YEAR) {
                try {
                    val yearsDifference =
                        (instance[1].toLong() + getYearsDifference(mSchedulingDateStart, currentDate) / schedulingInterval * schedulingInterval).toInt()
                    instance[1] = yearsDifference
                    if (currentDate >= instance.timeInMillis) {
                        instance[1] = (yearsDifference.toLong() + schedulingInterval).toInt()
                    }
                    instance.timeInMillis
                } catch (e: Exception) {
                    e.printStackTrace()
                    currentDate + alarmInterval
                }
            } else {
                val j6 = (currentDate - mSchedulingDateStart) / alarmInterval
                java.lang.Long.signum(j6)
                var j7 = mSchedulingDateStart + j6 * alarmInterval
                if (currentDate >= j7) {
                    j7 += alarmInterval
                }
                if (alarmInterval % 86400000 != 0L) {
                    z = false
                }
                if (!z) {
                    return j7
                }
                val instance2 = Calendar.getInstance()
                instance2.timeInMillis = j7
                instance2[11] = instance[11]
                instance2[12] = instance[12]
                instance2.timeInMillis
            }
        }

        fun cancelAlarm(context: Context, i: Int) {
            val broadcast = PendingIntent.getBroadcast(
                context, i, Intent(
                    context,
                    AlarmReceiver::class.java
                ), PendingIntent.FLAG_IMMUTABLE
            )
            (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(broadcast)
            broadcast.cancel()
        }

        fun cancelAlarms(context: Context, i: Int) {
            cancelAlarm(context, i)
            cancelAlarm(context, -i)
        }

        @SuppressLint("InlinedApi")
        fun setAlarm(
            context: Context?,
            schedulingDateStart: Long,
            schedulingDateEnd: Long,
            schedulingInterval: Int,
            schedulingIntervalMultiplier: Long,
            schedulingConfirmTask: Int,
            isSchedulingRepeat: Int,
            currentSchedulingId: Int,
            id: Long
        ) {
            val alarmManager = context!!.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, AlarmReceiver::class.java)
            intent.putExtra("_id", id)
            val broadcast =
                PendingIntent.getBroadcast(context, currentSchedulingId, intent, PendingIntent.FLAG_IMMUTABLE)
            if (isSchedulingRepeat == 0) {
                alarmManager[AlarmManager.RTC_WAKEUP, schedulingDateStart] = broadcast
            } else if (isSchedulingRepeat == 1) {
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    schedulingDateStart,
                    getAlarmInterval(schedulingInterval.toLong(), schedulingIntervalMultiplier),
                    broadcast
                )
            }
        }
    }
}