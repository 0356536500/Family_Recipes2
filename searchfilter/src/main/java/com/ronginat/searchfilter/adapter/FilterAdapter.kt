package com.ronginat.searchfilter.adapter

import com.ronginat.searchfilter.widget.FilterItem

abstract class FilterAdapter<T>(open val items: List<T>) {

    abstract fun createView(item: T, parent: T?): FilterItem
}
