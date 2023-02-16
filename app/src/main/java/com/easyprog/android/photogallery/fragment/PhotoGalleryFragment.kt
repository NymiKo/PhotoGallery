package com.easyprog.android.photogallery.fragment

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.*
import com.easyprog.android.photogallery.R
import com.easyprog.android.photogallery.api.ThumbnailDownloader
import com.easyprog.android.photogallery.local_storage.QueryPreferences
import com.easyprog.android.photogallery.models.GalleryItem
import com.easyprog.android.photogallery.viewmodel.PhotoGalleryViewModel
import com.easyprog.android.photogallery.work_manager.PollWorker
import java.util.concurrent.TimeUnit

class PhotoGalleryFragment : Fragment() {

    private lateinit var photoRecyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar

    private val viewModel: PhotoGalleryViewModel by lazy { ViewModelProvider(this)[PhotoGalleryViewModel::class.java] }

    private lateinit var thumbnailDownloader: ThumbnailDownloader<PhotoGalleryAdapter.PhotoGalleryViewHolder>

    companion object {
        private const val POLL_WORK = "POLL_WORK"

        fun newInstance() = PhotoGalleryFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true

        val responseHandler = Handler(Looper.getMainLooper())
        thumbnailDownloader = ThumbnailDownloader(responseHandler) { photoHolder, bitmap, title ->
            val drawable = BitmapDrawable(resources, bitmap)
            photoHolder.bindImage(drawable, title)
        }
        thumbnailDownloader.fragmentLifecycle = lifecycle
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_photo_gallery, container, false)

        photoRecyclerView = view.findViewById(R.id.photo_recycler_view) as RecyclerView
        progressBar = view.findViewById(R.id.progress) as ProgressBar
        photoRecyclerView.layoutManager = GridLayoutManager(context, 3)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMenu()
        viewModel.galleryItemLiveData.observe(viewLifecycleOwner) { galleryItems ->
            progressBar.visibility = View.GONE
            photoRecyclerView.visibility = View.VISIBLE
            photoRecyclerView.adapter = PhotoGalleryAdapter(galleryItems)
        }
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.fragment_photo_gallery, menu)

                val searchItem: MenuItem = menu.findItem(R.id.menu_item_search)
                val searchView = searchItem.actionView as SearchView

                searchView.apply {
                    setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                        override fun onQueryTextSubmit(query: String): Boolean {
                            progressBar.visibility = View.VISIBLE
                            photoRecyclerView.visibility = View.GONE
                            viewModel.fetchPhotos(query)
                            clearFocus()
                            return true
                        }

                        override fun onQueryTextChange(newText: String?): Boolean {
                            return false
                        }
                    })

                    setOnSearchClickListener {
                        searchView.setQuery(viewModel.searchTerm, false)
                    }

                    setOnFocusChangeListener { _, hasFocus ->
                        if (!hasFocus) searchItem.collapseActionView()
                    }
                }

                val toggleItem = menu.findItem(R.id.menu_item_toggle_polling)
                val isPolling = QueryPreferences.isPolling(requireContext())
                val toggleItemTitle =
                    if (isPolling) R.string.stop_polling else R.string.start_polling
                toggleItem.setTitle(toggleItemTitle)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.menu_item_clear -> {
                        viewModel.fetchPhotos("")
                        true
                    }
                    R.id.menu_item_toggle_polling -> {
                        val isPolling = QueryPreferences.isPolling(requireContext())
                        if (isPolling) {
                            WorkManager.getInstance(requireContext()).cancelUniqueWork(POLL_WORK)
                            QueryPreferences.setPolling(requireContext(), false)
                        } else {
                            val constraints = Constraints.Builder()
                                .setRequiredNetworkType(NetworkType.UNMETERED)
                                .build()

                            val periodicRequest = PeriodicWorkRequest.Builder(
                                PollWorker::class.java,
                                15,
                                TimeUnit.MINUTES
                            )
                                .setConstraints(constraints)
                                .build()

                            WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(
                                POLL_WORK, ExistingPeriodicWorkPolicy.KEEP, periodicRequest
                            )

                            QueryPreferences.setPolling(requireContext(), true)
                        }
                        activity?.invalidateMenu()
                        return true
                    }
                    else -> true
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    inner class PhotoGalleryAdapter(private val galleryItems: List<GalleryItem>) :
        RecyclerView.Adapter<PhotoGalleryAdapter.PhotoGalleryViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoGalleryViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val view = layoutInflater.inflate(R.layout.list_item_gallery, parent, false)
            return PhotoGalleryViewHolder(view)
        }

        override fun onBindViewHolder(holder: PhotoGalleryViewHolder, position: Int) {
            val galleryItem = galleryItems[position]
            val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.bill_up_close)
                ?: ColorDrawable()
            holder.bindImage(drawable, galleryItem.title)
            thumbnailDownloader.queueThumbnail(holder, galleryItem.url, galleryItem.title)
        }

        override fun getItemCount(): Int = galleryItems.size

        inner class PhotoGalleryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val imageView: ImageView = itemView.findViewById(R.id.image_view)
            private val textTitleImage: TextView = itemView.findViewById(R.id.title_image)

            val bindImage: (Drawable, CharSequence) -> Unit = { image, title ->
                imageView.setImageDrawable(image)
                textTitleImage.text = title
            }
        }
    }
}