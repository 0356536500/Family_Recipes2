package com.myapps.ron.searchfilter.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.FrameLayout
import com.myapps.ron.searchfilter.Constant
import com.myapps.ron.searchfilter.R
import com.myapps.ron.searchfilter.adapter.FilterAdapter
import com.myapps.ron.searchfilter.listener.CollapseListener
import com.myapps.ron.searchfilter.listener.FilterItemListener
import com.myapps.ron.searchfilter.listener.FilterListener
import com.myapps.ron.searchfilter.model.FilterModel
import kotlinx.android.synthetic.main.collapsed_container.view.*
import kotlinx.android.synthetic.main.filter.view.*
import java.util.*


@Suppress("PrivatePropertyName")
class Filter<T : FilterModel> : FrameLayout, FilterItemListener, CollapseListener {

    var adapter: FilterAdapter<T>? = null
    var listener: FilterListener<T>? = null
    var margin = dpToPx(getDimen(R.dimen.margin))
    var customTextView: String = ""
        set(value) {
            collapsedText.text = value
            field = value
        }
    /*var textToReplaceArrow: String = ""
        set(value) {
            collapseView.setText(value)
        }

    var replaceArrowByText: Boolean = false
        set(value) {
            collapseView.setHasText(value)
        }*/

    var customTextViewColor: Int = 0x827f93
        set(value) {
            field = value
            collapsedText.setTextColor(value)
            collapseView.invalidate()
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
    private val STATE_SELECTED_ITEMS = "state_selected_items"
    private val STATE_COLLAPSED = "state_collapsed"

    private var mNothingSelectedItem: FilterItem? = null

    private var mItems: LinkedHashMap<FilterItem, T> = LinkedHashMap()
    private var mainFilters: ArrayList<FilterItem> = ArrayList()
    private var mSelectedFilters: ArrayList<FilterItem> = ArrayList()
    private var mSelectedItems: ArrayList<T> = ArrayList()
    //private var mRemovedItems: ArrayList<T> = ArrayList()

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleRes: Int) : super(context, attrs, defStyleRes) {
        LayoutInflater.from(context).inflate(R.layout.filter, this, true)
        visibility = View.INVISIBLE
        collapseView.setOnClickListener { toggle() }
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
        Log.e(javaClass.simpleName, "build")
        if (!validate()) {
            return
        }

        val firstBuild = mainFilters.size > 0

        mItems.clear()
        mainFilters.clear()
        mSelectedItems.clear()
        mSelectedFilters.clear()

        expandedFilter.post {
            expandedFilter.removeAllViewsInLayout()
            adapter?.items?.forEach { item ->
                mainFilters.add(buildItem(item, null, true))
            }

            if (isCollapsed == null || isCollapsed == false) {
                if (firstBuild)
                    collapse(1, false)
                else
                    collapse(Constant.ANIMATION_RESTORE_DURATION, false)
                animateFadeInAfterCollapse()
            }
        }
        expandedFilter.margin = margin
    }

