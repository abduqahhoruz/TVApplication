package com.example.mydownloadmanager.ui.home

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.mydownloadmanager.MainActivity
import com.example.mydownloadmanager.R
import com.example.mydownloadmanager.common.Constants
import com.example.mydownloadmanager.common.MyKeyEventListener
import com.example.mydownloadmanager.common.lazyFast
import com.example.mydownloadmanager.data.DownloadStatus
import com.example.mydownloadmanager.data.MP4Payloads
import com.example.mydownloadmanager.data.Pojo
import com.example.mydownloadmanager.ui.home.notification.NotificationItem
import com.example.mydownloadmanager.ui.home.notification.NotificationUtils
import com.example.mydownloadmanager.util.NetworkStatus
import com.liulishuo.filedownloader.BaseDownloadTask
import com.liulishuo.filedownloader.FileDownloadQueueSet
import com.liulishuo.filedownloader.FileDownloader
import com.liulishuo.filedownloader.notification.BaseNotificationItem
import com.liulishuo.filedownloader.notification.FileDownloadNotificationListener
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment(R.layout.fragment_home), IOnItemClickListener, View.OnClickListener,
    MyKeyEventListener {


//    private var listener: NotificationListener? = null

    private val mViewModel by viewModels<HomeViewModel>()
    private lateinit var list: MutableList<Pojo>
    private val fileDownloader by lazy(LazyThreadSafetyMode.NONE) { FileDownloader.getImpl() }
    private val filterDM by lazyFast { requireActivity().cacheDir.absolutePath }
    private lateinit var mAdapter: MP4Adapter
    private val observerLoadPojoStatus = Observer<NetworkStatus> {
        when (it) {
            is NetworkStatus.LOADING -> {
                pb_loading.isVisible = true
                rv_pojo.isVisible = false
                llc_error.isVisible = false
            }
            is NetworkStatus.ERROR -> {
                pb_loading.isVisible = false
                rv_pojo.isVisible = false
                llc_error.isVisible = true
            }
            else -> {
                pb_loading.isVisible = false
                rv_pojo.isVisible = true
                llc_error.isVisible = false
            }
        }
    }
    private val observerList = Observer<MutableList<Pojo>> {
        list = it
        setupViews()
    }
    private val observerDeleteFileStatus = Observer<NetworkStatus> {
        when (it) {
            is NetworkStatus.LOADING -> {
            }
            is NetworkStatus.ERROR -> {
            }
            else -> {
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel.loadPojoList()
        NotificationUtils.createNotificationChannel(
            Constants.CHANNEL_ID,
            "MyFileDownloader",
            requireActivity().applicationContext
        )

    }

    private fun getFileNames(): ArrayList<String>? {
        val finalList: ArrayList<String>? = null
        val homeFolder = context?.getExternalFilesDir(filterDM)
        val fileList = homeFolder?.listFiles()
        if (fileList != null) {
            for (file in fileList) {
                Log.d("TAGTAG", "getFileNames:$file ")
                finalList?.add(file.name)
            }
        } else {
            Toast.makeText(requireActivity(), "$fileList, Ishlamavotti", Toast.LENGTH_SHORT).show()
        }

        finalList.let {
            return if (it !== null)
                it
            else
                null
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObserver()
        btn_retry.setOnClickListener(this)
    }

    private fun setupObserver() {
        mViewModel.liveLoadPojoStatus.observe(viewLifecycleOwner, observerLoadPojoStatus)
        mViewModel.livePojoList.observe(viewLifecycleOwner, observerList)
        mViewModel.liveDeleteFileStatus.observe(viewLifecycleOwner, observerDeleteFileStatus)
    }

    private fun setupViews() {
        Log.d("TAG", "setupViews: ${getFileNames()}")
        mAdapter = MP4Adapter(list, this, getFileNames())
        rv_pojo.adapter = mAdapter
    }

    override fun onClickDownload(position: Int, fileName: String, url: String): Int {
//        listener = NotificationListener(
//            WeakReference(requireActivity() as MainActivity), Constants.CHANNEL_ID,
//            mAdapter,
//            position
//        )
        val id: Int
        val queueSet = FileDownloadQueueSet(listener(position))
        val task = fileDownloader.create(url)
        task.path = "$filterDM/$fileName.mp4"
        id = task.id

        queueSet.downloadTogether(task)
            .addTaskFinishListener {
                (task.listener as FileDownloadNotificationListener).destroyNotification(task)
                mAdapter
            }
            .start()
//        task.start()

        return id
    }

    override fun onClickInfo(position: Int, fileName: String, title: String, description: String) {
        val args = bundleOf(
            Pair("key_title", title),
            Pair("key_fileName", fileName),
            Pair("key_description", description)
        )
        findNavController().navigate(R.id.navigation_home_info, args)
    }

    override fun onClickPlay(position: Int, fileName: String, url: String) {
        onClickDownload(position, fileName, url)
    }

    override fun onClickPause(id: Int) {
        fileDownloader.pause(id)
    }

    override fun onClickCancel(id: Int, fileName: String) {
        fileDownloader.pause(id)
        mViewModel.deleteFile(fileName)
    }

    private fun onRetry() {
        mViewModel.loadPojoList()
    }

    private fun listener(position: Int): FileDownloadNotificationListener {
        return object :
            FileDownloadNotificationListener((requireActivity() as MainActivity).notificationHelper) {

            override fun create(task: BaseDownloadTask?): BaseNotificationItem {
                return NotificationItem(
                    task!!.id,
                    "Task ${task.filename.split('.')[0]}",
                    "aa",
                    Constants.CHANNEL_ID
                )
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
            }

            override fun error(task: BaseDownloadTask?, e: Throwable?) {
                super.error(task, e)
                mAdapter.setStatus(position, DownloadStatus.ERROR, MP4Payloads.FILESTATUS)
            }

        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_retry -> {
                onRetry()
            }
            else -> {
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?) {
        Log.d("TAGTAG", "onKeyDown: $event")
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            Toast.makeText(context, "$keyCode", Toast.LENGTH_SHORT).show()
        }
    }
}