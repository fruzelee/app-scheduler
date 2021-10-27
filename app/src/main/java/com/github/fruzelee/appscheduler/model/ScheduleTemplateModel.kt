package com.github.fruzelee.appscheduler.model

import java.io.Serializable

/**
 * @author fazle rabbi
 * github.com/fruzelee
 * web: fr.crevado.com
 */
class ScheduleTemplateModel : Serializable {
    private var description: String? = null
    private var enabled = 0
    private var id: Long = 0
    private var name: String? = null
    private var packageName: String? = null
    private var schedulingConfirmTask = 0
    private var schedulingDateEnd: Long = 0
    private var schedulingDateStart: Long = 0
    private var schedulingId = 0
    private var schedulingInterval = 0
    private var schedulingIntervalMultiplier: Long = 0
    private var schedulingRepeat = 0
    private var schedulingStatus = 0
    private var text: String? = null
    fun getId(): Long {
        return id
    }

    fun setId(j: Long) {
        id = j
    }

    fun getName(): String? {
        return name
    }

    fun setName(str: String?) {
        name = str
    }

    fun getDescription(): String? {
        return description
    }

    fun setDescription(str: String?) {
        description = str
    }

    fun getPackageName(): String? {
        return packageName
    }

    fun setPackageName(str: String?) {
        packageName = str
    }

    fun getText(): String? {
        return text
    }

    fun setText(str: String?) {
        text = str
    }

    fun getEnabled(): Int {
        return enabled
    }

    fun setEnabled(i: Int) {
        enabled = i
    }

    fun getSchedulingStatus(): Int {
        return schedulingStatus
    }

    fun setSchedulingStatus(i: Int) {
        schedulingStatus = i
    }

    fun getSchedulingDateStart(): Long {
        return schedulingDateStart
    }

    fun setSchedulingDateStart(j: Long) {
        schedulingDateStart = j
    }

    fun getSchedulingDateEnd(): Long {
        return schedulingDateEnd
    }

    fun setSchedulingDateEnd(j: Long) {
        schedulingDateEnd = j
    }

    fun getSchedulingRepeat(): Int {
        return schedulingRepeat
    }

    fun setSchedulingRepeat(i: Int) {
        schedulingRepeat = i
    }

    fun getSchedulingInterval(): Int {
        return schedulingInterval
    }

    fun setSchedulingInterval(i: Int) {
        schedulingInterval = i
    }

    fun getSchedulingIntervalMultiplier(): Long {
        return schedulingIntervalMultiplier
    }

    fun setSchedulingIntervalMultiplier(j: Long) {
        schedulingIntervalMultiplier = j
    }

    fun getSchedulingConfirmTask(): Int {
        return schedulingConfirmTask
    }

    fun setSchedulingConfirmTask(i: Int) {
        schedulingConfirmTask = i
    }

    fun getSchedulingId(): Int {
        return schedulingId
    }

    fun setSchedulingId(i: Int) {
        schedulingId = i
    }
}