    private fun animateFadeInAfterCollapse(duration : Long = Constant.ANIMATION_DURATION) {
        val animation = AlphaAnimation(0f, 1f)
        animation.duration = duration
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

    private fun buildItem(item: FilterModel, parent: FilterModel?, isVisible: Boolean) : FilterItem{
        //add the group header to the main View
        val filter: T = item as T
        var castedParent: T? = null
        if (parent != null)
            castedParent = parent as T
        val itemView: FilterItem = adapter?.createView(filter, castedParent)!!
        mItems[itemView] = filter
        if(itemView.text == customTextView) {
            itemView.select()
            mNothingSelectedItem = itemView
        }
        itemView.listener = this
        // view saves reference to its FilterModel
        itemView.item = filter
        // if the view is initially visible then it's must be a main container, therefore not contained
        // else, this view is not initially visible, therefore contained by other view
        itemView.isContained = isVisible

        expandedFilter.addView(itemView)

        if (isVisible)
            itemView.visibility = View.VISIBLE
        else
            itemView.visibility = View.GONE

        // init the sub filters if exists. recursive method, so it can be flexible structure of filters
        if (filter.getSubs().isNotEmpty()) {
            itemView.isContainer = true
            itemView.subItems.addAll(filter.getSubs())
            filter.getSubs().forEach { subItem ->
                itemView.subFilters.add(buildItem(subItem, item,false))
            }
        }

        return itemView
    }

    private fun validate(): Boolean = adapter != null && adapter?.items != null && !adapter?.items?.isEmpty()!!

    override fun collapse() {
        collapse(Constant.ANIMATION_DURATION, true)
    }

    private fun collapse(notify: Boolean) {
        collapse(Constant.ANIMATION_DURATION, notify)
    }

    fun isCollapsed(): Boolean {
        return isCollapsed == null || isCollapsed!!
    }

    private fun collapse(duration: Long, notify: Boolean) {
        if (mIsBusy) return
        mIsBusy = true
        //mRemovedFilters.clear()
        isCollapsed = true

        //removeItemsFromParent()
        container.bringToFront()
        container.requestFocus()

        ValueAnimator.ofFloat(0f, duration.toFloat()).setDuration(duration).apply {
            addUpdateListener {
                val ratio = it.animatedValue as Float / duration

                collapseView.rotateArrow(180 * (1 - ratio))
                collapseView.turnIntoArrow(ratio)

                collapsedContainer.translationY = ratio * (-measuredHeight + collapsedContainer.height)
                dividerTop.alpha = 1 - 2 * ratio
                expandedFilterScroll.translationY = ratio * (-measuredHeight + collapsedContainer.height)

                collapsedText.visibility = View.VISIBLE
                collapsedText.alpha = ratio

                if (ratio == 1f) {
                    collapsedContainer.bringToFront()
                    collapsedContainer.requestFocus()
                    mIsBusy = false
                }
            }
        }.start()

        if (notify)
            notifyListener()
    }

    override fun expand() {
        if (mIsBusy) return

        mIsBusy = true

        isCollapsed = false

        //removeItemsFromParent()
        container.bringToFront()
        container.requestFocus()


        ValueAnimator.ofFloat(0f, Constant.ANIMATION_DURATION.toFloat()).setDuration(Constant.ANIMATION_DURATION).apply {
            addUpdateListener {
                val ratio = it.animatedValue as Float / Constant.ANIMATION_DURATION

                collapseView.rotateArrow(180 * ratio)
                collapseView.turnIntoOkButton(ratio)

                collapsedText.alpha = 1 - ratio
                dividerTop.alpha = 2 * ratio
                collapsedContainer.translationY = -container.height.toFloat() * (1 - ratio)
                expandedFilterScroll.translationY = -container.height.toFloat() * (1 - ratio)

                if (ratio == 1f) {
                    expandedFilterScroll.bringToFront()
                    expandedFilterScroll.requestFocus()
                    collapsedText.visibility = View.GONE
                    if (mSelectedItems.isEmpty())
                        mNothingSelectedItem?.select(false)
                    mIsBusy = false
                }
            }
        }.start()
    }

    override fun onItemSelected(item: FilterItem) {
        val filter = mItems[item]!!
        if (mItems.contains(item)) {
            if (item.isDeselectHead) {
                // deselect all
                deselectAll()
                collapse(false)
                return
            }
            mNothingSelectedItem?.deselect(false)

            mSelectedItems.add(filter)
            mSelectedFilters.add(item)

            //if item representing a whole group, make this group visible in ExpandedFilter
            if (item.isContainer && !item.isDeselectHead) {
                item.subFilters.forEach { subFilter ->
                    subFilter.visibility = View.VISIBLE
                }
            }

            // if the item is a leaf in filters structure, call onFilterSelected
            // if its a header, call onFilterSelected
            if(!item.isContainer || item.isDeselectHead) {
                listener?.onFilterSelected(filter)
            }
        }
    }

    /**
     * item deselected in ExpandedFilterView
     */
    override fun onItemDeselected(item: FilterItem) {
        if (mItems.contains(item)) {
            if (item.isDeselectHead) {
                // deselect all
                deselectAll()
                item.select(false)
                collapse(false)
                return
            }

            val filter = mItems[item]!!

            mSelectedItems.remove(filter)
            mSelectedFilters.remove(item)

            // if the item contains sub filters or sub sub filter etc, hide them all
            if(item.isContainer) {
                item.hideSubFilters()
            }

            if(!item.isContainer) {
                listener?.onFilterDeselected(filter)
            }

            /*if (mSelectedItems.isEmpty()) {
                mNothingSelectedItem?.select(false)
            }*/
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
        mSelectedFilters.forEach { filter ->
            val item: T? = mItems[filter]
            if (item != null && !filter.isContainer && !filter.isDeselectHead) {
                items.add(item)
            }
        }
        /*mSelectedFilters.keys.forEach { filter ->
            val item: T? = mItems[filter]

            if (item != null && !filter.isContainer) {
                items.add(item)
            }
        }*/

        return items
    }

    fun deselectAll() {
        if (mSelectedItems.isNotEmpty()) {
            mainFilters.forEach { filter ->
                if (filter.isDeselectHead) {
                    filter.select(false)
                } else {
                    filter.deselectAll()
                    filter.hideSubFilters()
                }
            }
        }
        mSelectedItems.clear()
        mSelectedFilters.clear()
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
            /*var collapsed: Boolean? = isCollapsed
            if(collapsed == null)
                collapsed = true
            putBoolean(STATE_COLLAPSED, collapsed)*/
            putBoolean(STATE_COLLAPSED, isCollapsed!!)
            val selectedItems = mSelectedItems
            putSerializable(STATE_SELECTED_ITEMS, selectedItems)
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
        if (state != null) {
            if (state is Bundle) {
                super.onRestoreInstanceState(state.getParcelable(STATE_SUPER))
                val selectedItems = state.getSerializable(STATE_SELECTED_ITEMS) as? ArrayList<T>
                isCollapsed = state.getBoolean(STATE_COLLAPSED)
                if (selectedItems is ArrayList<T>) {
                    mSelectedItems = selectedItems
                    //mSelectedFilters = selectedFilters
                    expandedFilter.post {
                        visibility = View.VISIBLE
                        restore(mItems)
                    }
                }
            }
        }
    }

    private fun restore(filters: LinkedHashMap<FilterItem, T>) {
        mSelectedFilters.clear()
        expandedFilter.post {
            filters.keys.forEach { filterItem ->
                filters[filterItem]?.let {
                    val item = { item: T -> filterItem.text == item.getText() }

                    if (mSelectedItems.any(item)) {
                        mSelectedFilters.add(filterItem)
                        filterItem.select(false)
                    }
                }
            }

            if (isCollapsed == null || isCollapsed as Boolean) {
                collapse(Constant.ANIMATION_RESTORE_DURATION, false)
                animateFadeInAfterCollapse(Constant.ANIMATION_DURATION)
            } else {
                expand()
            }
        }
    }

    /**
     * change the filterView state from expanded to collapsed and vice versa
     */
    override fun toggle() {
        if (mIsBusy) return

        if (isCollapsed != null && isCollapsed as Boolean) expand() else collapse()
    }

}