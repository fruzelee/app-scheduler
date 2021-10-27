package com.github.fruzelee.appscheduler.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import java.util.*

/**
 * @author fazle rabbi
 * github.com/fruzelee
 * web: fr.crevado.com
 */
object Globals {
    fun isValidValue(charSequence: CharSequence?): Boolean {
        if (charSequence != null) {
            try {
                if (charSequence.isNotEmpty()) {
                    return true
                }
            } catch (unused: Exception) {
                unused.printStackTrace()
            }
        }
        return false
    }

    fun isValidValue(str: String?): Boolean {
        if (str != null) {
            try {
                if (str.isNotEmpty()) {
                    return true
                }
            } catch (unused: Exception) {
                unused.printStackTrace()
            }
        }
        return false
    }

    fun isValidValue(strArr: Array<String?>?): Boolean {
        if (strArr != null) {
            try {
                if (strArr.isNotEmpty()) {
                    return true
                }
            } catch (unused: Exception) {
                unused.printStackTrace()
            }
        }
        return false
    }

    fun isValidValue(list: List<*>?): Boolean {
        if (list != null) {
            try {
                if (list.isNotEmpty()) {
                    return true
                }
            } catch (unused: Exception) {
                unused.printStackTrace()
            }
        }
        return false
    }

    fun getStringFromResources(i: Int, context: Context): String {
        return try {
            context.resources.getString(i)
        } catch (unused: Exception) {
            ""
        }
    }

    fun showToastMessage(str: String?, context: Context?) {
        if (str != null) {
            Toast.makeText(context, str, Toast.LENGTH_LONG).show()
        }
    }

    fun startGenericActivity(context: Context, bundle: Bundle?, i: Int, cls: Class<*>?) {
        try {
            val intent = Intent(context, cls)
            if (bundle != null) {
                intent.putExtras(bundle)
            }
            intent.addFlags(i)
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun sendGenericBroadcast(context: Context, str: String?, bundle: Bundle?) {
        try {
            val intent = Intent()
            intent.action = str
            if (bundle != null) {
                intent.putExtras(bundle)
            }
            context.sendBroadcast(intent)
        } catch (unused: Exception) {
            unused.printStackTrace()
        }
    }

    fun getUniqueId(arrayList: ArrayList<Int>): Int {
        val nextInt = Random().nextInt(Int.MAX_VALUE) + 1
        return if (!arrayList.contains(nextInt)) {
            nextInt
        } else getUniqueId(arrayList)
    }

    fun hideKeyboard(activity: Activity, view: View?) {
        if (view != null) {
            try {
                (activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
                    view.windowToken,
                    0
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun showKeyboard(activity: Activity, view: View?) {
        if (view != null) {
            try {
                (activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(
                    view,
                    0
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun openApp(context: Context, str: String?) {
        try {
            val launchIntentForPackage = context.packageManager.getLaunchIntentForPackage(
                str!!
            )
            launchIntentForPackage!!.addCategory("android.intent.category.LAUNCHER")
            launchIntentForPackage.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(launchIntentForPackage)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun canDrawOverlays(context: Context?): Boolean {
        return Build.VERSION.SDK_INT < 23 || Settings.canDrawOverlays(context)
    }
}
