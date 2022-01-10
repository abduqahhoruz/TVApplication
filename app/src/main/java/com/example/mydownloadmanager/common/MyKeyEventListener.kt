package com.example.mydownloadmanager.common

import android.view.KeyEvent

interface MyKeyEventListener {
    fun onKeyDown(keyCode: Int, event: KeyEvent?)
}