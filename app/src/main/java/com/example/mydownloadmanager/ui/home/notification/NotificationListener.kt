package com.example.mydownloadmanager.ui.home.notification

import android.util.Log
import com.example.mydownloadmanager.MainActivity
import com.example.mydownloadmanager.data.DownloadStatus
import com.example.mydownloadmanager.data.MP4Payloads
import com.example.mydownloadmanager.ui.home.MP4Adapter
import com.liulishuo.filedownloader.BaseDownloadTask
import com.liulishuo.filedownloader.notification.BaseNotificationItem
import com.liulishuo.filedownloader.notification.FileDownloadNotificationListener
import java.lang.ref.WeakReference

class NotificationListener(
    private val wActivity: WeakReference<MainActivity?>,
    private val channelId: String,
    private val mAdapter: MP4Adapter,
    private val position: Int
) :
    FileDownloadNotificationListener(wActivity.get()!!.notificationHelper) {

    override fun create(task: BaseDownloadTask?): BaseNotificationItem {
        return NotificationItem(task!!.id, "Task ${task.filename}", "aa", channelId)
    }

    override fun interceptCancel(
        task: BaseDownloadTask?,
        notificationItem: BaseNotificationItem?
    ): Boolean {
        return true
    }

    override fun connected(
        task: BaseDownloadTask?,
        etag: String?,
        isContinue: Boolean,
        soFarBytes: Int,
        totalBytes: Int
    ) {
        super.connected(task, etag, isContinue, soFarBytes, totalBytes)
        mAdapter.setStatus(position, DownloadStatus.CONNECTED, MP4Payloads.FILESTATUS)
    }

    override fun progress(
        task: BaseDownloadTask?,
        soFarBytes: Int,
        totalBytes: Int
    ) {
        super.progress(task, soFarBytes, totalBytes)
        mAdapter.setStatus(position, soFarBytes, totalBytes, MP4Payloads.FILEDOWNLOADING)
    }

    override fun completed(task: BaseDownloadTask?) {
        super.completed(task)
        mAdapter.setStatus(
            position,
            task?.largeFileSoFarBytes?.toInt() ?: 0,
            task?.largeFileTotalBytes?.toInt() ?: 0,
            MP4Payloads.FILESTATUS
        )
        mAdapter.setStatus(position, DownloadStatus.SUCCESS, MP4Payloads.FILESTATUS)
    }

    override fun paused(
        task: BaseDownloadTask?,
        soFarBytes: Int,
        totalBytes: Int
    ) {
        super.paused(task, soFarBytes, totalBytes)
        mAdapter.setStatus(position, DownloadStatus.PAUSED, MP4Payloads.FILESTATUS)
        Log.d("StatusTag", "paused: $task")
    }

    override fun error(task: BaseDownloadTask?, e: Throwable?) {
        super.error(task, e)
        mAdapter.setStatus(position, DownloadStatus.ERROR, MP4Payloads.FILESTATUS)
    }

}