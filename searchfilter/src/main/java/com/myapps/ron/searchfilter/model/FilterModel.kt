package com.myapps.ron.searchfilter.model

import java.io.Serializable


interface FilterModel : Serializable {

    fun getText(): String

    fun getSubs(): List<FilterModel>

}
