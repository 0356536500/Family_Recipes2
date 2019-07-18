package com.ronginat.searchfilter.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import com.google.android.flexbox.*
import com.ronginat.searchfilter.R
import com.ronginat.searchfilter.listener.CollapseListener


class ExpandedFilterView : FlexboxLayout {

    //private var mPrevItem: View? = null
    private var mStartX = 0f
    private var mStartY = 0f
    private var myLayoutParams: FlexboxLayout.LayoutParams =
            FlexboxLayout.LayoutParams(FlexboxLayout.LayoutParams.MATCH_PARENT, FlexboxLayout.LayoutParams.WRAP_CONTENT)

    internal var listener: CollapseListener? = null
    internal var margin: Int = dpToPx(getDimen(R.dimen.margin))
    //internal val filters: LinkedHashMap<FilterItem, Coord> = LinkedHashMap()

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleRes: Int) : super(context, attrs, defStyleRes) {
        val verticalPad = dpToPx(getDimen(R.dimen.expanded_vertical_padding))
        val horizontalPad = dpToPx(getDimen(R.dimen.expanded_horizontal_padding))
        setPadding(horizontalPad, verticalPad, horizontalPad, verticalPad)
    }

    init {
        myLayoutParams.setMargins(16,16,16,16)
        flexWrap = FlexWrap.WRAP
        flexDirection = FlexDirection.ROW
        alignItems = AlignItems.FLEX_START
        alignContent = AlignContent.FLEX_START
        justifyContent = JustifyContent.FLEX_START
    }

    @SuppressLint("ClickableViewAccessibility")
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