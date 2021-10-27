package com.github.fruzelee.appscheduler.dao

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

/**
 * @author fazle rabbi
 * github.com/fruzelee
 * web: fr.crevado.com
 */
class DBHelper : SQLiteOpenHelper {
    constructor(context: Context?) : super(context, DATABASE_NAME, null, 1)
    constructor(context: Context?, str: String?, cursorFactory: CursorFactory?, i: Int) : super(
        context,
        DATABASE_NAME,
        cursorFactory,
        1
    )

    override fun onCreate(sQLiteDatabase: SQLiteDatabase) {
        sQLiteDatabase.execSQL(SQL_CREATE_TABLE_TEMPLATES)
        sQLiteDatabase.execSQL(SQL_CREATE_TABLE_APPS)
    }

    override fun onUpgrade(sQLiteDatabase: SQLiteDatabase, i: Int, i2: Int) {
        Log.w(TAG, "Upgrading the database from version $i to $i2")
        if (i < 1) {
            upgradeVersion2(sQLiteDatabase)
        }
    }

    private fun upgradeVersion2(sQLiteDatabase: SQLiteDatabase) {
        sQLiteDatabase.execSQL(SQL_CREATE_TABLE_APPS)
    }

    companion object {
        const val COLUMN_APP_ID = "_id"
        const val COLUMN_APP_INDEX = "position"
        const val COLUMN_APP_NAME = "app_name"
        const val COLUMN_APP_NOTIFICATION = "notification"
        const val COLUMN_APP_PACKAGE_NAME = "package_name"
        const val COLUMN_APP_STATUS = "status"
        const val COLUMN_TEMPLATE_DESCRIPTION = "description"
        const val COLUMN_TEMPLATE_ENABLED = "enabled"
        const val COLUMN_TEMPLATE_ID = "_id"
        const val COLUMN_TEMPLATE_NAME = "name"
        const val COLUMN_TEMPLATE_PACKAGE_NAME = "package_name"
        const val COLUMN_TEMPLATE_SCHEDULING_CONFIRM_TASK = "scheduling_auto_task"
        const val COLUMN_TEMPLATE_SCHEDULING_DATE_END = "scheduling_date_end"
        const val COLUMN_TEMPLATE_SCHEDULING_DATE_START = "scheduling_date_start"
        const val COLUMN_TEMPLATE_SCHEDULING_ID = "scheduling_id"
        const val COLUMN_TEMPLATE_SCHEDULING_INTERVAL = "scheduling_interval"
        const val COLUMN_TEMPLATE_SCHEDULING_INTERVAL_MULTIPLIER = "scheduling_interval_multiplier"
        const val COLUMN_TEMPLATE_SCHEDULING_REPEAT = "scheduling_repeat"
        const val COLUMN_TEMPLATE_SCHEDULING_STATUS = "scheduling_status"
        const val COLUMN_TEMPLATE_TEXT = "text"
        const val DATABASE_NAME = "app.scheduler"
        private const val DATABASE_VERSION = 1
        private const val SQL_CREATE_TABLE_APPS =
            "CREATE TABLE apps(_id INTEGER PRIMARY KEY AUTOINCREMENT, package_name TEXT NOT NULL, status INTEGER NOT NULL, position INTEGER NOT NULL, notification INTEGER NOT NULL, app_name TEXT NOT NULL );"
        private const val SQL_CREATE_TABLE_TEMPLATES =
            "CREATE TABLE templates(_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, description TEXT NOT NULL, package_name TEXT NOT NULL, text TEXT NOT NULL, enabled INTEGER NOT NULL, scheduling_status INTEGER NOT NULL, scheduling_date_start INTEGER NOT NULL, scheduling_date_end INTEGER NOT NULL, scheduling_repeat INTEGER NOT NULL, scheduling_interval INTEGER NOT NULL, scheduling_interval_multiplier INTEGER NOT NULL, scheduling_auto_task INTEGER NOT NULL, scheduling_id INTEGER NOT NULL );"
        const val TABLE_APPS = "apps"
        const val TABLE_TEMPLATES = "templates"
        const val TAG = "DBHelper"
    }
}
