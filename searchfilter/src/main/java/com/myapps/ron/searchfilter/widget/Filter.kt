package com.myapps.ron.searchfilter.widget

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.myapps.ron.searchfilter.Constant
import com.myapps.ron.searchfilter.R
import com.myapps.ron.searchfilter.adapter.FilterAdapter
import com.myapps.ron.searchfilter.listener.CollapseListener
import com.myapps.ron.searchfilter.listener.FilterItemListener
import com.myapps.ron.searchfilter.listener.FilterListener
import com.myapps.ron.searchfilter.model.Coord
import com.myapps.ron.searchfilter.model.FilterModel
import kotlinx.android.synthetic.main.collapsed_container.view.*
import kotlinx.android.synthetic.main.filter.view.*
import java.io.Serializable
import java.util.*
import android.graphics.Color
import android.util.Log
import android.view.animation.AlphaAnimation
import android.view.animation.Animation


class Filter<T : FilterModel<T>> : FrameLayout, FilterItemListener, CollapseListener {

    var adapter: FilterAdapter<T>? = null
    var listener: FilterListener<T>? = null
    var margin = dpToPx(getDimen(R.dimen.margin))
    var customTextView: String = ""
        set(value) {
            collapsedText.text = value
        }
    var textToReplaceArrow: String = ""
        set(value) {
            collapseView.setText(value)
        }

    var replaceArrowByText: Boolean = false
        set(value) {
            collapseView.setHasText(value)
        }

    var collapsedBackground: Int = Color.WHITE
        set(value) {
            field = value
            collapsedContainer.containerBackground = value
            collapsedContainer.invalidate()
        }

    var expandedBackground: Int = Color.WHITE
        set(value) {
            field = value
            expandedFilter.setBackgroundColor(value)
            expandedFilter.invalidate()
        }

    //private val firstExpandList: MutableList<FilterItem> = mutableListOf()
    //private var firstExpand: Boolean = true
    private var mIsBusy = false

    private var isCollapsed: Boolean? = null

    private val STATE_SUPER = "state_super"
    private val STATE_SELECTED = "state_selected"
    private val STATE_REMOVED = "state_removed"
    private val STATE_COLLAPSED = "state_collapsed"

