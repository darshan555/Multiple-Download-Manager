package com.example.multipledownload

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.downloader.Error
import com.downloader.PRDownloader
import com.downloader.Status
import com.example.multipledownload.databinding.RecViewItemBinding

class DownloadAdapter(private val context: Context): RecyclerView.Adapter<DownloadAdapter.MyViewHolder>() {

    var tasks = mutableListOf<DownloadTask>()
    fun addDownloadTask(task: DownloadTask) {
        // Check for duplicate file names and update accordingly
        var updatedFileName = task.fileName
        var counter = 1
        while (tasks.any { it.fileName == updatedFileName }) {
            updatedFileName = "${task.fileNameWithoutExtension()} (${counter++}).${task.fileExtension()}"
        }
        task.fileName = updatedFileName
        tasks.add(task)
        notifyItemInserted(tasks.size - 1)
    }

    private fun DownloadTask.fileNameWithoutExtension(): String {
        return fileName.substringBeforeLast('.', fileName)
    }

    private fun DownloadTask.fileExtension(): String {
        return fileName.substringAfterLast('.', "")
    }

    inner class MyViewHolder(private val binding: RecViewItemBinding):ViewHolder(binding.root){
        private var downloadId: Int? = null
        init {
            binding.start.setOnClickListener { handleStartButtonClick() }
            binding.cancelBTN.setOnClickListener { handleCancelButtonClick() }
        }
        fun bind(task: DownloadTask){
            downloadId = task.downloadId
            binding.itemName.text = task.fileName

            updateStatus()
        }
        private fun updateStatus(){

            when(PRDownloader.getStatus(downloadId ?: 0)){
                Status.RUNNING -> {
                    // Download is running, show pause option
                    binding.start.text = "Pause"
                    setButtonEnabledState(true)
                }
                Status.PAUSED -> {
                    // Download is paused, show resume option
                    binding.start.text = "Resume"
                    setButtonEnabledState(true)
                }
                else -> {
                    // Download is not running, show start option
                    binding.start.text = "Start"
                    setButtonEnabledState(true)
                }
            }
        }
        private fun handleStartButtonClick() {
            when (PRDownloader.getStatus(downloadId ?: 0)) {
                Status.RUNNING -> {
                    // Download is running, pause it
                    PRDownloader.pause(downloadId ?: 0)
                }
                Status.PAUSED -> {
                    // Download is paused, resume it
                    PRDownloader.resume(downloadId ?: 0)
                }
                else -> {
                    // Download is not running, start it
                    startDownload()
                }
            }
        }

        private fun startDownload() {
            // Start the download and update UI
            val task = tasks[adapterPosition]

            downloadId = PRDownloader.download(task.url, task.path, task.fileName)
                .build()
                .setOnStartOrResumeListener {

                    binding.start.text = "Pause"
                    setButtonEnabledState(true)
                    Toast.makeText(context, "Downloading started", Toast.LENGTH_SHORT).show()
                }
                .setOnPauseListener {
                    binding.start.text = "Resume"
                    setButtonEnabledState(true)
                    Toast.makeText(context, "Downloading Paused", Toast.LENGTH_SHORT).show()
                }
                .setOnCancelListener {
                    downloadId = 0
                    binding.start.text = "Start"
                    setButtonEnabledState(true)
                    binding.progressBar.progress = 0
                    Toast.makeText(context, "Downloading Cancelled", Toast.LENGTH_SHORT).show()
                }
                .setOnProgressListener { progress ->
                    val progressPercent = (progress.currentBytes * 100 / progress.totalBytes).toInt()
                    binding.progressBar.progress = progressPercent
                    binding.progressBar.isIndeterminate = false
                }
                .start(object : com.downloader.OnDownloadListener {
                    override fun onDownloadComplete() {
                        setButtonEnabledState(false)
                        binding.start.text = "Completed"
                        Toast.makeText(context, "Downloading Completed", Toast.LENGTH_SHORT).show()
                    }

                    override fun onError(error: Error?) {
                        setButtonEnabledState(true)
                        binding.start.text = "Start"
                        binding.progressBar.progress = 0
                        downloadId = 0
                        Log.d("TAG11",error.toString())
                        Toast.makeText(context, "Error Occurred", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        private fun handleCancelButtonClick() {
            // Handle cancel button click
            PRDownloader.cancel(downloadId ?: 0)
        }

        private fun setButtonEnabledState(enabled: Boolean) {
            binding.start.isEnabled = enabled
            binding.cancelBTN.isEnabled = enabled
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = RecViewItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MyViewHolder(inflater)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val task = tasks[position]
        holder.bind(task)
    }

    override fun getItemCount(): Int {
        return tasks.size
    }
}