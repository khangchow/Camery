package com.chow.camery.extension

import android.view.View

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.showOrHideWithCondition(isNeedShown: Boolean) {
    if (isNeedShown) visible() else gone()
}

fun View.showOrInvisibleWithCondition(isNeedShown: Boolean) {
    if (isNeedShown) visible() else invisible()
}
