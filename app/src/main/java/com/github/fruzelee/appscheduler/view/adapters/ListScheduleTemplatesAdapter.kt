package com.github.fruzelee.appscheduler.view.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.github.fruzelee.appscheduler.view.activities.AddScheduleActivity.Companion.getSpinnerSelectionByIntervalMultiplier
import com.github.fruzelee.appscheduler.view.activities.MainActivity
import com.github.fruzelee.appscheduler.R
import com.github.fruzelee.appscheduler.dao.DBHelper
import com.github.fruzelee.appscheduler.dao.ScheduleTemplateDAO
import com.github.fruzelee.appscheduler.model.ScheduleTemplateModel
import com.github.fruzelee.appscheduler.receiver.AlarmReceiver
import com.github.fruzelee.appscheduler.util.Constants
import com.github.fruzelee.appscheduler.util.DateHelper.getCurrentDate
import com.github.fruzelee.appscheduler.util.DateHelper.getDateTime
import com.github.fruzelee.appscheduler.util.DateHelper.isValidSchedulingDateStart
import com.github.fruzelee.appscheduler.util.Globals.isValidValue
import com.github.fruzelee.appscheduler.util.Globals.showToastMessage
import java.lang.Exception

/**
 * @author fazle rabbi
 * github.com/fruzelee
 * web: fr.crevado.com
 */
