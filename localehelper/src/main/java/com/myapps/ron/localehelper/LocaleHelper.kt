package com.myapps.ron.localehelper

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import java.util.*

/**
 * Created by ronginat on 01/04/2019.
 *
 * https://gunhansancar.com/change-language-programmatically-in-android/
 * https://github.com/zeugma-solutions/locale-helper-android
 */

object LocaleHelper {

    private const val SELECTED_LANGUAGE = "Locale.Helper.Selected.Language"
    //private const val SELECTED_COUNTRY = "Locale.Helper.Selected.Country"

    fun onAttach(context: Context): Context {
        val locale = load(context)
        return updateResources(context, locale)
    }

    fun getLocale(context: Context): Locale {
        return load(context)
    }

    //TODO: Check why {@link #updateResources(Context, Locale)} not working
    fun setLocale(context: Context, locale: Locale): Context {
        //persist(context, locale)
        Locale.setDefault(locale)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N/*P + 20*/) updateResources(context, locale)
                else updateResourcesLegacy(context, locale)
    }

    fun isRTL(locale: Locale): Boolean {
        return Locales.RTL.contains(locale.language)
    }

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(/*LocaleHelper::class.java.name*/ context.getString(R.string.sharedPreferences), Context.MODE_PRIVATE)
    }

    /*private fun persist(context: Context, locale: Locale?) {
        if (locale == null) return
        getPreferences(context)
                .edit()
                .putString(SELECTED_LANGUAGE, locale.language)
                .putString(SELECTED_COUNTRY, locale.country)
                .apply()
    }*/

    private fun load(context: Context): Locale {
        val preferences = getPreferences(context)
        val language = preferences.getString(SELECTED_LANGUAGE, Locale.getDefault().language)
        //val country = preferences.getString(SELECTED_COUNTRY, Locale.getDefault().country)

        return Locale(language/*, country*/)
    }

    private fun updateResources(context: Context, locale: Locale): Context {
        Locale.setDefault(locale)

        val configuration = context.resources.configuration
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)

        return context.createConfigurationContext(configuration)
    }

    @Suppress("deprecation")
    private fun updateResourcesLegacy(context: Context, locale: Locale): Context {
        Locale.setDefault(locale)

        val resources = context.resources

        val configuration = resources.configuration
        configuration.locale = locale
        configuration.setLayoutDirection(locale)

        resources.updateConfiguration(configuration, resources.displayMetrics)

        return context
    }
}