package com.example.filemgmt

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class FileViewerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_viewer)

        val filePath = intent.getStringExtra("filePath")
        if (filePath != null) {
            val file = File(filePath)
            findViewById<TextView>(R.id.textContent).text = file.readText()
        }
    }
} 