package com.github.fruzelee.appscheduler.view.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.res.Resources
import android.os.AsyncTask
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.github.fruzelee.appscheduler.R
import com.github.fruzelee.appscheduler.dao.AppDAO
import com.github.fruzelee.appscheduler.databinding.ActivityListAppsBinding
import com.github.fruzelee.appscheduler.model.AppListModel
import com.github.fruzelee.appscheduler.util.Globals.hideKeyboard
import com.github.fruzelee.appscheduler.util.Globals.isValidValue
import com.github.fruzelee.appscheduler.util.Globals.showKeyboard
import com.github.fruzelee.appscheduler.util.Globals.startGenericActivity
import com.github.fruzelee.appscheduler.view.adapters.ListAppsAdapter
import java.util.*


/**
 * @author fazle rabbi
 * github.com/fruzelee
 * web: fr.crevado.com
 */
class ListAppsActivity : AppCompatActivity(), ActivityDynamics, OnItemClickListener,
    View.OnClickListener {
    private lateinit var mBinding: ActivityListAppsBinding
    private var activity: Activity? = null
    private var fullList = true
    private var init = true
    private var mAdapter: ListAppsAdapter? = null
    private var mAppDao: AppDAO? = null
    private var mAppNamesAdapter: ArrayAdapter<String?>? = null
    private var pDialog: ProgressDialog? = null
    private var res: Resources? = null

    public override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        try {
            initViews()
            initValue()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    public override fun onResume() {
        super.onResume()
        val listAppsAdapter = mAdapter
        listAppsAdapter?.notifyDataSetChanged()
    }

    public override fun onStop() {
        super.onStop()
        dismissProgressDialog()
    }

    public override fun onDestroy() {
        super.onDestroy()
        releaseResources()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu_list_apps, menu)
        return true
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        val itemId = menuItem.itemId
        return when {
            itemId == android.R.id.home -> {
                finish()
                false
            }
            itemId != R.id.refresh -> {
                false
            }
            else -> {
                reloadList(true)
                dataChanged = true
                false
            }
        }
    }

    private fun initViews() {
        mBinding = ActivityListAppsBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        setSupportActionBar(mBinding.tTopBar)
    }

    private fun initValue() {
        activity = this
        res = resources
        mBinding.lApps.onItemClickListener = this
        mBinding.bCancelText.setOnClickListener(this)
        mBinding.bSearchApp.setOnClickListener(this)
        mBinding.eSetText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i2: Int, i3: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i2: Int, i3: Int) {}
            override fun afterTextChanged(editable: Editable) {
                if (isValidValue(editable.toString())) {
                    val allPackageNamesSuggestions =
                        getAllPackageNamesSuggestions(editable.toString())
                    if (isValidValue(allPackageNamesSuggestions)) {
                        mAppNamesAdapter = ArrayAdapter<String?>(
                            activity!!,
                            android.R.layout.simple_list_item_1,
                            allPackageNamesSuggestions!!
                        )
                        mAppNamesAdapter!!.notifyDataSetChanged()
                        mBinding.eSetText.setAdapter(mAppNamesAdapter)
                    }
                }
            }
        })
        mBinding.eSetText.onItemClickListener =
            OnItemClickListener { _: AdapterView<*>?, _: View?, _: Int, _: Long ->
                val str: String? = try {
                    mBinding.eSetText.text.toString()
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
                createList(str, false)
            }

        val listAppsAdapter = ListAppsAdapter(activity!!, mListAppListModels)
        mAdapter = listAppsAdapter
        mBinding.lApps.adapter = listAppsAdapter
        mAppDao = AppDAO(this)
        intent.extras
        createList(null, false)
    }

    private fun setEmptyListText() {
        res!!.getString(R.string.no_data)
        val str: String = if (fullList) {
            res!!.getString(R.string.no_results)
        } else {
            res!!.getString(R.string.no_info_meets_search_criteria)
        }
        mBinding.tEmptyListApps.text = str
    }

    private fun createList(str: String?, z: Boolean) {
        LoadList(str, z).execute()
    }

    override fun onClick(view: View) {
        val obj = mBinding.eSetText.text.toString()
        val id = view.id
        when {
            id == R.id.bCancelText -> {
                if (isValidValue(obj)) {
                    mBinding.eSetText.setText("")
                    return
                }
                createList(null, false)
                hideKeyboard(activity!!, mBinding.eSetText)
            }
            id != R.id.bSearchApp -> {
            }
            else -> {
                if (isValidValue(obj)) {
                    createList(obj, false)
                    return
                }
                mBinding.eSetText.requestFocus()
                showKeyboard(activity!!, mBinding.eSetText)
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    inner class LoadList(var keyword: String?, z: Boolean) :
        AsyncTask<Long?, String?, String?>() {
        private var refresh = false
        public override fun onPreExecute() {
            super.onPreExecute()
            fullList = true
            setProgressDialog(true)
        }

        override fun doInBackground(vararg params: Long?): String? {
            try {
                if (!isValidValue(keyword)) {
                    mListAppListModels.clear()
                    if (init || !isValidValue(mFullList)) {
                        if (refresh) {
                            mListAppListModels = mAppDao!!.refreshInstalledApplications(
                                activity!!
                            ) as ArrayList<AppListModel>
                        } else {
                            val allAppsIfUnchanged = mAppDao!!.getAllAppsIfUnchanged(
                                activity!!
                            )
                            if (allAppsIfUnchanged == null) {
                                mListAppListModels = mAppDao!!.refreshInstalledApplications(
                                    activity!!
                                ) as ArrayList<AppListModel>
                            } else {
                                mListAppListModels = allAppsIfUnchanged as ArrayList<AppListModel>
                            }
                        }
                        mFullList.clear()
                        mFullList = ArrayList<AppListModel?>(mListAppListModels)
                    } else {
                        mListAppListModels = ArrayList<AppListModel>(mFullList)
                    }
                    fullList = true
                } else if (!isValidValue(mFullList)) {
                    return null
                } else {
                    val arrayList: ArrayList<AppListModel?> = ArrayList<AppListModel?>()
                    val lowerCase = keyword?.lowercase(Locale.getDefault())
                    for (i in mFullList.indices) {
                        if (isCancelled) {
                            return null
                        }
                        if (mFullList[i]!!.getApp_name()!!.lowercase(Locale.getDefault())
                                .contains(lowerCase!!)
                        ) {
                            arrayList.add(mFullList[i])
                        }
                    }
                    mListAppListModels.clear()
                    mListAppListModels = ArrayList(arrayList)
                    arrayList.clear()
                    fullList = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        public override fun onPostExecute(str: String?) {
            init = false
            mAdapter = ListAppsAdapter(activity!!, mListAppListModels)
            mBinding.lApps.adapter = mAdapter
            refreshList()
            setProgressDialog(false)
        }

        init {
            refresh = z
        }
    }

    fun setProgressDialog(z: Boolean) {
        if (z) {
            try {
                if (pDialog == null) {
                    pDialog = ProgressDialog(activity)
                }
                pDialog!!.setMessage("Loading...")
                pDialog!!.isIndeterminate = false
                pDialog!!.setCancelable(false)
                pDialog!!.show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            dismissProgressDialog()
        }
    }

    private fun dismissProgressDialog() {
        try {
            val progressDialog = pDialog
            if (progressDialog != null && progressDialog.isShowing) {
                pDialog!!.dismiss()
            }
        } catch (e: Exception) {
            e.printStackTrace()
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
        if (!fullList) {
            createList(null, false)
            mBinding.eSetText.setText("")
            return
        }
        finish()
    }

    fun getAllPackageNamesSuggestions(str: String): Array<String?>? {
        if (!isValidValue(mFullList)) {
            return null
        }
        val arrayList: ArrayList<String?> = ArrayList<String?>()
        val lowerCase = str.lowercase(Locale.getDefault())
        for (appModel in mFullList) {
            if (appModel!!.getApp_name()!!.lowercase(Locale.getDefault()).contains(lowerCase)) {
                arrayList.add(appModel.getApp_name())
            }
        }
        if (!isValidValue(arrayList as List<*>)) {
            return null
        }
        val hashSet: HashSet<String?> = HashSet<String?>()
        hashSet.addAll(arrayList)
        return hashSet.toTypedArray()
    }

    private fun reloadList(z: Boolean) {
        mFullList.clear()
        mListAppListModels.clear()
        mBinding.eSetText.setText("")
        hideKeyboard(activity!!, mBinding.eSetText)
        createList(null, z)
    }

    fun refreshList() {
        val list = mListAppListModels
        var i = 0
        val z = list.isEmpty()
        mBinding.lEmptyListApps.visibility = if (z) View.VISIBLE else View.GONE
        val listView = mBinding.lApps
        if (z) {
            i = 8
        }
        listView.visibility = i
        setEmptyListText()
    }

    override fun onItemClick(adapterView: AdapterView<*>?, view: View?, i: Int, j: Long) {
        val item = mAdapter!!.getItem(i)
        val bundle = Bundle()
        bundle.putString("PACKAGE_NAME", item!!.getPackage_name())
        startGenericActivity(
            activity!!, bundle, Intent.FLAG_ACTIVITY_NEW_TASK,
            AddScheduleActivity::class.java
        )
    }

    override fun releaseResources() {
        mAppDao!!.close()
    }

    companion object {
        const val TAG = "ListAppsActivity"
        var dataChanged = false
        var mFullList: ArrayList<AppListModel?> = ArrayList<AppListModel?>()
        var mListAppListModels: ArrayList<AppListModel> = ArrayList<AppListModel>()
    }
}
