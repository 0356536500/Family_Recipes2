package com.myapps.ron.searchfilter.adapter

import com.myapps.ron.searchfilter.widget.FilterItem

abstract class FilterAdapter<T>(open val items: List<T>) {

    abstract fun createView(item: T, parent: T?): FilterItem
}
