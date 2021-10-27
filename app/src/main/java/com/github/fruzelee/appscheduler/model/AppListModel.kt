package com.github.fruzelee.appscheduler.model

import java.io.Serializable

/**
 * @author fazle rabbi
 * github.com/fruzelee
 * web: fr.crevado.com
 */
class AppListModel : Serializable {
    private var _id: Long = 0
    private var app_name: String? = null
    private var available = false
    private var index: Long = 0
    private var notification = 0
    private var package_name: String? = null
    private var position: Long = 0
    private var status = 0

    constructor() {}
    constructor(j: Long, str: String?, i: Int, j2: Long, i2: Int, str2: String?) {
        _id = j
        package_name = str
        status = i
        index = j2
        notification = i2
        app_name = str2
    }

    fun get_id(): Long {
        return _id
    }

    fun set_id(j: Long) {
        _id = j
    }

    fun getPackage_name(): String? {
        return package_name
    }

    fun setPackage_name(str: String?) {
        package_name = str
    }

    fun getStatus(): Int {
        return status
    }

    fun setStatus(i: Int) {
        status = i
    }

    fun getIndex(): Long {
        return index
    }

    fun setIndex(j: Long) {
        index = j
    }

    fun getNotification(): Int {
        return notification
    }

    fun setNotification(i: Int) {
        notification = i
    }

    fun isAvailable(): Boolean {
        return available
    }

    fun setAvailable(z: Boolean) {
        available = z
    }

    fun getPosition(): Long {
        return position
    }

    fun setPosition(j: Long) {
        position = j
    }

    fun getApp_name(): String? {
        return app_name
    }

    fun setApp_name(str: String?) {
        app_name = str
    }
}
