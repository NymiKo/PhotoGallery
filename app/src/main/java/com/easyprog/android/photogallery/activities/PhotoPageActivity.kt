package com.easyprog.android.photogallery.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.webkit.WebView
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import com.easyprog.android.photogallery.R
import com.easyprog.android.photogallery.fragments.PhotoPageFragment

class PhotoPageActivity: AppCompatActivity() {

    private lateinit var webView: WebView

    companion object {
        fun newIntent(context: Context, photoPageUri: Uri): Intent {
            return Intent(context, PhotoPageActivity::class.java).apply {
                data = photoPageUri
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_page)

        val fm = supportFragmentManager
        val currentFragment = fm.findFragmentById(R.id.fragment_container)

        if (currentFragment == null) {
            val fragment = PhotoPageFragment.newInstance(intent.data!!)
            fm.beginTransaction().add(R.id.fragment_container, fragment).commit()
        }
    }
}