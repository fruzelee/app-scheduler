package com.github.fruzelee.appscheduler.view.activities

import android.app.Activity
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.github.fruzelee.appscheduler.R
import com.github.fruzelee.appscheduler.databinding.ActivityPermissionSettingsBinding
import com.github.fruzelee.appscheduler.util.Globals.canDrawOverlays
import com.github.fruzelee.appscheduler.util.Globals.startGenericActivity

/**
 * @author fazle rabbi
 * github.com/fruzelee
 * web: fr.crevado.com
 */
class PermissionSettingsActivity : Activity(), View.OnClickListener, ActivityDynamics {
    private lateinit var mBinding: ActivityPermissionSettingsBinding
    private var actionSystemAlertWindowSettings = false
    private var activity: Activity? = null
    private var res: Resources? = null
    private var welcomeContext = MAIN_CONTEXT

    override fun releaseResources() {}

    public override fun onCreate(bundle: Bundle?) {
        var extras: Bundle
        super.onCreate(bundle)
        try {
            initViews()
            initValue()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        val intent = intent
        if (intent != null && intent.extras.also { extras = it!! } != null) {
            welcomeContext = intent.extras!!.getString("WELCOME_CONTEXT", MAIN_CONTEXT)
        }
    }

    public override fun onResume() {
        super.onResume()
        refreshViews()
    }

    public override fun onDestroy() {
        super.onDestroy()
        releaseResources()
    }

    private fun initViews() {
        mBinding = ActivityPermissionSettingsBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
    }

    private fun initValue() {
        activity = this
        res = resources
        mBinding.vWelcomeAction.setOnClickListener(this)
    }

    private fun refreshViews() {
        val canDrawOverlays = canDrawOverlays(activity)
        actionSystemAlertWindowSettings = false
        if (canDrawOverlays) {
            mBinding.vWelcome.setBackgroundColor(ContextCompat.getColor(this, R.color.green_500))
            mBinding.iWelcomeIndicator.setImageResource(
                res!!.getIdentifier(
                    "ic_check_circle_white_48dp", "mipmap",
                    packageName
                )
            )
            mBinding.tWelcomeTitle.text =
                res!!.getString(R.string.overlay_permission_enabled_title)
            mBinding.tWelcomeMessage.text =
                res!!.getString(R.string.overlay_permission_enabled_message)
                    .replace("#APP_NAME#", res!!.getString(R.string.app_name))
            mBinding.tWelcomeSuggestion.text =
                res!!.getString(R.string.overlay_permission_enabled_action)
                    .replace("#APP_NAME#", res!!.getString(R.string.app_name))
            mBinding.tWelcomeAction.text = res!!.getString(R.string.start_action)
            mBinding.iWelcomeActionIndicator.setImageResource(
                res!!.getIdentifier(
                    "ic_arrow_forward_black_36dp", "mipmap",
                    packageName
                )
            )
            return
        }
        actionSystemAlertWindowSettings = true
        mBinding.vWelcome.setBackgroundColor(ContextCompat.getColor(this, R.color.blue_500))
        mBinding.iWelcomeIndicator.setImageResource(
            res!!.getIdentifier(
                "ic_launch_white_48dp", "mipmap",
                packageName
            )
        )
        if (welcomeContext == WELCOME_CONTEXT) {
            mBinding.tWelcomeTitle.text = res!!.getString(R.string.enable_permission)
        } else {
            mBinding.tWelcomeTitle.text = res!!.getString(R.string.problem_detected)
        }
        mBinding.tWelcomeMessage.text =
            res!!.getString(R.string.enable_overlay_permission_message)
                .replace("#APP_NAME#", res!!.getString(R.string.app_name))
        mBinding.tWelcomeSuggestion.text =
            res!!.getString(R.string.enable_overlay_permission_action)
                .replace("#APP_NAME#", res!!.getString(R.string.app_name))
        mBinding.tWelcomeAction.text = res!!.getString(R.string.enable_permission_action)
        mBinding.iWelcomeActionIndicator.setImageResource(
            res!!.getIdentifier(
                "ic_touch_app_black_36dp", "mipmap",
                packageName
            )
        )
    }

    override fun onClick(view: View) {
        if (view.id != R.id.vWelcomeAction) {
            return
        }
        if (actionSystemAlertWindowSettings) {
            startActivity(
                Intent(
                    "android.settings.action.MANAGE_OVERLAY_PERMISSION", Uri.parse(
                        "package:$packageName"
                    )
                )
            )
            return
        }
        startGenericActivity(
            activity!!, null, Intent.FLAG_ACTIVITY_NEW_TASK,
            MainActivity::class.java
        )
        finish()
    }

    companion object {
        const val MAIN_CONTEXT = "MAIN"
        const val TAG = "PermissionSettingsActivity"
        const val WELCOME_CONTEXT = "WELCOME"
    }
}

