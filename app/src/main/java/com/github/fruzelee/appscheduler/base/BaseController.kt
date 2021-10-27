package com.github.fruzelee.appscheduler.base

import android.content.Context
import android.content.Intent
import android.widget.Toast

/**
 * @author fazle
 * Created 27-Oct-21 at 7:07 AM
 * github.com/fruzelee
 * web: fr.crevado.com
 */
open class BaseController {
    fun Redirect(context: Context, actCls: Class<*>?) {
        val intent = Intent(context, actCls)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        context.startActivity(intent)
    }

    fun OpenActivity(context: Context, actCls: Class<*>?) {
        val intent = Intent(context, actCls)
        context.startActivity(intent)
    }

    fun showToast(context: Context?, msg: String?) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }
}