package com.github.fruzelee.appscheduler.view.adapters

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.github.fruzelee.appscheduler.R
import com.github.fruzelee.appscheduler.model.AppListModel

/**
 * @author fazle rabbi
 * github.com/fruzelee
 * web: fr.crevado.com
 */
class ListAppsAdapter(context: Context, list: List<AppListModel>) :
    BaseAdapter() {
    private val mContext: Context
    private val mInflater: LayoutInflater
    private var mItems: List<AppListModel>? = null
    private val pm: PackageManager
    private val res: Resources
    override fun getCount(): Int {
        return if (getItems() == null || getItems()!!.isEmpty()) {
            0
        } else getItems()!!.size
    }

    override fun getItem(i: Int): AppListModel? {
        return if (getItems() == null || getItems()!!.isEmpty()) {
            null
        } else getItems()!![i]
    }

    override fun getItemId(i: Int): Long {
        return if (getItems() == null || getItems()!!.isEmpty()) i.toLong() else getItems()!![i].get_id()
    }

    override fun getView(i: Int, view: View?, viewGroup: ViewGroup?): View? {
        var view = view
        var viewHolder: ViewHolder? = null
        if (view == null) {
            try {
                view = mInflater.inflate(R.layout.list_item_app, viewGroup, false)
                viewHolder = ViewHolder()
                viewHolder.name = view.findViewById(R.id.name)
                viewHolder.packageName = view.findViewById(R.id.packageName)
                viewHolder.icon = view.findViewById(R.id.icon)
                viewHolder.status = view.findViewById(R.id.status)
                view.tag = viewHolder
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            viewHolder = view.tag as ViewHolder
        }
        val item = getItem(i)
        if (item != null) {
            var applicationInfo: ApplicationInfo? = null
            res.getString(R.string.unavailable)
            try {
                applicationInfo = pm.getApplicationInfo(item.getPackage_name()!!, 0)
                pm.getApplicationLabel(applicationInfo)
            } catch (unused: Exception) {
                unused.printStackTrace()
            }
            viewHolder!!.name!!.text = item.getApp_name()
            if (applicationInfo == null) {
                viewHolder.packageName!!.text = item.getPackage_name()
                viewHolder.icon!!.setImageResource(
                    res.getIdentifier(
                        "ic_block_black_48dp",
                        "mipmap",
                        mContext.packageName
                    )
                )
            } else {
                viewHolder.packageName!!.text = applicationInfo.packageName
                try {
                    viewHolder.icon!!.setImageDrawable(pm.getApplicationIcon(applicationInfo.packageName))
                } catch (e2: Exception) {
                    e2.printStackTrace()
                }
            }
        }
        return view
    }

    private fun getItems(): List<AppListModel>? {
        return mItems
    }

    private fun setItems(list: List<AppListModel>?) {
        mItems = list
    }

    internal class ViewHolder {
        var icon: ImageView? = null
        var name: TextView? = null
        var packageName: TextView? = null
        var status: ImageView? = null
    }

    companion object {
        const val TAG = "ListAppsAdapter"
    }

    init {
        setItems(list)
        mInflater = LayoutInflater.from(context)
        mContext = context
        res = context.resources
        pm = mContext.packageManager
    }
}