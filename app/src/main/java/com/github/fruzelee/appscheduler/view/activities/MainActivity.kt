package com.github.fruzelee.appscheduler.view.activities

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemLongClickListener
import androidx.appcompat.app.AppCompatActivity
import com.github.fruzelee.appscheduler.R
import com.github.fruzelee.appscheduler.dao.ScheduleTemplateDAO
import com.github.fruzelee.appscheduler.databinding.ActivityMainBinding
import com.github.fruzelee.appscheduler.model.ScheduleTemplateModel
import com.github.fruzelee.appscheduler.util.Constants
import com.github.fruzelee.appscheduler.util.Globals.canDrawOverlays
import com.github.fruzelee.appscheduler.util.Globals.startGenericActivity
import com.github.fruzelee.appscheduler.view.adapters.ListScheduleTemplatesAdapter

/**
 * @author fazle rabbi
 * github.com/fruzelee
 * web: fr.crevado.com
 */
class MainActivity : AppCompatActivity(), ActivityDynamics, OnItemClickListener {
    private lateinit var mBinding: ActivityMainBinding
    private var activity: Activity? = null
    private var canSettingsManageOverlayPermissionIntentBeHandled = false
    private var mAdapterSchedule: ListScheduleTemplatesAdapter? = null
    private var mListScheduleTemplateModels: List<ScheduleTemplateModel>? = null
    private var mScheduleTemplateDao: ScheduleTemplateDAO? = null
    private var notificationReceiver: BroadcastReceiver? = null
    private var res: Resources? = null

    public override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        notificationReceiver = Receiver()
        val intentFilter = IntentFilter(Constants.BROADCAST_ACTION_SERVICE_STATUS_CHANGED)
        intentFilter.addAction(Constants.BROADCAST_ACTION_SERVICE_RUNNING)
        registerReceiver(notificationReceiver, intentFilter)
        try {
            initViews()
            initValue()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    public override fun onStart() {
        super.onStart()
        sendToWelcome()
    }

    public override fun onResume() {
        super.onResume()
        if (dataChanged) {
            createList()
            dataChanged = false
        }
        val listTemplatesAdapter = mAdapterSchedule
        listTemplatesAdapter?.notifyDataSetChanged()
    }

    public override fun onDestroy() {
        super.onDestroy()
        releaseResources()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        val bundle = Bundle()
        val itemId = menuItem.itemId
        if (itemId == android.R.id.home) {
            finish()
        } else if (itemId == R.id.overlaySettings) {
            if (canSettingsManageOverlayPermissionIntentBeHandled && Build.VERSION.SDK_INT >= 23) {
                startActivity(
                    Intent(
                        "android.settings.action.MANAGE_OVERLAY_PERMISSION", Uri.parse(
                            "package:$packageName"
                        )
                    )
                )
            }
        } else if (itemId == R.id.addTemplate) {
            val list2 = mListScheduleTemplateModels
            bundle.putInt(EXTRA_LIST_SIZE, list2?.size ?: 0)
            startGenericActivity(
                activity!!, bundle, Intent.FLAG_ACTIVITY_CLEAR_TOP,
                AddScheduleActivity::class.java
            )
        }
        return false
    }

    private fun initViews() {
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
    }

    private fun initValue() {
        activity = this
        res = resources
        setSupportActionBar(mBinding.tTopBar)
        mBinding.lTemplates.onItemClickListener = this
        mScheduleTemplateDao = ScheduleTemplateDAO(this)
        val sb = StringBuilder()
        sb.append("package:")
        sb.append(packageName)
        val z = Intent(
            "android.settings.action.MANAGE_OVERLAY_PERMISSION",
            Uri.parse(sb.toString())
        ).resolveActivity(
            packageManager
        ) != null
        canSettingsManageOverlayPermissionIntentBeHandled = z
        if (!z || canDrawOverlays(activity)) {
            createList()
        }
    }

    public override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val extras = intent.extras
        if (extras != null) {
            val j = extras.getLong(EXTRA_NEW_SCHEDULE, -1)
            if (j != -1L) {
                createList()
            }
        }
    }

    fun createList() {
        mListScheduleTemplateModels = mScheduleTemplateDao!!.getAllTemplates()
        val listTemplatesAdapter = ListScheduleTemplatesAdapter(
            this, mListScheduleTemplateModels,
            mScheduleTemplateDao!!
        )
        mAdapterSchedule = listTemplatesAdapter
        mBinding.lTemplates.adapter = listTemplatesAdapter
        refreshList()
    }

    private fun refreshList() {
        val list = mListScheduleTemplateModels
        var i = View.VISIBLE
        val z = list == null || list.isEmpty()
        mBinding.tEmptyListTemplates.visibility = if (z) View.VISIBLE else View.GONE
        val listView = mBinding.lTemplates
        if (z) {
            i = View.GONE
        }
        listView.visibility = i
    }

    override fun onItemClick(adapterView: AdapterView<*>?, view: View?, i: Int, j: Long) {
        val item = mAdapterSchedule!!.getItem(i)
        val bundle = Bundle()
        bundle.putSerializable(EXTRA_SCHEDULE, item)
        val list = mListScheduleTemplateModels
        bundle.putInt(EXTRA_LIST_SIZE, list?.size ?: 0)
        startGenericActivity(
            activity!!, bundle, Intent.FLAG_ACTIVITY_NEW_TASK,
            AddScheduleActivity::class.java
        )
    }

    override fun releaseResources() {
        try {
            unregisterReceiver(notificationReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mScheduleTemplateDao!!.close()
    }

    private inner class Receiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            try {
                val action = intent.action
                var c = 65535.toChar()
                val hashCode = action.hashCode()
                if (hashCode != -1028762965) {
                    if (hashCode == 2059833531) {
                        if (action == Constants.BROADCAST_ACTION_SERVICE_STATUS_CHANGED) {
                            c = 0.toChar()
                        }
                    }
                } else if (action == Constants.BROADCAST_ACTION_SERVICE_RUNNING) {
                    c = 1.toChar()
                }
                if (c.code == 0) {
                    val i = intent.extras!!.getInt("POSITION")
                    mListScheduleTemplateModels!![i].setSchedulingStatus(
                        intent.extras!!.getInt("STATUS")
                    )
                    if (mAdapterSchedule != null) {
                        mAdapterSchedule!!.notifyDataSetChanged()
                    }
                } else if (c.code == 1) {
                    if (mAdapterSchedule != null) {
                        mAdapterSchedule!!.notifyDataSetChanged()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun sendToWelcome() {
        try {
            if (canSettingsManageOverlayPermissionIntentBeHandled && !canDrawOverlays(
                    activity
                )
            ) {
                val bundle = Bundle()
                bundle.putString(
                    "WELCOME_CONTEXT",
                    PermissionSettingsActivity.WELCOME_CONTEXT
                )
                startGenericActivity(
                    activity!!, bundle, Intent.FLAG_ACTIVITY_NEW_TASK,
                    PermissionSettingsActivity::class.java
                )
                finish()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        const val EXTRA_LIST_SIZE = "extra_list_size"
        const val EXTRA_NEW_SCHEDULE = "extra_key_new_schedule"
        const val EXTRA_SCHEDULE = "schedule"
        const val TAG = "MainActivity"
        var dataChanged = false
    }
}
