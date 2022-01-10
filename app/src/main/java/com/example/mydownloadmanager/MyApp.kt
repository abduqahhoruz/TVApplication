package com.example.mydownloadmanager

import android.app.Application
import android.content.Context
import com.liulishuo.filedownloader.FileDownloader

class MyApp :Application(){
    companion object{
        var CONTEXT: Context? = null
    }
    override fun onCreate() {
        // for demo.
       CONTEXT = this.applicationContext
        FileDownloader.setupOnApplicationOnCreate(this)
        super.onCreate()
    }
}