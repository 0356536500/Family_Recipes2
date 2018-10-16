package com.myapps.ron.searchfilter.listener

import com.myapps.ron.searchfilter.widget.FilterItem

interface FilterItemListener {

    fun onItemSelected(item: FilterItem)

    fun onItemDeselected(item: FilterItem)

    fun onItemRemoved(item: FilterItem)

}
