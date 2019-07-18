package com.ronginat.searchfilter.listener

import com.ronginat.searchfilter.widget.FilterItem

interface FilterItemListener {

    fun onItemSelected(item: FilterItem)

    fun onItemDeselected(item: FilterItem)

    //fun onItemRemoved(item: FilterItem)

}
