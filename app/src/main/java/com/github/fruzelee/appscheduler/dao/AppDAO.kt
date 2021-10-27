package com.github.fruzelee.appscheduler.dao

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.github.fruzelee.appscheduler.model.AppListModel
import com.github.fruzelee.appscheduler.util.AppHelper.getInstalledPackagesCount
import com.github.fruzelee.appscheduler.util.AppHelper.isPackageInstalled
import java.lang.Exception
import java.sql.SQLException
import java.util.*

/**
 * @author fazle rabbi
 * github.com/fruzelee
 * web: fr.crevado.com
 */
class AppDAO(context: Context?) {
    private val mAllColumns = arrayOf(
        "_id",
        "package_name",
        "status",
        DBHelper.COLUMN_APP_INDEX,
        DBHelper.COLUMN_APP_NOTIFICATION,
        DBHelper.COLUMN_APP_NAME
    )
    private var mDatabase: SQLiteDatabase? = null
    private val mDbHelper: DBHelper = DBHelper(context)

    @Throws(SQLException::class)
    fun open() {
        mDatabase = mDbHelper.writableDatabase
    }

    fun close() {
        mDbHelper.close()
        mDatabase!!.close()
    }

    private fun createApp(packageName: String?, status: Int, columnAppIndex: Long, columnAppNotification: Int, columnAppName: String?): AppListModel? {
        val contentValues = ContentValues()
        contentValues.put("package_name", packageName)
        contentValues.put("status", status)
        contentValues.put(DBHelper.COLUMN_APP_INDEX, columnAppIndex)
        contentValues.put(DBHelper.COLUMN_APP_NOTIFICATION, columnAppNotification)
        contentValues.put(DBHelper.COLUMN_APP_NAME, columnAppName)
        val insert = mDatabase!!.insert(DBHelper.TABLE_APPS, null, contentValues)
        val sQLiteDatabase = mDatabase
        val strArr = mAllColumns
        val query = sQLiteDatabase!!.query(
            DBHelper.TABLE_APPS, strArr,
            "_id = $insert", null, null, null, null
        )
        query.moveToFirst()
        val cursorToAppModel = cursorToApp(query)
        query.close()
        return cursorToAppModel
    }

    private fun updateAppName(str: String?, str2: String): Int {
        val contentValues = ContentValues()
        contentValues.put(DBHelper.COLUMN_APP_NAME, str)
        return mDatabase!!.update(
            DBHelper.TABLE_APPS,
            contentValues,
            "package_name = ?",
            arrayOf(str2)
        )
    }

    private fun deleteApp(appListModel: AppListModel?) {
        if (appListModel != null) {
            val id = appListModel.get_id()
            val sQLiteDatabase = mDatabase
            sQLiteDatabase!!.delete(DBHelper.TABLE_APPS, "_id = $id", null)
        }
    }

    private fun getAllApps(): MutableList<AppListModel?> {
        val arrayList = ArrayList<AppListModel?>()
        val rawQuery = mDatabase!!.rawQuery(
            "SELECT * FROM apps ORDER BY app_name COLLATE NOCASE , package_name",
            null
        )
        rawQuery.moveToFirst()
        while (!rawQuery.isAfterLast) {
            arrayList.add(cursorToApp(rawQuery))
            rawQuery.moveToNext()
        }
        rawQuery.close()
        return arrayList
    }

    fun getAllAppsIfUnchanged(context: Context): List<AppListModel?>? {
        return try {
            val arrayList = ArrayList<AppListModel?>()
            val packageManager = context.packageManager
            val appsCount = getAppsCount()
            if (appsCount <= 0 || appsCount != getInstalledPackagesCount(
                    packageManager,
                    z = true,
                    z2 = false
                )
            ) {
                return null
            }
            val rawQuery = mDatabase!!.rawQuery(
                "SELECT * FROM apps ORDER BY app_name COLLATE NOCASE , package_name",
                null
            )
            rawQuery.moveToFirst()
            while (!rawQuery.isAfterLast) {
                val cursorToAppModel = cursorToApp(rawQuery)
                if (!isPackageInstalled(cursorToAppModel!!.getPackage_name(), packageManager)) {
                    return null
                }
                arrayList.add(cursorToAppModel)
                rawQuery.moveToNext()
            }
            rawQuery.close()
            arrayList
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun appExists(str: String): Boolean {
        var z = true
        val query = mDatabase!!.query(
            DBHelper.TABLE_APPS,
            arrayOf("_id"),
            "package_name = ?",
            arrayOf(str),
            null,
            null,
            null
        )
        if (!query.moveToFirst() || query.count == 0) {
            z = false
        }
        query.close()
        return z
    }

    private fun getAppsCount(): Int {
        val rawQuery = mDatabase!!.rawQuery("SELECT COUNT(*) FROM apps", null)
        rawQuery.moveToFirst()
        val i = rawQuery.getInt(0)
        rawQuery.close()
        return i
    }

    @SuppressLint("QueryPermissionsNeeded")
    fun refreshInstalledApplications(context: Context): List<AppListModel?> {
        var charSequence: String? = null
        var arrayList: MutableList<AppListModel?> = ArrayList()
        try {
            val packageManager = context.packageManager
            for (applicationInfo in packageManager.getInstalledApplications(0)) {
                if (packageManager.getLaunchIntentForPackage(applicationInfo.packageName) != null) {
                    try {
                        charSequence =
                            packageManager.getApplicationLabel(applicationInfo).toString()
                    } catch (unused: Exception) {
                        unused.printStackTrace()
                    }
                    if (!appExists(applicationInfo.packageName)) {
                        createApp(applicationInfo.packageName, 0, 0, 0, charSequence)
                    } else {
                        updateAppName(charSequence, applicationInfo.packageName)
                    }
                }
            }
            arrayList = getAllApps()
            for (appModel in ArrayList(arrayList)) {
                if (!isPackageInstalled(appModel!!.getPackage_name(), packageManager)) {
                    deleteApp(appModel)
                    arrayList.remove(appModel)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return arrayList
    }

    private fun cursorToApp(cursor: Cursor?): AppListModel? {
        if (cursor == null || cursor.count == 0) {
            return null
        }
        val appModel = AppListModel()
        try {
            val columnIndex = cursor.getColumnIndex("_id")
            if (columnIndex == -1) {
                return null
            }
            appModel.set_id(cursor.getLong(columnIndex))
            val columnIndex2 = cursor.getColumnIndex("package_name")
            if (columnIndex2 != -1) {
                appModel.setPackage_name(cursor.getString(columnIndex2))
            }
            val columnIndex3 = cursor.getColumnIndex("status")
            if (columnIndex3 != -1) {
                appModel.setStatus(cursor.getInt(columnIndex3))
            }
            val columnIndex4 = cursor.getColumnIndex(DBHelper.COLUMN_APP_INDEX)
            if (columnIndex4 != -1) {
                appModel.setIndex(cursor.getLong(columnIndex4))
            }
            val columnIndex5 = cursor.getColumnIndex(DBHelper.COLUMN_APP_NOTIFICATION)
            if (columnIndex5 != -1) {
                appModel.setNotification(cursor.getInt(columnIndex5))
            }
            val columnIndex6 = cursor.getColumnIndex(DBHelper.COLUMN_APP_NAME)
            if (columnIndex6 != -1) {
                appModel.setApp_name(cursor.getString(columnIndex6))
            }
            return appModel
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return appModel
    }

    companion object {
        const val TAG = "AppDAO"
    }

    init {
        try {
            open()
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }
}
