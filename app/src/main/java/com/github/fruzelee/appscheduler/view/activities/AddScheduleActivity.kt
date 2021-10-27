package com.github.fruzelee.appscheduler.view.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.Dialog
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.text.format.DateFormat
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.github.fruzelee.appscheduler.R
import com.github.fruzelee.appscheduler.dao.ScheduleTemplateDAO
import com.github.fruzelee.appscheduler.databinding.ActivityAddScheduleBinding
import com.github.fruzelee.appscheduler.model.ScheduleTemplateModel
import com.github.fruzelee.appscheduler.receiver.AlarmReceiver
import com.github.fruzelee.appscheduler.util.Constants
import com.github.fruzelee.appscheduler.util.DateHelper.getCurrentDate
import com.github.fruzelee.appscheduler.util.DateHelper.getDate
import com.github.fruzelee.appscheduler.util.DateHelper.getTime
import com.github.fruzelee.appscheduler.util.Globals.getStringFromResources
import com.github.fruzelee.appscheduler.util.Globals.getUniqueId
import com.github.fruzelee.appscheduler.util.Globals.isValidValue
import com.github.fruzelee.appscheduler.util.Globals.openApp
import com.github.fruzelee.appscheduler.util.Globals.showToastMessage
import com.github.fruzelee.appscheduler.util.Globals.startGenericActivity
import java.util.*

/**
 * @author fazle rabbi
 * github.com/fruzelee
 * web: fr.crevado.com
 */
class AddScheduleActivity : AppCompatActivity(), ActivityDynamics, View.OnClickListener {
    private lateinit var mBinding: ActivityAddScheduleBinding
    private var activity: Activity? = null
    private var allAlarmIds: ArrayList<Int>? = null
    private var currentDescription = ""
    private var currentName = ""
    private var currentPackageName: String? = ""
    private var currentSchedulingConfirmTask = 0
    private var currentSchedulingDateEnd: Long = 0
    private var currentSchedulingDateStart: Long = 0
    private var currentSchedulingId = -1
    private var currentSchedulingInterval = 1
    private var currentSchedulingIntervalMultiplier: Long = 86400000
    private var currentSchedulingRepeat = 0
    private var currentText = ""
    private var dataChanged = false
    private var mListSize = 0
    private var mScheduleTemplateModel: ScheduleTemplateModel? = null
    private var mScheduleTemplateDao: ScheduleTemplateDAO? = null
    private var mTemplateId: Long = -1
    private var newScheduleService = true
    private var pm: PackageManager? = null
    private var res: Resources? = null

    private fun hasAlarm(): Boolean {
        return alarmSet || alarmChanged
    }

    private fun configurationChanged(): Boolean {
        return dataChanged || alarmChanged
    }

