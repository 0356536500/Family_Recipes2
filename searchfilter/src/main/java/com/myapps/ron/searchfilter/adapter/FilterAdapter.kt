package com.myapps.ron.searchfilter.adapter

import com.myapps.ron.searchfilter.widget.FilterItem

abstract class FilterAdapter<T>(open val items: List<T>) {

    //abstract fun createView(item: T): FilterItem

    abstract fun createView(position: Int, item: T): FilterItem

    abstract fun createSubCategory(position: Int, item: T, parent: FilterItem): FilterItem

}
