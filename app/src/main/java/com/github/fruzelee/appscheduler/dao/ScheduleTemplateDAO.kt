package com.github.fruzelee.appscheduler.dao

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.github.fruzelee.appscheduler.model.ScheduleTemplateModel
import com.github.fruzelee.appscheduler.receiver.AlarmReceiver
import java.lang.Exception
import java.sql.SQLException
import java.util.*

/**
 * @author fazle rabbi
 * github.com/fruzelee
 * web: fr.crevado.com
 */
class ScheduleTemplateDAO(context: Context?) {
    private val mAllColumns = arrayOf(
        "_id",
        DBHelper.COLUMN_TEMPLATE_NAME,
        DBHelper.COLUMN_TEMPLATE_DESCRIPTION,
        "package_name",
        DBHelper.COLUMN_TEMPLATE_TEXT,
        DBHelper.COLUMN_TEMPLATE_ENABLED,
        DBHelper.COLUMN_TEMPLATE_SCHEDULING_STATUS,
        DBHelper.COLUMN_TEMPLATE_SCHEDULING_DATE_START,
        DBHelper.COLUMN_TEMPLATE_SCHEDULING_DATE_END,
        DBHelper.COLUMN_TEMPLATE_SCHEDULING_REPEAT,
        DBHelper.COLUMN_TEMPLATE_SCHEDULING_INTERVAL,
        DBHelper.COLUMN_TEMPLATE_SCHEDULING_INTERVAL_MULTIPLIER,
        DBHelper.COLUMN_TEMPLATE_SCHEDULING_CONFIRM_TASK,
        DBHelper.COLUMN_TEMPLATE_SCHEDULING_ID
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

    fun createTemplate(
        name: String?,
        desc: String?,
        packageName: String?,
        text: String?,
        isEnabled: Int,
        status: Int,
        startDate: Long,
        endDate: Long,
        isRepeat: Int,
        interval: Int,
        intervalMultiplier: Long,
        confirmTask: Int,
        id: Int
    ): ScheduleTemplateModel? {
        val contentValues = ContentValues()
        contentValues.put(DBHelper.COLUMN_TEMPLATE_NAME, name)
        contentValues.put(DBHelper.COLUMN_TEMPLATE_DESCRIPTION, desc)
        contentValues.put("package_name", packageName)
        contentValues.put(DBHelper.COLUMN_TEMPLATE_TEXT, text)
        contentValues.put(DBHelper.COLUMN_TEMPLATE_ENABLED, isEnabled)
        contentValues.put(DBHelper.COLUMN_TEMPLATE_SCHEDULING_STATUS, status)
        contentValues.put(DBHelper.COLUMN_TEMPLATE_SCHEDULING_DATE_START, startDate)
        contentValues.put(DBHelper.COLUMN_TEMPLATE_SCHEDULING_DATE_END, endDate)
        contentValues.put(DBHelper.COLUMN_TEMPLATE_SCHEDULING_REPEAT, isRepeat)
        contentValues.put(DBHelper.COLUMN_TEMPLATE_SCHEDULING_INTERVAL, interval)
        contentValues.put(DBHelper.COLUMN_TEMPLATE_SCHEDULING_INTERVAL_MULTIPLIER, intervalMultiplier)
        contentValues.put(DBHelper.COLUMN_TEMPLATE_SCHEDULING_CONFIRM_TASK, confirmTask)
        contentValues.put(DBHelper.COLUMN_TEMPLATE_SCHEDULING_ID, id)
        val insert = mDatabase!!.insert(DBHelper.TABLE_TEMPLATES, null, contentValues)
        val sQLiteDatabase = mDatabase
        val strArr = mAllColumns
        val query = sQLiteDatabase!!.query(
            DBHelper.TABLE_TEMPLATES, strArr,
            "_id = $insert", null, null, null, null
        )
        query.moveToFirst()
        val cursorToTemplateModel = cursorToTemplate(query)
        query.close()
        return cursorToTemplateModel
    }

    fun updateTemplate(i: Int, j: Long, str: String) {
        mDatabase!!.execSQL("UPDATE templates SET $str = $i WHERE _id = $j")
    }

    fun updateTemplate(j: Long, j2: Long, str: String) {
        mDatabase!!.execSQL("UPDATE templates SET $str = $j WHERE _id = $j2")
    }

    fun updateTemplate(
        name: String?,
        desc: String?,
        packageName: String?,
        text: String?,
        isEnabled: Int,
        status: Int,
        startDate: Long,
        endDate: Long,
        isRepeat: Int,
        interval: Int,
        intervalMultiplier: Long,
        confirmTask: Int,
        id: Int,
        j4: Long
    ): Int {
        val contentValues = ContentValues()
        contentValues.put(DBHelper.COLUMN_TEMPLATE_NAME, name)
        contentValues.put(DBHelper.COLUMN_TEMPLATE_DESCRIPTION, desc)
        contentValues.put("package_name", packageName)
        contentValues.put(DBHelper.COLUMN_TEMPLATE_TEXT, text)
        contentValues.put(DBHelper.COLUMN_TEMPLATE_ENABLED, isEnabled)
        contentValues.put(DBHelper.COLUMN_TEMPLATE_SCHEDULING_STATUS, status)
        contentValues.put(DBHelper.COLUMN_TEMPLATE_SCHEDULING_DATE_START, startDate)
        contentValues.put(DBHelper.COLUMN_TEMPLATE_SCHEDULING_DATE_END, endDate)
        contentValues.put(DBHelper.COLUMN_TEMPLATE_SCHEDULING_REPEAT, isRepeat)
        contentValues.put(DBHelper.COLUMN_TEMPLATE_SCHEDULING_INTERVAL, interval)
        contentValues.put(DBHelper.COLUMN_TEMPLATE_SCHEDULING_INTERVAL_MULTIPLIER, intervalMultiplier)
        contentValues.put(DBHelper.COLUMN_TEMPLATE_SCHEDULING_CONFIRM_TASK, confirmTask)
        contentValues.put(DBHelper.COLUMN_TEMPLATE_SCHEDULING_ID, id)
        return mDatabase!!.update(
            DBHelper.TABLE_TEMPLATES,
            contentValues,
            "_id = ?",
            arrayOf(j4.toString())
        )
    }

    fun getTemplate(id: Long): ScheduleTemplateModel? {
        ScheduleTemplateModel()
        var scheduleTemplateModel: ScheduleTemplateModel? = null
        val rawQuery = mDatabase!!.rawQuery("SELECT * FROM templates WHERE _id = $id", null)
        if (rawQuery.moveToFirst() && rawQuery.count != 0) {
            scheduleTemplateModel = cursorToTemplate(rawQuery)
        }
        rawQuery.close()
        return scheduleTemplateModel
    }

    fun getAllTemplates(): List<ScheduleTemplateModel> {
        val arrayList: ArrayList<ScheduleTemplateModel> = ArrayList<ScheduleTemplateModel>()
        val rawQuery = mDatabase!!.rawQuery(
            "SELECT * FROM templates ORDER BY scheduling_status DESC , scheduling_date_start ASC ",
            null
        )
        rawQuery.moveToFirst()
        while (!rawQuery.isAfterLast) {
            cursorToTemplate(rawQuery)?.let { arrayList.add(it) }
            rawQuery.moveToNext()
        }
        rawQuery.close()
        return arrayList
    }

    fun getSchedulingIds(): ArrayList<Int> {
        val arrayList = ArrayList<Int>()
        val rawQuery = mDatabase!!.rawQuery(
            "SELECT scheduling_id FROM templates WHERE scheduling_id > 0",
            null
        )
        rawQuery.moveToFirst()
        while (!rawQuery.isAfterLast) {
            arrayList.add(rawQuery.getInt(rawQuery.getColumnIndex(DBHelper.COLUMN_TEMPLATE_SCHEDULING_ID)))
            rawQuery.moveToNext()
        }
        rawQuery.close()
        return arrayList
    }

    fun setAlarms(context: Context?) {
        val rawQuery =
            mDatabase!!.rawQuery("SELECT * FROM templates WHERE scheduling_status = 1", null)
        rawQuery.moveToFirst()
        while (!rawQuery.isAfterLast) {
            AlarmReceiver.setAlarm(
                context,
                rawQuery.getLong(rawQuery.getColumnIndex(DBHelper.COLUMN_TEMPLATE_SCHEDULING_DATE_START)),
                rawQuery.getLong(rawQuery.getColumnIndex(DBHelper.COLUMN_TEMPLATE_SCHEDULING_DATE_END)),
                rawQuery.getInt(rawQuery.getColumnIndex(DBHelper.COLUMN_TEMPLATE_SCHEDULING_INTERVAL)),
                rawQuery.getLong(rawQuery.getColumnIndex(DBHelper.COLUMN_TEMPLATE_SCHEDULING_INTERVAL_MULTIPLIER)),
                rawQuery.getInt(rawQuery.getColumnIndex(DBHelper.COLUMN_TEMPLATE_SCHEDULING_CONFIRM_TASK)),
                rawQuery.getInt(rawQuery.getColumnIndex(DBHelper.COLUMN_TEMPLATE_SCHEDULING_REPEAT)),
                rawQuery.getInt(rawQuery.getColumnIndex(DBHelper.COLUMN_TEMPLATE_SCHEDULING_ID)),
                rawQuery.getLong(rawQuery.getColumnIndex("_id"))
            )
            rawQuery.moveToNext()
        }
        rawQuery.close()
    }

    private fun cursorToTemplate(cursor: Cursor?): ScheduleTemplateModel? {
        if (cursor == null || cursor.count == 0) {
            return null
        }
        val scheduleTemplateModel = ScheduleTemplateModel()
        try {
            val columnIndex = cursor.getColumnIndex("_id")
            if (columnIndex == -1) {
                return null
            }
            scheduleTemplateModel.setId(cursor.getLong(columnIndex))
            val columnIndex2 = cursor.getColumnIndex(DBHelper.COLUMN_TEMPLATE_NAME)
            if (columnIndex2 != -1) {
                scheduleTemplateModel.setName(cursor.getString(columnIndex2))
            }
            val columnIndex3 = cursor.getColumnIndex(DBHelper.COLUMN_TEMPLATE_DESCRIPTION)
            if (columnIndex3 != -1) {
                scheduleTemplateModel.setDescription(cursor.getString(columnIndex3))
            }
            val columnIndex4 = cursor.getColumnIndex("package_name")
            if (columnIndex4 != -1) {
                scheduleTemplateModel.setPackageName(cursor.getString(columnIndex4))
            }
            val columnIndex5 = cursor.getColumnIndex(DBHelper.COLUMN_TEMPLATE_TEXT)
            if (columnIndex5 != -1) {
                scheduleTemplateModel.setText(cursor.getString(columnIndex5))
            }
            val columnIndex6 = cursor.getColumnIndex(DBHelper.COLUMN_TEMPLATE_ENABLED)
            if (columnIndex6 != -1) {
                scheduleTemplateModel.setEnabled(cursor.getInt(columnIndex6))
            }
            val columnIndex7 = cursor.getColumnIndex(DBHelper.COLUMN_TEMPLATE_SCHEDULING_STATUS)
            if (columnIndex7 != -1) {
                scheduleTemplateModel.setSchedulingStatus(cursor.getInt(columnIndex7))
            }
            val columnIndex8 = cursor.getColumnIndex(DBHelper.COLUMN_TEMPLATE_SCHEDULING_DATE_START)
            if (columnIndex8 != -1) {
                scheduleTemplateModel.setSchedulingDateStart(cursor.getLong(columnIndex8))
            }
            val columnIndex9 = cursor.getColumnIndex(DBHelper.COLUMN_TEMPLATE_SCHEDULING_DATE_END)
            if (columnIndex9 != -1) {
                scheduleTemplateModel.setSchedulingDateEnd(cursor.getLong(columnIndex9))
            }
            val columnIndex10 = cursor.getColumnIndex(DBHelper.COLUMN_TEMPLATE_SCHEDULING_REPEAT)
            if (columnIndex10 != -1) {
                scheduleTemplateModel.setSchedulingRepeat(cursor.getInt(columnIndex10))
            }
            val columnIndex11 = cursor.getColumnIndex(DBHelper.COLUMN_TEMPLATE_SCHEDULING_INTERVAL)
            if (columnIndex11 != -1) {
                scheduleTemplateModel.setSchedulingInterval(cursor.getInt(columnIndex11))
            }
            val columnIndex12 =
                cursor.getColumnIndex(DBHelper.COLUMN_TEMPLATE_SCHEDULING_INTERVAL_MULTIPLIER)
            if (columnIndex12 != -1) {
                scheduleTemplateModel.setSchedulingIntervalMultiplier(cursor.getLong(columnIndex12))
            }
            val columnIndex13 =
                cursor.getColumnIndex(DBHelper.COLUMN_TEMPLATE_SCHEDULING_CONFIRM_TASK)
            if (columnIndex13 != -1) {
                scheduleTemplateModel.setSchedulingConfirmTask(cursor.getInt(columnIndex13))
            }
            val columnIndex14 = cursor.getColumnIndex(DBHelper.COLUMN_TEMPLATE_SCHEDULING_ID)
            if (columnIndex14 != -1) {
                scheduleTemplateModel.setSchedulingId(cursor.getInt(columnIndex14))
            }
            return scheduleTemplateModel
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return scheduleTemplateModel
    }

    companion object {
        const val TAG = "ScheduleTemplateDAO"
    }

    init {
        try {
            open()
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }
}