    public override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        try {
            initViews()
            initValue()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            mScheduleTemplateModel = intent.getSerializableExtra(MainActivity.EXTRA_SCHEDULE) as ScheduleTemplateModel?
            mListSize = extras.getInt(MainActivity.EXTRA_LIST_SIZE, 0)
        }
        if (mScheduleTemplateModel != null) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
            (activity as AddScheduleActivity).title = res?.getString(R.string.edit_schedule)
            mTemplateId = mScheduleTemplateModel!!.getId()
            mBinding.eName.setText(mScheduleTemplateModel!!.getName())
            mBinding.eDescription.setText(mScheduleTemplateModel!!.getDescription())
            currentPackageName = mScheduleTemplateModel!!.getPackageName()
            currentSchedulingStatus = mScheduleTemplateModel!!.getSchedulingStatus()
            currentSchedulingDateStart = mScheduleTemplateModel!!.getSchedulingDateStart()
            currentSchedulingDateEnd = mScheduleTemplateModel!!.getSchedulingDateEnd()
            currentSchedulingConfirmTask = mScheduleTemplateModel!!.getSchedulingConfirmTask()
            currentSchedulingRepeat = mScheduleTemplateModel!!.getSchedulingRepeat()
            currentSchedulingInterval = mScheduleTemplateModel!!.getSchedulingInterval()
            currentSchedulingIntervalMultiplier = mScheduleTemplateModel!!.getSchedulingIntervalMultiplier()
            currentSchedulingId = mScheduleTemplateModel!!.getSchedulingId()
            newScheduleService = false
        } else {
            (activity as AddScheduleActivity).title = res?.getString(R.string.add_new_schedule)
        }
        initAlarmView(activity)
        refreshAppView(currentPackageName)
        setListeners()
    }

    public override fun onDestroy() {
        super.onDestroy()
        releaseResources()
    }

    private fun initValue() {
        activity = this
        res = resources
        pm = packageManager

        mEnable = mBinding.bEnable
        mConfirmTask = mBinding.cConfirmTask
        mRepeat = mBinding.cRepeat
        mDate = mBinding.tDate
        mHour = mBinding.tHour
        mStatus = mBinding.tStatus

        mScheduleTemplateDao = ScheduleTemplateDAO(this)
        mBinding.lAppSelector.setOnClickListener(this)
        mBinding.bEnable.setOnClickListener(this)
        mBinding.lHour.setOnClickListener(this)
        mBinding.lDate.setOnClickListener(this)
        mBinding.cConfirmTask.setOnClickListener(this)
        mBinding.cRepeat.setOnClickListener(this)

        val createFromResource = ArrayAdapter.createFromResource(
            this,
            R.array.intervals_array,
            android.R.layout.simple_spinner_item
        )
        createFromResource.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mBinding.sIntervals.adapter = createFromResource
    }

    public override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val extras = intent.extras
        if (extras != null) {
            val string = extras.getString("PACKAGE_NAME")
            if (isValidValue(string)) {
                currentPackageName = string
                refreshAppView(string)
                dataChanged = true
            }
        }
    }

    private fun setListeners() {
        mBinding.eName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable) {}
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i2: Int, i3: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i2: Int, i3: Int) {
                dataChanged = true
            }
        })
        mBinding.eDescription.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable) {}
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i2: Int, i3: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i2: Int, i3: Int) {
                dataChanged = true
            }
        })
        mBinding.eText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable) {}
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i2: Int, i3: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i2: Int, i3: Int) {
                dataChanged = true
            }
        })
        mBinding.eInterval.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable) {}
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i2: Int, i3: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i2: Int, i3: Int) {
                alarmChanged = true
            }
        })
        mBinding.sIntervals.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View, i: Int, j: Long) {
                if (i != getSpinnerSelectionByIntervalMultiplier(
                        currentSchedulingIntervalMultiplier
                    )
                ) {
                    alarmChanged = true
                }
            }
        }
    }

    private fun refreshAppView(str: String?) {
        var charSequence: CharSequence?
        try {
            if (isValidValue(str)) {
                var applicationInfo: ApplicationInfo? = null
                try {
                    applicationInfo = pm!!.getApplicationInfo(str!!, 0)
                    charSequence = pm!!.getApplicationLabel(applicationInfo)
                } catch (unused: Exception) {
                    charSequence = str
                }
                mBinding.tAppName.text = charSequence
                mBinding.tAppPackageName.text = str
                if (applicationInfo == null) {
                    mBinding.iAppIcon.setImageResource(
                        res!!.getIdentifier(
                            "ic_block_black_48dp", "mipmap",
                            packageName
                        )
                    )
                    return
                }
                try {
                    mBinding.iAppIcon.setImageDrawable(pm!!.getApplicationIcon(applicationInfo.packageName))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e2: Exception) {
            e2.printStackTrace()
        }
    }

    private fun initAlarmView(context: Context?) {
        var i = 1
        if (currentSchedulingId <= 0 || currentSchedulingDateStart <= 0) {
            calendar.time = Date()
        } else {
            alarmSet = true
            calendar.time = Date(currentSchedulingDateStart)
            mBinding.tHour.text =
                getTime(currentSchedulingDateStart, 3)
        }
        mBinding.tDate.text =
            getDate(calendar.timeInMillis, 2)
        var i2 = 0
        mBinding.cConfirmTask.isChecked = currentSchedulingConfirmTask == 1
        mBinding.cRepeat.isChecked = currentSchedulingRepeat == 1
        val view = mBinding.lInterval
        if (currentSchedulingRepeat != 1) {
            i2 = 8
        }
        view.visibility = i2
        val editText = mBinding.eInterval
        val i3 = currentSchedulingInterval
        if (i3 > 0) {
            i = i3
        }
        editText.setText(i.toString())
        mBinding.sIntervals.setSelection(
            getSpinnerSelectionByIntervalMultiplier(
                currentSchedulingIntervalMultiplier
            )
        )
        refreshAlarmView(context)
    }

    private fun setAlarm() {
        AlarmReceiver.setAlarm(
            activity,
            currentSchedulingDateStart,
            currentSchedulingDateEnd,
            currentSchedulingInterval,
            currentSchedulingIntervalMultiplier,
            currentSchedulingConfirmTask, currentSchedulingRepeat, currentSchedulingId, mTemplateId
        )
    }

    private fun cancelAlarm() {
        AlarmReceiver.cancelAlarms(activity!!, currentSchedulingId)
    }

    private fun refreshAlarm() {
        cancelAlarm()
        setAlarm()
    }

    private fun registerAlarm(z: Boolean) {
        if (currentSchedulingStatus == 1) {
            if (alarmChanged || z) {
                refreshAlarm()
            }
        } else if (alarmChanged && !z) {
            cancelAlarm()
        }
        alarmChanged = false
    }

    class TimePickerFragment : DialogFragment(),
        OnTimeSetListener {
        override fun onCreateDialog(bundle: Bundle?): Dialog {
            val instance = Calendar.getInstance()
            return TimePickerDialog(
                activity, this, instance[11], instance[12], DateFormat.is24HourFormat(
                    activity
                )
            )
        }

        override fun onTimeSet(timePicker: TimePicker, i: Int, i2: Int) {
            if (timePicker.isShown) {
                calendar[11] = i
                calendar[12] = i2
                calendar[13] = 0
                calendar[14] = 0
                mHour!!.text =
                    getTime(calendar.timeInMillis, 3)
                enableAlarm(activity, true)
                showDatePickerDialog(parentFragmentManager)
            }
        }
    }

    class DatePickerFragment : DialogFragment(),
        OnDateSetListener {
        override fun onCreateDialog(bundle: Bundle?): Dialog {
            val instance = Calendar.getInstance()
            return DatePickerDialog(requireActivity(), this, instance[1], instance[2], instance[5])
        }

        override fun onDateSet(datePicker: DatePicker, i: Int, i2: Int, i3: Int) {
            if (datePicker.isShown) {
                calendar[1] = i
                calendar[2] = i2
                calendar[5] = i3
                mDate!!.text =
                    getDate(calendar.timeInMillis, 2)
                enableAlarm(activity, true)
            }
        }
    }

    override fun onKeyDown(i: Int, keyEvent: KeyEvent?): Boolean {
        if (i != 4) {
            return super.onKeyDown(i, keyEvent)
        }
        onKeyDownAction()
        return true
    }

    private fun onKeyDownAction() {
        if (!configurationChanged()) {
            finish()
        } else if (newScheduleService) {
            showExitDialogConfirmation(res!!.getString(R.string.wizard_exit))
        } else {
            showExitDialogConfirmation(res!!.getString(R.string.service_changed_exit))
        }
    }

    private fun showExitDialogConfirmation(str: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(res!!.getString(R.string.exit))
        builder.setMessage(str)
        builder.setPositiveButton(R.string.yes) { dialogInterface, _ ->
            dialogInterface.dismiss()
            finish()
        }
        builder.setNeutralButton(R.string.cancel) { dialogInterface, _ -> dialogInterface.dismiss() }
        builder.create().show()
    }

    private fun initViews() {
        mBinding = ActivityAddScheduleBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        setSupportActionBar(mBinding.tTopBar)
        // add back arrow to toolbar
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu_add_template, menu)
        return true
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        val itemId = menuItem.itemId
        return when {
            itemId == android.R.id.home -> {
                onKeyDownAction()
                false
            }
            itemId == R.id.runTemplate -> {
                if (!isValidValue(currentPackageName)) {
                    return false
                }
                openApp(activity!!, currentPackageName)
                false
            }
            itemId != R.id.saveTemplate -> {
                false
            }
            else -> {
                if (mScheduleTemplateModel != null) {
                    showConfirmation(getStringFromResources(R.string.save, activity!!))
                    return false
                }
                addScheduleTemplate(newScheduleService)
                false
            }
        }
    }

    private fun resetFields() {
        currentName = ""
        currentDescription = ""
        currentText = ""
        currentSchedulingDateStart = 0
        currentSchedulingDateEnd = 0
        currentSchedulingConfirmTask = 0
        currentSchedulingRepeat = 0
        currentSchedulingInterval = 1
        currentSchedulingIntervalMultiplier = 86400000
    }

    private fun validate(isNewScheduleService: Boolean): Boolean {
        resetFields()
        val mName = mBinding.eName.text
        val mDesc = mBinding.eDescription.text
        val mText = mBinding.eText.text
        val mInterval = mBinding.eInterval.text
        return if (!isValidValue(currentPackageName)) {
            Toast.makeText(this, R.string.empty_fields_message, Toast.LENGTH_LONG).show()
            false
        } else if (mBinding.cRepeat.isChecked && TextUtils.isEmpty(mInterval)) {
            Toast.makeText(this, R.string.empty_fields_message, Toast.LENGTH_LONG).show()
            false
        } else if (!hasAlarm() || currentSchedulingStatus != 1 || calendar.timeInMillis > getCurrentDate()) {
            currentName = mName.toString()
            currentDescription = mDesc.toString()
            currentText = mText.toString()
            if (hasAlarm()) {
                currentSchedulingDateStart = calendar.timeInMillis
            }
            currentSchedulingConfirmTask = if (mBinding.cConfirmTask.isChecked) 1 else 0
            currentSchedulingRepeat = if (mBinding.cRepeat.isChecked) 1 else 0
            if (mBinding.cRepeat.isChecked) {
                currentSchedulingInterval = mInterval.toString().toInt()
                currentSchedulingIntervalMultiplier = getIntervalMultiplierBySpinnerSelection(
                    mBinding.sIntervals.selectedItemPosition
                )
            }
            true
        } else {
            Toast.makeText(this, R.string.set_future_date, Toast.LENGTH_LONG).show()
            false
        }
    }

    private fun addScheduleTemplate(isNewScheduleService: Boolean) {
        try {
            if (validate(isNewScheduleService)) {
                val schedulingIds: ArrayList<Int> = mScheduleTemplateDao!!.getSchedulingIds()
                allAlarmIds = schedulingIds
                val uniqueId = getUniqueId(schedulingIds)
                currentSchedulingId = uniqueId
                val createTemplateModel = mScheduleTemplateDao!!.createTemplate(
                    currentName,
                    currentDescription,
                    currentPackageName,
                    currentText, 1, currentSchedulingStatus,
                    currentSchedulingDateStart,
                    currentSchedulingDateEnd,
                    currentSchedulingRepeat,
                    currentSchedulingInterval,
                    currentSchedulingIntervalMultiplier, currentSchedulingConfirmTask, uniqueId
                )
                mTemplateId = createTemplateModel!!.getId()
                registerAlarm(isNewScheduleService)
                val bundle = Bundle()
                bundle.putLong(MainActivity.EXTRA_NEW_SCHEDULE, createTemplateModel.getId())
                startGenericActivity(
                    activity!!, bundle, Intent.FLAG_ACTIVITY_CLEAR_TOP,
                    MainActivity::class.java
                )
                finish()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateTemplate() {
        try {
            if (validate(newScheduleService)) {
                mScheduleTemplateDao!!.updateTemplate(
                    currentName,
                    currentDescription,
                    currentPackageName,
                    currentText, 1, currentSchedulingStatus,
                    currentSchedulingDateStart,
                    currentSchedulingDateEnd,
                    currentSchedulingRepeat,
                    currentSchedulingInterval,
                    currentSchedulingIntervalMultiplier,
                    currentSchedulingConfirmTask, currentSchedulingId, mTemplateId
                )
                registerAlarm(newScheduleService)
                val bundle = Bundle()
                bundle.putLong(MainActivity.EXTRA_NEW_SCHEDULE, mTemplateId)
                startGenericActivity(
                    activity!!, bundle, Intent.FLAG_ACTIVITY_CLEAR_TOP,
                    MainActivity::class.java
                )
                finish()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showConfirmation(str: String?) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(str)
        builder.setItems(
            arrayOf<CharSequence>(
                getStringFromResources(
                    R.string.update,
                    activity!!
                ), getStringFromResources(R.string.add_new_schedule, activity!!)
            )
        ) { _: DialogInterface?, i: Int ->
            if (i == 0) {
                updateTemplate()
            } else if (i == 1) {
                addScheduleTemplate(true)
            }
        }
        builder.show()
    }

    override fun onClick(view: View) {
        val id = view.id
        if (id == R.id.lAppSelector) {
            startGenericActivity(
                activity!!, null, Intent.FLAG_ACTIVITY_NEW_TASK,
                ListAppsActivity::class.java
            )
            return
        }
        var i = 0
        var z = false
        if (id == R.id.bEnable) {
            if (alarmSet) {
                val activity2 = activity
                if (currentSchedulingStatus != 1) {
                    z = true
                }
                enableAlarm(activity2, z)
                return
            }
            showTimePickerDialog(supportFragmentManager)
        } else if (id == R.id.lHour) {
            showTimePickerDialog(supportFragmentManager)
        } else if (id == R.id.lDate) {
            if (hasAlarm()) {
                showDatePickerDialog(supportFragmentManager)
            } else {
                showToastMessage(
                    getStringFromResources(
                        R.string.no_scheduling_set,
                        activity!!
                    ), activity
                )
            }
        } else if (id == R.id.cConfirmTask) {
            alarmChanged = true
        } else if (id == R.id.cRepeat) {
            val mInterval = mBinding.lInterval
            if (!mRepeat!!.isChecked) {
                i = 8
            }
            mInterval.visibility = i
            alarmChanged = true
        }
    }

    override fun releaseResources() {
        calendar.clear()
        alarmChanged = false
        alarmSet = false
        dataChanged = false
        resetFields()
        currentSchedulingStatus = 0
        mScheduleTemplateDao!!.close()
    }

    companion object {
        const val TAG = "AddTemplateActivity"
        var alarmChanged = false
        var alarmSet = false

        @SuppressLint("StaticFieldLeak")
        var mEnable: ImageButton? = null

        @SuppressLint("StaticFieldLeak")
        var mConfirmTask: CheckBox? = null

        @SuppressLint("StaticFieldLeak")
        var mRepeat: CheckBox? = null
        var calendar: Calendar = Calendar.getInstance()
        private var currentSchedulingStatus = 0

        @SuppressLint("StaticFieldLeak")
        var mDate: TextView? = null

        @SuppressLint("StaticFieldLeak")
        var mHour: TextView? = null

        @SuppressLint("StaticFieldLeak")
        var mStatus: TextView? = null
        fun getIntervalMultiplierBySpinnerSelection(i: Int): Long {
            var j: Long = 86400000
            var j2 = if (i == 0) Constants.ALARM_MANAGER_INTERVAL_MINUTE else 86400000
            if (i == 1) {
                j2 = 3600000
            }
            if (i != 2) {
                j = j2
            }
            if (i == 3) {
                j = Constants.ALARM_MANAGER_INTERVAL_MONTH
            }
            return if (i == 4) Constants.ALARM_MANAGER_INTERVAL_YEAR else j
        }

        fun getSpinnerSelectionByIntervalMultiplier(j: Long): Int {
            var i = 2
            var i2 = if (j == Constants.ALARM_MANAGER_INTERVAL_MINUTE) 0 else 2
            if (j == 3600000L) {
                i2 = 1
            }
            if (j != 86400000L) {
                i = i2
            }
            if (j == Constants.ALARM_MANAGER_INTERVAL_MONTH) {
                i = 3
            }
            return if (j == Constants.ALARM_MANAGER_INTERVAL_YEAR) {
                4
            } else i
        }

        fun refreshAlarmView(context: Context?) {
            if (currentSchedulingStatus == 1) {
                mEnable!!.setBackgroundResource(
                    context!!.resources.getIdentifier(
                        "pink_500_circle_drawable",
                        "drawable",
                        context.packageName
                    )
                )
                mEnable!!.setImageResource(
                    context.resources.getIdentifier(
                        "com.github.fruzelee.appscheduler:mipmap/ic_alarm_on_white_24dp",
                        null,
                        null
                    )
                )
            } else {
                mEnable!!.setBackgroundResource(
                    context!!.resources.getIdentifier(
                        "blue_grey_500_circle_drawable",
                        "drawable",
                        context.packageName
                    )
                )
                mEnable!!.setImageResource(
                    context.resources.getIdentifier(
                        "com.github.fruzelee.appscheduler:mipmap/ic_alarm_off_white_24dp",
                        null,
                        null
                    )
                )
            }
            mConfirmTask!!.isEnabled = alarmSet
            mRepeat!!.isEnabled = alarmSet
        }

        fun enableAlarm(context: Context?, z: Boolean) {
            currentSchedulingStatus = if (z) 1 else 0
            alarmChanged = true
            alarmSet = true
            refreshAlarmView(context)
            if (currentSchedulingStatus == 1) {
                showToastMessage(
                    getStringFromResources(
                        R.string.scheduling_set,
                        context!!
                    ), context
                )
            } else {
                showToastMessage(
                    getStringFromResources(
                        R.string.scheduling_off,
                        context!!
                    ), context
                )
            }
        }

        fun showTimePickerDialog(fragmentManager: FragmentManager?) {
            TimePickerFragment().show(fragmentManager!!, "timePicker")
        }

        fun showDatePickerDialog(fragmentManager: FragmentManager?) {
            DatePickerFragment().show(fragmentManager!!, "datePicker")
        }
    }
}
