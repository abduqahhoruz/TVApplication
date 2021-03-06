package com.example.mydownloadmanager.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes

fun <T> lazyFast(
    mode: LazyThreadSafetyMode = LazyThreadSafetyMode.NONE,
    initializer: () -> T
): Lazy<T> = lazy(mode, initializer)

fun ViewGroup.inflater(@LayoutRes layoutId: Int): View =
    LayoutInflater.from(context).inflate(layoutId, this, false)
