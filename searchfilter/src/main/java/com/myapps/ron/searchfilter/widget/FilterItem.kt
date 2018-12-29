package com.myapps.ron.searchfilter.widget

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.myapps.ron.searchfilter.R
import com.myapps.ron.searchfilter.listener.FilterItemListener
import kotlinx.android.synthetic.main.item_filter.view.*
import java.io.Serializable


class FilterItem : FrameLayout, Serializable {

    var isContained: Boolean = false
    var isContainer: Boolean = false
    var isHeader: Boolean = false
    var item: Any? = null
    var subFilters: MutableList<FilterItem> = mutableListOf()
    val subItems: MutableList<Any> = mutableListOf()
    private var isFilterSelected: Boolean = false
    var startX: Float = 0f
    var startY: Float = 0f

    var strokeWidth: Int = 5
    @ColorInt var color: Int? = null
    @ColorInt var checkedColor: Int? = null
    @ColorInt var strokeColor: Int? = null
    @ColorInt var checkedTextColor: Int? = null
    @ColorInt var textColor: Int? = null
    var typeface: Typeface? = null
        set(value) {
            textView.typeface = value
        }
    var text: String
        get() = textView.text.toString()
        set(value) {
            mText = value
            textView.text = value
        }

    var cornerRadius: Float = 100f
        set(value) {
            field = value
            updateBackground()
        }
    internal var listener: FilterItemListener? = null

    private var mText: String? = null
    //private var mStrokeWidth: Int = dpToPx(1.25f)

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleRes: Int) : super(context, attrs, defStyleRes)

    init {
        LayoutInflater.from(context).inflate(R.layout.item_filter, this, true)

        textBackground.setOnClickListener { textView.performClick() }
        textView.setOnClickListener {
            if (isFilterSelected) {
                deselect()
            } else {
                select()
            }
        }
    }

    fun hideSubFilters() {
        subFilters.forEach { filter ->
            filter.hide()
        }
    }

    private fun hide() {
        visibility = View.GONE
        if (subFilters.isNotEmpty())
            hideSubFilters()
    }

    fun deselectAll() {
        subFilters.forEach { filter ->
            filter.deselectAll()
        }
        if (isFilterSelected)
            deselect(false)
    }

    fun select(notify: Boolean = true) {
        isFilterSelected = true
        updateView()

        if (notify) {
            listener?.onItemSelected(this)
        }
    }

    fun select() = select(true)

    fun deselect() = deselect(true)

    fun deselect(notify: Boolean = true) {
        isFilterSelected = false
        updateView()

        if (notify) {
            listener?.onItemDeselected(this)
        }
    }

    private fun updateView() {
        updateTextColor()
        updateBackground()
    }

    private fun updateTextColor() {
        @ColorInt val color: Int? = if (isFilterSelected) checkedTextColor else textColor

        if (color != null) {
            textView.setTextColor(color)
        }
    }

    private fun updateBackground() {
        @ColorInt val color: Int? = if (isFilterSelected) checkedColor else color
        //color = removeAlpha(color)
        //val strokeColor = if (isFilterSelected) color else removeAlpha(strokeColor)

        //ResourcesCompat.getDrawable(resources, R.drawable.item_shape, null)
        val drawable = GradientDrawable()
        drawable.shape = GradientDrawable.RECTANGLE
        drawable.cornerRadius = cornerRadius

        if (color != null) {
            drawable.setColor(color)
            //textBackground.setBackgroundColor(color)
        } else {
            drawable.setColor(getColor(android.R.color.white))
            //textBackground.setBackgroundColor(getColor(android.R.color.white))
        }

        if (!isFilterSelected) {
            var strokeColorCurrent = Color.BLACK
            if (strokeColor != null)
                strokeColorCurrent = strokeColor!!
            drawable.setStroke(strokeWidth, strokeColorCurrent)
        }

        textBackground.background = drawable

        /*if (strokeColor != null) {
            //drawable.setStroke(mStrokeWidth, strokeColor!!)
            topStroke.setBackgroundColor(strokeColor!!)
            bottomStroke.setBackgroundColor(strokeColor!!)
        }*/
    }

    private fun getColor(@ColorRes color: Int): Int {
        return ContextCompat.getColor(context, color)
    }

    //private fun removeAlpha(@ColorInt color: Int?): Int? = color?.or(0xff000000.toInt())

    fun removeFromParent() {
        if (parent != null) {
            (parent as ViewGroup).removeView(this)
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean = when (parent) {
        null -> false
        else -> super.onInterceptTouchEvent(ev)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FilterItem) return false

        if (mText != other.mText) return false

        return true
    }

    override fun hashCode(): Int {
        return mText?.hashCode() ?: 0
    }


}