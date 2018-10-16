package com.myapps.ron.searchfilter.model

import java.io.Serializable


interface FilterModel<T> : Serializable {

    fun getText(): String

    fun getSubs(): List<T>

}
