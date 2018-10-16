package com.myapps.ron.searchfilter.listener

import java.util.*

interface FilterListener<T> {

    fun onFiltersSelected(filters: ArrayList<T>)

    fun onNothingSelected()

    fun onFilterSelected(item: T)

    fun onFilterDeselected(item: T)

}