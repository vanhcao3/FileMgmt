package com.example.filemgmt

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var fileAdapter: FileAdapter
    private var currentPath: File = Environment.getExternalStorageDirectory()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        checkPermissions()
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11 trở lên
            if (!Environment.isExternalStorageManager()) {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.addCategory("android.intent.category.DEFAULT")
                    intent.data = Uri.parse("package:${applicationContext.packageName}")
                    startActivityForResult(intent, MANAGE_STORAGE_PERMISSION_CODE)
                } catch (e: Exception) {
                    val intent = Intent()
                    intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                    startActivityForResult(intent, MANAGE_STORAGE_PERMISSION_CODE)
                }
            } else {
                loadFiles(currentPath)
            }
        } else {
            // Android 10 trở xuống
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ),
                    STORAGE_PERMISSION_CODE
                )
            } else {
                loadFiles(currentPath)
            }
        }
    }

    private fun loadFiles(directory: File) {
        val files = directory.listFiles()?.sortedWith(compareBy { !it.isDirectory })
        fileAdapter = FileAdapter(files?.toList() ?: emptyList()) { file ->
            when {
                file.isDirectory -> {
                    currentPath = file
                    loadFiles(file)
                }
                file.extension.lowercase() in textExtensions -> {
                    startActivity(Intent(this, FileViewerActivity::class.java).apply {
                        putExtra("filePath", file.absolutePath)
                    })
                }
                else -> {
                    Toast.makeText(this, "Không thể mở file này", Toast.LENGTH_SHORT).show()
                }
            }
        }
        recyclerView.adapter = fileAdapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MANAGE_STORAGE_PERMISSION_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    loadFiles(currentPath)
                } else {
                    Toast.makeText(
                        this,
                        "Cần cấp quyền để truy cập files",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadFiles(currentPath)
            } else {
                Toast.makeText(
                    this,
                    "Cần cấp quyền để truy cập files",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onBackPressed() {
        if (currentPath.absolutePath != Environment.getExternalStorageDirectory().absolutePath) {
            currentPath = currentPath.parentFile!!
            loadFiles(currentPath)
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        private const val STORAGE_PERMISSION_CODE = 100
        private const val MANAGE_STORAGE_PERMISSION_CODE = 101
        private val textExtensions = listOf("txt", "json", "xml", "html", "css", "js")
    }
}