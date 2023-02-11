package com.easyprog.android.photogallery.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.easyprog.android.photogallery.R
import com.easyprog.android.photogallery.models.GalleryItem

class PhotoGalleryAdapter(private val galleryItems: List<GalleryItem>): RecyclerView.Adapter<PhotoGalleryAdapter.PhotoGalleryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoGalleryViewHolder {
        val textView = TextView(parent.context)
        return PhotoGalleryViewHolder(textView)
    }

    override fun onBindViewHolder(holder: PhotoGalleryViewHolder, position: Int) {
        val galleryItem = galleryItems[position]
        holder.bindTitle(galleryItem.title)
    }

    override fun getItemCount(): Int = galleryItems.size

    class PhotoGalleryViewHolder(itemTextView: TextView): RecyclerView.ViewHolder(itemTextView) {
        val bindTitle: (CharSequence) -> Unit = itemTextView::setText
    }
}