package com.example.multipledownload

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.webkit.URLUtil
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.downloader.PRDownloader
import com.example.multipledownload.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter : DownloadAdapter

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                downloadStart()
            } else {
                downloadStart()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = DownloadAdapter(this)
        binding.recView.layoutManager = LinearLayoutManager(this)
        binding.recView.adapter = adapter

        binding.downloadBTN.setOnClickListener {
            checkPermissionsAndDownload()
        }
    }

    private fun checkPermissionsAndDownload() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                when {
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        downloadStart()
                    }
                    else -> {
                        requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }
                }
            }
            else -> downloadStart()
        }
    }

    private fun downloadStart() {
        val downloadLink = binding.urlEt.text.toString()
        val fileName = URLUtil.guessFileName(downloadLink, null, null)

        val downloadPath = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "Demo"
        ).absolutePath

        val downloadId = PRDownloader.download(downloadLink, downloadPath, fileName)
            .build()
            .downloadId

        val downloadTask = DownloadTask(
            downloadId = downloadId,
            url = downloadLink,
            path = downloadPath.toString(),
            fileName = fileName
        )
        adapter.addDownloadTask(downloadTask)

        binding.urlEt.text.clear()
    }

}