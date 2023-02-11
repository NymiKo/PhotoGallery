package com.easyprog.android.photogallery.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.easyprog.android.photogallery.R
import com.easyprog.android.photogallery.fragment.PhotoGalleryFragment

class PhotoGalleryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_gallery)

        val isFragmentContainerEmpty = savedInstanceState == null
        if (isFragmentContainerEmpty) {
            supportFragmentManager.beginTransaction()
                .add(R.id.fragmentContainer, PhotoGalleryFragment.newInstance())
                .commit()
        }
    }
}