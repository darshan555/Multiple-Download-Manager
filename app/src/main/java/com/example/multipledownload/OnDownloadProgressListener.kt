package com.example.multipledownload

interface OnDownloadProgressListener {
    fun percent(percent: Int)
    fun downloadStart()
    fun downloadedSuccess()
    fun downloadFail(error: String?)
}