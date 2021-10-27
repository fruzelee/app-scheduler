package com.github.fruzelee.appscheduler.base

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import com.github.fruzelee.appscheduler.util.Constants

/**
 * @author fazle rabbi
 * github.com/fruzelee
 * web: fr.crevado.com
 */
class BaseApplication : Application() {

    private var appsContext: Context? = null
    private var instances: BaseApplication? = null

    fun getInstances(): BaseApplication? {
        return instances
    }

    fun getAppsContext(): Context? {
        return appsContext
    }

    private fun setStrictPolicy() {
        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
    }

    override fun onCreate() {
        appsContext = this
        instances = this

        super.onCreate()
        setStrictPolicy()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel(
                    Constants.DEFAULT_NOTIFICATION_CHANNEL_ID,
                    "Schedule Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
        }
    }
}