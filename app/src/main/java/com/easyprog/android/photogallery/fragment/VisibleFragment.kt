package com.easyprog.android.photogallery.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.easyprog.android.photogallery.work_manager.PollWorker

abstract class VisibleFragment: Fragment() {

    private val onShowNotification = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            Toast.makeText(requireContext(), "Got a broadcast: ${intent.action}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter(PollWorker.ACTION_SHOW_NOTIFICATION)
        requireActivity().registerReceiver(onShowNotification, filter, PollWorker.PREM_PRIVATE, null)
    }

    override fun onStop() {
        super.onStop()
        requireActivity().unregisterReceiver(onShowNotification)
    }
}