    private var mNothingSelectedItem: FilterItem? = null
    private val mSelectedFilters: LinkedHashMap<FilterItem, Coord> = LinkedHashMap()
    private val mRemovedFilters: LinkedHashMap<FilterItem, Coord> = LinkedHashMap()
    private val mItems: LinkedHashMap<FilterItem, T> = LinkedHashMap()
    private var mSelectedItems: ArrayList<T> = ArrayList()
    private var mRemovedItems: ArrayList<T> = ArrayList()

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleRes: Int) : super(context, attrs, defStyleRes) {
        LayoutInflater.from(context).inflate(R.layout.filter, this, true)
        visibility = View.INVISIBLE
        collapseView.setOnClickListener { toggle() }
        collapsedFilter.scrollListener = this
        collapsedContainer.listener = this
        expandedFilter.listener = this
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.Filter, 0, 0)
        try {
            collapsedContainer.containerBackground = attributes.getColor(R.styleable.Filter_collapsedBackground, Color.WHITE)
            expandedFilter.setBackgroundColor(attributes.getColor(R.styleable.Filter_expandedBackground, Color.WHITE))
        } finally {
            attributes.recycle()
        }
    }

    fun build() {
        if (!validate()) {
            return
        }

        mItems.clear()
        expandedFilter.post {
            val mainItems: MutableList<FilterItem> = mutableListOf()
            adapter?.items?.forEachIndexed { i, item ->
                //add the group header to the main View
                val view: FilterItem = adapter?.createView(i, item)!!
                view.listener = this
                view.isContainer = true
                mainItems.add(i, view)
                //firstExpandList.add(view)
                expandedFilter.addView(view)
                mItems.put(view, item)
                if(view.text == customTextView) {
                    view.select()
                    mNothingSelectedItem = view
                }
            }
            val subList: MutableList<FilterItem> = mutableListOf()
            val subItems: MutableList<T> = mutableListOf()
            //var runningIndex = 0
            //add all its sub filters to View and make them invisible
            adapter?.items?.forEachIndexed { index, item ->
                if(index > 0) {
                    for ((indexInParent, subItem) in item.getSubs().withIndex()) {
                        val subView: FilterItem = adapter?.createSubCategory(indexInParent, item, mainItems.get(index))!!
                        subView.listener = this
                        subView.isContainer = false
                        subView.isHidden = true
                        subList.add(indexInParent, subView)
                        subItems.add(indexInParent, subItem)
                        //expandedFilter.addView(subView)
                        mItems.put(subView, subItem)
                    }
                    mainItems.get(index).subFilters.addAll(subList)
                    mainItems.get(index).subItems.addAll(subItems)
                    subList.clear()
                }
            }

            if (isCollapsed == null) {
                collapse(1)
                val animation = AlphaAnimation(0f, 1f)
                animation.duration = Constant.ANIMATION_DURATION
                animation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationRepeat(animation: Animation?) {
                        // actually, I don't need this method but I have to implement this.
                    }

                    override fun onAnimationEnd(animation: Animation?) {
                        visibility = View.VISIBLE
                    }

                    override fun onAnimationStart(animation: Animation?) {
                        // actually, I don't need this method but I have to implement this.
                    }
                })
                startAnimation(animation)
            }
        }
        expandedFilter.margin = margin
        collapsedFilter.margin = margin
    }

    private fun validate(): Boolean = adapter != null && adapter?.items != null && !adapter?.items?.isEmpty()!!

    override fun collapse() {
        collapse(Constant.ANIMATION_DURATION)
    }

    fun isCollapsed(): Boolean {
        return isCollapsed!!
    }

    private fun collapse(duration: Long) {
        if (mIsBusy || collapsedFilter.isBusy) return
        mIsBusy = true
        mRemovedFilters.clear()

        isCollapsed = true

        //removeItemsFromParent()
        container.bringToFront()
        container.requestFocus()

        ValueAnimator.ofFloat(0f, duration.toFloat()).setDuration(duration).apply {
            addUpdateListener {
                val ratio = it.animatedValue as Float / duration

                collapseView.rotateArrow(180 * (1 - ratio))
                collapseView.turnIntoArrow(ratio)

                /*mSelectedFilters.keys.forEachIndexed { index, filterItem ->
                    val x = calculateX(index, collapsedFilterScroll.measuredWidth, margin, filterItem.collapsedSize)

                    filterItem.decrease(ratio)

                    if (index >= calculateCount(collapsedFilterScroll.measuredWidth, filterItem.collapsedSize, margin)) {
                        filterItem.alpha = 1 - ratio * 3
                    } else {
                        filterItem.translationX = filterItem.startX + (x - filterItem.startX
                                - filterItem.measuredWidth / 2 + filterItem.collapsedSize / 2) * ratio
                        filterItem.translationY = filterItem.startY + (dpToPx(getDimen(R.dimen.margin)).toFloat() / 4
                                - filterItem.startY) * ratio
                    }

                    if (ratio == 1f) {
                        filterItem.removeFromParent()
                        collapsedFilter.addView(filterItem)
                        filterItem.translationX = (x - filterItem.measuredWidth / 2 + filterItem.collapsedSize / 2).toFloat()
                        filterItem.translationY = dpToPx(getDimen(R.dimen.margin)).toFloat() / 4
                        filterItem.alpha = 1f
                        filterItem.bringToFront()
                    }
                }*/

                collapsedContainer.translationY = ratio * (-measuredHeight + collapsedContainer.height)
                dividerTop.alpha = 1 - 2 * ratio
                expandedFilterScroll.translationY = ratio * (-measuredHeight + collapsedContainer.height)

                /*if (mSelectedFilters.isEmpty()) {
                    collapsedText.visibility = View.VISIBLE
                    collapsedText.alpha = ratio
                } else {
                    collapsedText.visibility = View.GONE
                    collapsedText.alpha = 1 - ratio
                }*/

                collapsedText.visibility = View.VISIBLE
                collapsedText.alpha = ratio

                if (ratio == 1f) {
                    collapsedContainer.bringToFront()
                    collapsedContainer.requestFocus()
                    mIsBusy = false
                }
            }
        }.start()

        notifyListener()
    }

    override fun expand() {
        if (collapsedFilter.isBusy || mIsBusy) return

        mIsBusy = true

        isCollapsed = false

        //removeItemsFromParent()
        container.bringToFront()
        container.requestFocus()

        /*if (firstExpand) {
            firstExpand = false
            firstExpandList.forEach { view ->
                expandedFilter.addView(view)
            }
            firstExpandList.clear()
        }*/

        ValueAnimator.ofFloat(0f, Constant.ANIMATION_DURATION.toFloat()).setDuration(Constant.ANIMATION_DURATION).apply {
            addUpdateListener {
                val ratio = it.animatedValue as Float / Constant.ANIMATION_DURATION

                collapseView.rotateArrow(180 * ratio)
                collapseView.turnIntoOkButton(ratio)

                /*mSelectedFilters.keys.forEachIndexed { index, filterItem ->

                    val x = mSelectedFilters[filterItem]?.x
                    val y = mSelectedFilters[filterItem]?.y

                    if (index < calculateCount(collapsedFilterScroll.measuredWidth, filterItem.collapsedSize, margin)) {
                        filterItem.translationX = filterItem.startX + (x!! - filterItem.startX) * ratio
                        filterItem.translationY = filterItem.startY + (y!! - filterItem.startY) * ratio
                    } else {
                        filterItem.translationX = x!!.toFloat()
                        filterItem.translationY = y!!.toFloat()
                        filterItem.alpha = ratio
                    }
                    filterItem.increase(ratio)

                    if (ratio == 1f) {
                        filterItem.removeFromParent()
                        if(!filterItem.isHidden)
                            expandedFilter.addView(filterItem)
                        filterItem.translationX = 0f
                        filterItem.translationY = 0f
                    }
                }

                mRemovedFilters.keys.forEach { filterItem ->
                    filterItem.alpha = ratio

                    filterItem.removeFromParent()
                    if(!filterItem.isHidden)
                        expandedFilter.addView(filterItem)
                    filterItem.translationX = mRemovedFilters[filterItem]?.x!! * (1 - ratio)
                    filterItem.translationY = mRemovedFilters[filterItem]?.y!! * (1 - ratio)
                }*/
                collapsedText.alpha = 1 - ratio
                dividerTop.alpha = 2 * ratio
                collapsedContainer.translationY = -container.height.toFloat() * (1 - ratio)
                expandedFilterScroll.translationY = -container.height.toFloat() * (1 - ratio)

                if (ratio == 1f) {
                    expandedFilterScroll.bringToFront()
                    expandedFilterScroll.requestFocus()
                    collapsedText.visibility = View.GONE
                    mIsBusy = false
                }
            }
        }.start()

        mRemovedFilters.keys.forEach { filterItem ->
            val x = mRemovedFilters[filterItem]?.x
            val y = mRemovedFilters[filterItem]?.y

            filterItem.translationX = x!!.toFloat()
            filterItem.translationY = y!!.toFloat()
            filterItem.increase(1f)
            filterItem.deselect()
        }
    }

    private fun removeItemsFromParent() {
        mSelectedFilters.keys.forEach { item ->
            remove(item)
        }
    }

    /**
     * remove the view after it has been removed from {@link #mSelectedFilters}
     * @param item - to be removed from collapsedView
     */
    private fun remove(item: FilterItem) {
        val x = item.x
        val y = item.y
        item.removeFromParent()
        container.addView(item)
        item.translationX = x
        item.translationY = y
        item.startX = x
        item.startY = y
        item.bringToFront()
    }

    override fun onItemSelected(item: FilterItem) {
        val filter = mItems[item]!!
        if (mItems.contains(item)) {
            mSelectedItems.add(filter)

            //if item representing a whole group, re-create list of views for ExpandedFilterView
            //and refresh its measures
            if(item.isContainer && !item.isHeader) {
                val expandedChildren: MutableList<View> = mutableListOf()
                for (index in 0..(expandedFilter.childCount - 1)) {
                    if(expandedFilter.getChildAt(index) != null)
                        expandedChildren.add(expandedFilter.getChildAt(index))

                }
                val parentIndex = expandedChildren.indexOf(item)
                item.subFilters.forEachIndexed { i, subView ->
                    if (expandedFilter.indexOfChild(subView) >= 0)
                        expandedFilter.removeView(subView)

                    subView.isHidden = false
                    //add a subView right after its parent
                    expandedChildren.add(parentIndex + i + 1, subView)
                }
                expandedFilter.addAllViews(expandedChildren)
                expandedFilter.refreshView()
            }
        }

        mSelectedFilters.put(item, Coord(item.x.toInt(), item.y.toInt()))
        if(!item.isContainer || item.isHeader) {
            listener?.onFilterSelected(filter)
        }
    }

    /**
     * item deselected in ExpandedFilterView
     */
    override fun onItemDeselected(item: FilterItem) {
        val filter = mItems[item]!!
        if (mItems.contains(item)) {
            mSelectedItems.remove(filter)
            if(item.isContainer) {
                item.subFilters.forEach{ subView ->
                    expandedFilter.removeView(subView)
                    subView.isHidden = true
                }
                expandedFilter.refreshView()
            }
        }

        mSelectedFilters.remove(item)
        if(!item.isContainer) {
            listener?.onFilterDeselected(filter)
        }
    }

    /**
     * cancelButton onClick in CollapsedFilterView
     */
    override fun onItemRemoved(item: FilterItem) {
        val coord = mSelectedFilters[item]
        if (coord != null && collapsedFilter.removeItem(item)) {
            mSelectedFilters.remove(item)
            mSelectedItems.remove(mItems[item])
            mRemovedFilters.put(item, coord)

            postDelayed({
                remove(item)

                if (mSelectedFilters.isEmpty()) {
                    collapsedText.visibility = View.VISIBLE
                    collapsedText.alpha = 1f
                }
            }, Constant.ANIMATION_DURATION / 2)

            notifyListener()
        }
    }

    /**
     * called to update client's filter list, after the user collapsed the ExpandedFilterView
     * or removed an item from the CollapsedFilterView
     */
    private fun notifyListener() {
        if (mSelectedFilters.isEmpty()) {
            listener?.onNothingSelected()
        } else {
            listener?.onFiltersSelected(getSelectedItems())
        }
    }

    private fun getSelectedItems(): ArrayList<T> {
        val items: ArrayList<T> = ArrayList()
        mSelectedFilters.keys.forEach { filter ->
            val item: T? = mItems[filter]

            if (item != null && !filter.isContainer) {
                items.add(item)
            }
        }

        return items
    }

    fun deselectAll() {
        if(mSelectedFilters.isNotEmpty()) {
            mSelectedFilters.keys.forEach { item -> item.deselect(false) }
            mSelectedFilters.keys.forEach { item ->
                if (item.isContainer) {
                    item.subFilters.forEach { subView ->
                        expandedFilter.removeView(subView)
                        subView.isHidden = true
                    }
                }
            }
            expandedFilter.refreshView()
            mSelectedFilters.clear()
            mSelectedItems.clear()
        }
        listener?.onNothingSelected()
    }

    /**
     * Hook allowing a view to generate a representation of its internal state
     * that can later be used to create a new instance with that same state.
     * This state should only contain information that is not persistent or can
     * not be reconstructed later. For example, you will never store your
     * current position on screen because that will be computed again when a
     * new instance of the view is placed in its view hierarchy.
     * <p>
     * Some examples of things you may store here: the current cursor position
     * in a text view (but usually not the text itself since that is stored in a
     * content provider or other persistent storage), the currently selected
     * item in a list view.
     *
     * @return Returns a Parcelable object containing the view's current dynamic
     *         state, or null if there is nothing interesting to save.
     * @see #onRestoreInstanceState(Parcelable)
     * @see #saveHierarchyState(SparseArray)
     * @see #dispatchSaveInstanceState(SparseArray)
     * @see #setSaveEnabled(boolean)
     */
    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        return Bundle().apply {
            putParcelable(STATE_SUPER, superState)
            var collapsed: Boolean? = isCollapsed
            if(collapsed == null)
                collapsed = false
            putBoolean(STATE_COLLAPSED, collapsed)
            //putBoolean(STATE_COLLAPSED, isCollapsed!!)
            val selected = mSelectedItems
            val removed = mRemovedItems
            if (selected is Serializable) {
                putSerializable(STATE_SELECTED, selected)
            }
            if (removed is Serializable) {
                putSerializable(STATE_REMOVED, removed)
            }
        }
    }

    /**
     * Hook allowing a view to re-apply a representation of its internal state that had previously
     * been generated by {@link #onSaveInstanceState}. This function will never be called with a
     * null state.
     *
     * @param state The frozen state that had previously been returned by
     *        {@link #onSaveInstanceState}.
     *
     * @see #onSaveInstanceState()
     * @see #restoreHierarchyState(android.util.SparseArray)
     * @see #dispatchRestoreInstanceState(android.util.SparseArray)
     */
    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            super.onRestoreInstanceState(state.getParcelable(STATE_SUPER))
            val selected = state.getSerializable(STATE_SELECTED) as? List<T>
            val removed = state.getSerializable(STATE_REMOVED) as? List<T>
            isCollapsed = state.getBoolean(STATE_COLLAPSED)
            if (selected is ArrayList<T> && removed is ArrayList<T>) {
                mSelectedItems = selected
                mRemovedItems = removed
                expandedFilter.post {
                    restore(expandedFilter.filters)
                }
            }
        }
    }

    private fun restore(filters: LinkedHashMap<FilterItem, Coord>) {
        mSelectedFilters.clear()
        expandedFilter.post {
            filters.keys.forEach { filterItem ->
                filters[filterItem]?.let { coord ->
                    val item = { item: T -> filterItem.text == item.getText() }

                    if (mSelectedItems.any(item)) {
                        mSelectedFilters.put(filterItem, coord)
                        filterItem.select(false)
                    } else if (mRemovedItems.any(item)) {
                        mRemovedFilters.put(filterItem, coord)
                        filterItem.deselect(false)
                    }
                }
            }

            if (isCollapsed == null || isCollapsed as Boolean) {
                collapse(1)
            } else {
                expand()
            }
        }
    }

    /**
     * change the filterView state from expanded to collapsed and vice versa
     */
    override fun toggle() {
        if (collapsedFilter.isBusy || mIsBusy) return

        if (isCollapsed != null && isCollapsed as Boolean) expand() else collapse()
    }

}