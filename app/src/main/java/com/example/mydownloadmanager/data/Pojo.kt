package com.example.mydownloadmanager.data

data class Pojo(
    val description: String,
    val sources: List<String>,
    val subtitle: String,
    val thumb: String,
    val title: String,

    var id: Int = -1,
    var fileName: String = "",
    var soFarBytes: Int = 0,
    var totalBytes: Int = 0,
    var status: DownloadStatus = DownloadStatus.EMPTY,
    var paused: Boolean = false

)

enum class DownloadStatus {
    PAUSED,
    SUCCESS,
    ERROR,
    EMPTY,
    CONNECTED
}

sealed class MP4Payloads {
    object FILESTATUS : MP4Payloads()
    object FILEDOWNLOADING : MP4Payloads()
}
