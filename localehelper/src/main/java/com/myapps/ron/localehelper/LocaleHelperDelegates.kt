package com.myapps.ron.localehelper

import android.app.Activity
import android.content.Context
import android.view.View
import java.util.*

/**
 * Created by ronginat on 01/04/2019.
 */

interface LocaleHelperActivityDelegate {
    fun setLocale(activity: Activity, newLocale: Locale)
    fun attachBaseContext(newBase: Context): Context
    fun onPaused()
    fun onResumed(activity: Activity)
    fun onCreate(activity: Activity)
}

class LocaleHelperActivityDelegateImpl : LocaleHelperActivityDelegate {
    override fun onCreate(activity: Activity) {
        activity.window.decorView.layoutDirection =
                if (LocaleHelper.isRTL(Locale.getDefault())) View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR

    }

    private var locale: Locale = Locale.getDefault()

    override fun setLocale(activity: Activity, newLocale: Locale) {
        LocaleHelper.setLocale(activity, newLocale)
        locale = newLocale
        activity.recreate()
    }

    override fun attachBaseContext(newBase: Context): Context {
        return LocaleHelper.onAttach(newBase)
    }

    override fun onPaused() {
        locale = Locale.getDefault()
    }

    override fun onResumed(activity: Activity) {
        if (locale.language == Locale.getDefault().language) return

        activity.recreate()
    }
}

class LocaleHelperApplicationDelegate {
    fun attachBaseContext(base: Context): Context {
        return LocaleHelper.onAttach(base)
    }

    fun onConfigurationChanged(context: Context) {
        LocaleHelper.onAttach(context)
    }
}