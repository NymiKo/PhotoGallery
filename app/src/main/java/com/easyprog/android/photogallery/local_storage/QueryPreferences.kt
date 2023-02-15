package com.easyprog.android.photogallery.local_storage

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager

private const val PREF_SEARCH_QUERY = "searchQuery"
private const val PREF_LAST_RESULT_ID = "lastResultId"

object QueryPreferences {

    fun getStorageQuery(context: Context): String {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getString(PREF_SEARCH_QUERY, "")!!
    }

    fun setStorageQuery(context: Context, query: String) {
        PreferenceManager.getDefaultSharedPreferences(context).edit {
            putString(PREF_SEARCH_QUERY, query)
        }
    }

    fun getLastResultId(context: Context): String {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getString(PREF_LAST_RESULT_ID, "")!!
    }

    fun setLastResultId(context: Context, lastResultId: String) {
        PreferenceManager.getDefaultSharedPreferences(context).edit {
            putString(PREF_LAST_RESULT_ID, lastResultId)
        }
    }

}