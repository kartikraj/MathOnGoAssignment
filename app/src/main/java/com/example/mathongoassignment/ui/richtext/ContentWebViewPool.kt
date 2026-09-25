package com.example.mathongoassignment.ui.richtext

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf

class ContentWebViewPool(private val context: Context) {

    private val holders = LinkedHashMap<String, ContentWebViewHolder>()

    fun acquire(slotKey: String): ContentWebViewHolder =
        holders.getOrPut(slotKey) { ContentWebViewHolder(context) }

    fun destroy() {
        holders.values.forEach(ContentWebViewHolder::destroy)
        holders.clear()
    }
}

val LocalContentWebViewPool = staticCompositionLocalOf<ContentWebViewPool> {
    error("No ContentWebViewPool provided")
}
