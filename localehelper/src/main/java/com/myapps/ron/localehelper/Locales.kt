package com.myapps.ron.localehelper

import java.util.*

/**
 * Created by ronginat on 01/04/2019.
 */

object Locales {
    private val English: Locale by lazy { Locale("en", "US") }
    private val Hebrew_IW: Locale by lazy { Locale("iw", "IL") }
    private val Hebrew_HE: Locale by lazy { Locale("he", "IL") }

    private val Locales: Map<String, Locale> by lazy {
        hashMapOf(
                "en" to English,
                "iw" to Hebrew_IW,
                "he" to Hebrew_HE
        )
    }

    val RTL: Set<String> by lazy {
        hashSetOf(
                "he",
                "iw"
        )
    }

    fun getLocale(language: String): Locale {
        return if (Locales.containsKey(language)) Locales[language]!! else English
    }



    /*val Turkish: Locale by lazy { Locale("tr", "TR") }
    val Romanian: Locale by lazy { Locale("ro", "RO") }
    val Polish: Locale by lazy { Locale("pl", "PL") }
    val Hindi: Locale by lazy { Locale("hi", "IN") }
    val Urdu: Locale by lazy { Locale("ur", "IN") }

    val RTL: Set<String> by lazy {
        hashSetOf(
                "ar",
                "dv",
                "fa",
                "ha",
                "he",
                "iw",
                "ji",
                "ps",
                "sd",
                "ug",
                "ur",
                "yi"
        )
    }*/
}