package com.example.mydownloadmanager.ui.home

import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mydownloadmanager.R
import com.example.mydownloadmanager.common.Constants
import com.example.mydownloadmanager.common.inflater
import com.example.mydownloadmanager.data.DownloadStatus
import com.example.mydownloadmanager.data.MP4Payloads
import com.example.mydownloadmanager.data.Pojo
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_row_mp4.view.*
import java.math.BigDecimal

class MP4Adapter(
    private val mDatalist: MutableList<Pojo>,
    private val onClickListener: IOnItemClickListener,
    fileNames: ArrayList<String>?
) : RecyclerView.Adapter<MP4Adapter.MP4VH>() {
    private val fileNames = fileNames

    inner class MP4VH(
        override val containerView: View,
        private val onClickListener: IOnItemClickListener
    ) : RecyclerView.ViewHolder(containerView),
        LayoutContainer, View.OnClickListener {
        private var statusText: String = "status"

        fun onBind(pojo: Pojo) {
            if (checkWhetherDownloaded(pojo)) {
                containerView.iv_download.visibility = View.INVISIBLE
            }

            containerView.tv_title.text = pojo.title
            containerView.tv_description.text = pojo.description
            Glide.with(containerView.context)
                .load("${Constants.BASE_URL}${pojo.thumb}")
                .placeholder(R.drawable.ic_video_placeholder)
                .into(containerView.iv_mp4)

            containerView.iv_download.setOnClickListener(this)
            containerView.btn_pause.setOnClickListener(this)
            containerView.btn_cancel.setOnClickListener(this)
            onFileStatus(pojo)
        }

        private fun checkWhetherDownloaded(pojo: Pojo): Boolean {
            var boolean = false
            if (fileNames != null) {
                for (i in fileNames) {
                    if (i == pojo.fileName) {
                        boolean = true
                    }
                }

            }
            return boolean
        }

        fun onFileDownloading(pojo: Pojo) {
            if (pojo.totalBytes != 0) {
                val progressPercent = pojo.soFarBytes.toFloat() * 100 / pojo.totalBytes.toFloat()
                if (progressPercent > 0) {
                    containerView.pb_downloading.isIndeterminate = false
                    containerView.pb_downloading.progress = progressPercent.toInt()
                }
                var soFarBytes = pojo.soFarBytes.toFloat()
                var totalBytes = pojo.totalBytes.toFloat()
                var sofarText = "B"
                var totalText = "B"
                if (soFarBytes > 1_024 * 1_024) {
                    soFarBytes /= (1_024 * 1_024)
                    sofarText = "MB"
                } else if (soFarBytes > 1_024) {
                    soFarBytes /= 1_024
                    sofarText = "KB"
                }

                if (totalBytes > 1_024 * 1_024) {
                    totalBytes /= (1_024 * 1_024)
                    totalText = "MB"
                } else if (totalBytes > 1_024) {
                    totalBytes /= 1_024
                    totalText = "KB"
                }

                soFarBytes = round(soFarBytes, 1)
                totalBytes = round(totalBytes, 1)
                containerView.tv_progress.text =
                    "$statusText: ${progressPercent.toInt()}%  ${soFarBytes} $sofarText/$totalBytes $totalText"
            }
        }

        fun onFileStatus(pojo: Pojo) {
            Log.d("status", "onFileStatus: ${pojo.title} ${pojo.status}")
            when (pojo.status) {
                DownloadStatus.EMPTY -> {
                    containerView.ll_download.isVisible = false
                    containerView.iv_download.isVisible = true
                    containerView.iv_download.isClickable = true
                    statusText = ""
                }
                DownloadStatus.CONNECTED -> {
                    mDatalist[adapterPosition].paused = false
                    containerView.ll_download.isVisible = true
                    containerView.iv_download.isVisible = true
                    containerView.iv_download.isClickable = false
                    containerView.btn_pause.isInvisible = false
                    statusText = "Downloading"
                }
                DownloadStatus.SUCCESS -> {
                    containerView.ll_download.isVisible = false
                    containerView.iv_download.isVisible = false
                    containerView.iv_download.isClickable = false
                    containerView.btn_pause.isInvisible = true
                    containerView.llc_item_row.setOnClickListener(this)
                    statusText = "Download completed"
                }
                DownloadStatus.ERROR -> {
                    containerView.ll_download.isVisible = true
                    containerView.iv_download.isVisible = true
                    containerView.iv_download.isClickable = true
                    statusText = "Error downloading"
                }
                DownloadStatus.PAUSED -> {
                    Log.d("PAUSED", "onFileStatus: ")
                    mDatalist[adapterPosition].paused = true
                    containerView.ll_download.isVisible = mDatalist[adapterPosition].soFarBytes > 0
                    containerView.iv_download.isVisible = true
                    containerView.iv_download.isClickable = true
                    statusText = "Downloading paused"
                }
            }
            onFileDownloading(pojo)

        }

        private fun round(d: Float, decimalPlace: Int): Float {
            var bd = BigDecimal(d.toString())
            bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP)
            return bd.toFloat()
        }

        override fun onClick(v: View) {
            when (v.id) {
                R.id.iv_download -> {
                    val pojo = mDatalist[adapterPosition]
                    val fileName = mDatalist[adapterPosition].title.replace(" ", "")
                    containerView.ll_download.isVisible = true
                    containerView.btn_pause.setImageResource(R.drawable.ic_pause_circle)
                    val id =
                        onClickListener.onClickDownload(adapterPosition, fileName, pojo.sources[0])
                    pojo.fileName = fileName
                    pojo.id = id
                }
                R.id.btn_pause -> {
                    val pojo = mDatalist[adapterPosition]
                    if (pojo.paused) {
                        //pause -> downloading
                        pojo.paused = false
                        containerView.btn_pause.setImageResource(R.drawable.ic_pause_circle)
                        onClickListener.onClickPlay(adapterPosition, pojo.fileName, pojo.sources[0])
                    } else {
                        //downloading -> pause
                        pojo.paused = true
                        containerView.btn_pause.setImageResource(R.drawable.ic_play_circle)
                        onClickListener.onClickPause(pojo.id)
                    }
                }
                R.id.btn_cancel -> {
//                    mDatalist[adapterPosition].paused = false
                    val pojo = mDatalist[adapterPosition]
                    pojo.soFarBytes = 0
                    onClickListener.onClickCancel(pojo.id, pojo.fileName)
                    containerView.pb_downloading.isIndeterminate = true
                    containerView.iv_download.isVisible = true
                    containerView.iv_download.isClickable = true
                    containerView.ll_download.isVisible = false
                }
                R.id.llc_item_row -> {
                    val pojo = mDatalist[adapterPosition]
                    onClickListener.onClickInfo(
                        adapterPosition,
                        pojo.fileName,
                        pojo.title,
                        pojo.description
                    )
                }
            }
        }
    }

    fun setStatus(position: Int, status: DownloadStatus, payloads: MP4Payloads) {
        mDatalist[position].status = status
        notifyItemChanged(position, payloads)
    }

    fun setStatus(position: Int, soFarBytes: Int, totalBytes: Int, payloads: MP4Payloads) {
        mDatalist[position].soFarBytes = soFarBytes
        mDatalist[position].totalBytes = totalBytes
        notifyItemChanged(position, payloads)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MP4VH {
        val view = parent.inflater(R.layout.item_row_mp4)
        return MP4VH(view, onClickListener)
    }

    override fun onBindViewHolder(holder: MP4VH, position: Int) {
        holder.onBind(mDatalist[position])
    }

    override fun onBindViewHolder(
        holder: MP4VH,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isNotEmpty()) {
            when (payloads[0]) {
                MP4Payloads.FILEDOWNLOADING -> {
                    holder.onFileDownloading(mDatalist[position])
                }
                MP4Payloads.FILESTATUS -> {
                    holder.onFileStatus(mDatalist[position])
                }
            }
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun getItemCount() = mDatalist.size


}

interface IOnItemClickListener {
    fun onClickInfo(position: Int, fileName: String, title: String, description: String)
    fun onClickDownload(position: Int, fileName: String, url: String): Int
    fun onClickPlay(position: Int, fileName: String, url: String)
    fun onClickPause(id: Int)
    fun onClickCancel(id: Int, fileName: String)
}

