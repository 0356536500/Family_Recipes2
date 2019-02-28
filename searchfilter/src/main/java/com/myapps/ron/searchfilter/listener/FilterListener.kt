package com.myapps.ron.searchfilter.listener

import androidx.annotation.NonNull
import java.util.*

interface FilterListener<T> {

    fun onFiltersSelected(@NonNull filters: ArrayList<T>)

    fun onNothingSelected()

    fun onFilterSelected(item: T)

    fun onFilterDeselected(item: T)

}