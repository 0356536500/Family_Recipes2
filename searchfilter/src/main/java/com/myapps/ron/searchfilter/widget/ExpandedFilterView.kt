package com.myapps.ron.searchfilter.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.google.android.flexbox.*
import com.myapps.ron.searchfilter.R
import com.myapps.ron.searchfilter.listener.CollapseListener
import com.myapps.ron.searchfilter.model.Coord
import java.util.*


class ExpandedFilterView : FlexboxLayout {

    private var mPrevItem: View? = null
    private var mStartX = 0f
    private var mStartY = 0f
    private var myLayoutParams: FlexboxLayout.LayoutParams =
            FlexboxLayout.LayoutParams(FlexboxLayout.LayoutParams.MATCH_PARENT, FlexboxLayout.LayoutParams.WRAP_CONTENT)

    internal var listener: CollapseListener? = null
    internal var margin: Int = dpToPx(getDimen(R.dimen.margin))
    internal val filters: LinkedHashMap<FilterItem, Coord> = LinkedHashMap()

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleRes: Int) : super(context, attrs, defStyleRes)

    init {
        myLayoutParams.setMargins(16,16,16,16)
        flexWrap = FlexWrap.WRAP
        flexDirection = FlexDirection.ROW
        alignItems = AlignItems.FLEX_START
        alignContent = AlignContent.FLEX_START
        justifyContent = JustifyContent.FLEX_START
    }
    /*override fun onLayout(p0: Boolean, p1: Int, p2: Int, p3: Int, p4: Int) {
        if (!filters.isEmpty()) {
            for (i in 0..childCount - 1) {
                val child: View = getChildAt(i)
                val coord: Coord? = filters[child]

                if (coord != null) {
                    child.layout(coord.x, coord.y, coord.x + child.measuredWidth, coord.y + child.measuredHeight)
                }
            }
        }
    }*/

    fun refreshView() {
        /*filters.clear()
        mPrevItem = null
        measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)*/
    }

    fun addAllViews(views: MutableList<View>) {
        removeAllViews()
        views.forEach { view ->
            (view as FilterItem).removeFromParent()
            addView(view, myLayoutParams)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mStartX = event.x
                mStartY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                if (event.y - mStartY < -20) {
                    listener?.collapse()
                    mStartX = 0f
                    mStartY = 0f
                }
            }
        }

        return true
    }
}