package com.myapps.ron.searchfilter.listener

import android.support.annotation.Nullable
import java.util.*

interface FilterListener<T> {

    fun onFiltersSelected(@Nullable filters: ArrayList<T>)

    fun onNothingSelected()

    fun onFilterSelected(item: T)

    fun onFilterDeselected(item: T)

}