class ListScheduleTemplatesAdapter(context: Context, list: List<ScheduleTemplateModel>?, scheduleTemplateDAO: ScheduleTemplateDAO) :
    BaseAdapter() {
    private val mContext: Context
    private val mInflater: LayoutInflater
    private var mItems: List<ScheduleTemplateModel>? = null
    private val mScheduleTemplateDao: ScheduleTemplateDAO
    private val pm: PackageManager
    private val res: Resources
    override fun getCount(): Int {
        return if (getItems() == null || getItems()!!.isEmpty()) {
            0
        } else getItems()!!.size
    }

    override fun getItem(i: Int): ScheduleTemplateModel? {
        return if (getItems() == null || getItems()!!.isEmpty()) {
            null
        } else getItems()!![i]
    }

    override fun getItemId(i: Int): Long {
        return if (getItems() == null || getItems()!!.isEmpty()) i.toLong() else getItems()!![i].getId()
    }

    @SuppressLint("SetTextI18n")
    override fun getView(i: Int, view: View?, viewGroup: ViewGroup?): View? {
        var view = view
        var viewHolder: ViewHolder? = null
        var applicationInfo: ApplicationInfo?
        if (view == null) {
            try {
                view = mInflater.inflate(R.layout.list_item_schedule, viewGroup, false)
                viewHolder = ViewHolder()
                viewHolder.tName = view.findViewById(R.id.tName)
                viewHolder.tDescription = view.findViewById(R.id.tDescription)
                viewHolder.tText = view.findViewById(R.id.tText)
                viewHolder.iIcon = view.findViewById(R.id.iIcon)
                viewHolder.tScheduling = view.findViewById(R.id.tScheduling)
                viewHolder.tInterval = view.findViewById(R.id.tInterval)
                viewHolder.lInterval = view.findViewById(R.id.lInterval)
                viewHolder.bScheduling = view.findViewById(R.id.bScheduling)
                view.tag = viewHolder
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            viewHolder = view.tag as ViewHolder
        }
        val item = getItem(i)
        if (item != null) {
            var packageName: CharSequence? = item.getPackageName()
            try {
                applicationInfo = pm.getApplicationInfo(item.getPackageName()!!, 0)
                try {
                    packageName = pm.getApplicationLabel(applicationInfo)
                } catch (unused: Exception) {
                    unused.printStackTrace()
                }
            } catch (unused2: Exception) {
                applicationInfo = null
            }
            viewHolder!!.tName!!.text = packageName
            var i2 = 8
            viewHolder.tName!!.visibility =
                if (isValidValue(packageName)) View.VISIBLE else View.GONE
            viewHolder.tDescription!!.text = item.getDescription()
            viewHolder.tDescription!!.visibility =
                if (isValidValue(item.getDescription())) View.VISIBLE else View.GONE
            viewHolder.tText!!.text = item.getPackageName()
            viewHolder.tText!!.visibility = if (applicationInfo != null) View.VISIBLE else View.GONE
            val templateModel = mScheduleTemplateDao.getTemplate(item.getId())
            item.setSchedulingDateStart(templateModel!!.getSchedulingDateStart())
            item.setSchedulingStatus(templateModel.getSchedulingStatus())
            val dateTime = getDateTime(item.getSchedulingDateStart(), 2, 3)
            viewHolder.tScheduling!!.text = dateTime
            viewHolder.tScheduling!!.setTextColor(
                getTextColor(
                    item.getSchedulingStatus(),
                    mContext
                )
            )
            viewHolder.tScheduling!!.visibility =
                if (isValidValue(dateTime)) View.VISIBLE else View.GONE
            val view2 = viewHolder.lInterval
            if (item.getSchedulingRepeat() == 1) {
                i2 = 0
            }
            view2!!.visibility = i2
            if (item.getSchedulingRepeat() == 1) {
                viewHolder.tInterval!!.text = item.getSchedulingInterval()
                    .toString() + " " + res.getStringArray(R.array.intervals_array)[getSpinnerSelectionByIntervalMultiplier(
                    item.getSchedulingIntervalMultiplier()
                )]
                viewHolder.tInterval!!.setTextColor(
                    getTextColor(
                        item.getSchedulingStatus(),
                        mContext
                    )
                )
            }
            viewHolder.bScheduling!!.setBackgroundResource(
                getBackgroundResource(
                    item.getSchedulingStatus(),
                    mContext
                )
            )
            viewHolder.bScheduling!!.setOnClickListener(ChangeSchedulingStatusListener(item, i))
            viewHolder.bScheduling!!.setImageResource(
                getImageResource(
                    item.getSchedulingRepeat(),
                    mContext
                )
            )
            viewHolder.bScheduling!!.isFocusable = false
            if (applicationInfo == null) {
                viewHolder.iIcon!!.setImageResource(
                    res.getIdentifier(
                        "ic_block_black_48dp",
                        "mipmap",
                        mContext.packageName
                    )
                )
            } else {
                try {
                    viewHolder.iIcon!!.setImageDrawable(pm.getApplicationIcon(applicationInfo.packageName))
                } catch (e2: Exception) {
                    e2.printStackTrace()
                }
            }
        }
        return view
    }

    fun getItems(): List<ScheduleTemplateModel>? {
        return mItems
    }

    fun setItems(list: List<ScheduleTemplateModel>?) {
        mItems = list
    }

    private inner class ChangeSchedulingStatusListener(
        private val parent: ScheduleTemplateModel,
        private val position: Int
    ) :
        View.OnClickListener {
        override fun onClick(view: View) {
            val i: Int
            val resources: Resources
            if (view.id == R.id.bScheduling) {
                val i2 = if (parent.getSchedulingStatus() == 1) 0 else 1
                var schedulingDateStart = parent.getSchedulingDateStart()
                if (i2 == 1 && !isValidSchedulingDateStart(
                        getCurrentDate(),
                        parent.getSchedulingDateStart()
                    )
                ) {
                    if (parent.getSchedulingRepeat() == 0) {
                        showToastMessage(res.getString(R.string.invalid_date), mContext)
                        return
                    } else {
                        schedulingDateStart = AlarmReceiver.getFutureDate(
                            getCurrentDate(),
                            parent.getSchedulingDateStart(),
                            parent.getSchedulingInterval().toLong(),
                            parent.getSchedulingIntervalMultiplier()
                        )
                        mScheduleTemplateDao.updateTemplate(
                            schedulingDateStart,
                            parent.getId(),
                            DBHelper.COLUMN_TEMPLATE_SCHEDULING_DATE_START
                        )
                    }
                }
                if (i2 == 1) {
                    AlarmReceiver.setAlarm(
                        mContext,
                        schedulingDateStart,
                        parent.getSchedulingDateEnd(),
                        parent.getSchedulingInterval(),
                        parent.getSchedulingIntervalMultiplier(),
                        parent.getSchedulingConfirmTask(),
                        parent.getSchedulingRepeat(),
                        parent.getSchedulingId(),
                        parent.getId()
                    )
                } else {
                    AlarmReceiver.cancelAlarms(mContext, parent.getSchedulingId())
                }
                mScheduleTemplateDao.updateTemplate(
                    i2,
                    parent.getId(),
                    DBHelper.COLUMN_TEMPLATE_SCHEDULING_STATUS
                )
                if (i2 == 1) {
                    resources = res
                    i = R.string.scheduling_on
                } else {
                    resources = res
                    i = R.string.scheduling_off
                }
                showToastMessage(resources.getString(i), mContext)
                val intent = Intent()
                intent.action = Constants.BROADCAST_ACTION_SERVICE_STATUS_CHANGED
                intent.putExtra("STATUS", i2)
                intent.putExtra("POSITION", position)
                mContext.sendBroadcast(intent)
                (mContext as MainActivity).createList()
            }
        }
    }

    internal class ViewHolder {
        var bScheduling: ImageButton? = null
        var iIcon: ImageView? = null
        var lInterval: View? = null
        var tDescription: TextView? = null
        var tInterval: TextView? = null
        var tName: TextView? = null
        var tScheduling: TextView? = null
        var tText: TextView? = null
    }

    private fun getBackgroundResource(i: Int, context: Context): Int {
        return res.getIdentifier(
            if (i == 0) "black_circle_drawable" else "pink_500_circle_drawable",
            "drawable",
            context.packageName
        )
    }

    private fun getImageResource(i: Int, context: Context): Int {
        return res.getIdentifier(
            if (i == 1) "ic_repeat_white_18dp" else "ic_access_time_white_18dp",
            "mipmap",
            context.packageName
        )
    }

    private fun getTextColor(i: Int, context: Context?): Int {
        return if (i != 0) {
            ContextCompat.getColor(context!!, R.color.pink_500)
        } else try {
            ContextCompat.getColor(context!!, R.color.blue_grey_400)
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    companion object {
        const val TAG = "ListTemplatesAdapter"
    }

    init {
        setItems(list)
        mInflater = LayoutInflater.from(context)
        mContext = context
        mScheduleTemplateDao = scheduleTemplateDAO
        res = context.resources
        pm = mContext.packageManager
    }
}
