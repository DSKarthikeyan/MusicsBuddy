package com.dsk.musicbuddy.util

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import com.dsk.musicbuddy.R

class ExpandShrinkHeader : LinearLayout {
    private var title: TextView? = null
    private var subTitle: TextView? = null
    private var totalDuration: TextView? = null

    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)


    override fun onFinishInflate() {
        super.onFinishInflate()
        title = findViewById(R.id.headerViewTitle)
        subTitle = findViewById(R.id.headerViewSubTitle)
        totalDuration = findViewById(R.id.headerViewSubTitleDuration)
    }

    @JvmOverloads
    fun bindTo(title: String?, subTitle: String? = "", textDuration: String? = "") {
        hideOrSetText(this.title!!, title)
        hideOrSetText(this.subTitle!!, subTitle)
        hideOrSetText(totalDuration!!, textDuration)
    }

    private fun hideOrSetText(tv: TextView, text: String?) {
        if (text == null || text == "") tv.visibility = GONE
        else tv.text = text
    }
}