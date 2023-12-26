package com.example.multipledownload

data class DownloadTask(
    var downloadId: Int ? = null,
    val url: String,
    val path: String,
    var fileName: String,
    var counter: Int = 0
)
