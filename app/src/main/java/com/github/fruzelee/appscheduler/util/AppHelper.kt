package com.github.fruzelee.appscheduler.util

import android.annotation.SuppressLint
import android.content.pm.PackageManager

/**
 * @author fazle rabbi
 * github.com/fruzelee
 * web: fr.crevado.com
 */
object AppHelper {
    fun isPackageInstalled(str: String?, packageManager: PackageManager): Boolean {
        return try {
            packageManager.getPackageInfo(str!!, 0)
            true
        } catch (unused: PackageManager.NameNotFoundException) {
            false
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    fun getInstalledPackagesCount(packageManager: PackageManager, z: Boolean): Int {
        var i = 0
        try {
            val installedApplications = packageManager.getInstalledApplications(0)
            if (!z) {
                return installedApplications.size
            }
            for (applicationInfo in installedApplications) {
                if (packageManager.getLaunchIntentForPackage(applicationInfo.packageName) != null) {
                    i++
                }
            }
            return i
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return i
    }

    fun getInstalledPackagesCount(packageManager: PackageManager, z: Boolean, z2: Boolean): Int {
        val installedPackagesCount = getInstalledPackagesCount(packageManager, z)
        return if (z2) installedPackagesCount - 1 else installedPackagesCount
    }
}
