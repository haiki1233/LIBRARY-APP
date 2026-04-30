package com.example.library_mobile.data.local

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class RecentSearchManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    fun getRecentSearches(): List<String> {
        val json = prefs.getString(KEY_RECENT, null) ?: return emptyList()
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    fun addSearch(keyword: String) {
        if (keyword.isBlank()) return
        val list = getRecentSearches().toMutableList()
        list.remove(keyword)        // Xóa nếu đã có (để thêm lên đầu)
        list.add(0, keyword)        // Thêm lên đầu
        val trimmed = list.take(10) // Giữ tối đa 10 từ khóa
        prefs.edit().putString(KEY_RECENT, gson.toJson(trimmed)).apply()
    }

    fun removeSearch(keyword: String) {
        val list = getRecentSearches().toMutableList()
        list.remove(keyword)
        prefs.edit().putString(KEY_RECENT, gson.toJson(list)).apply()
    }

    fun clearAll() {
        prefs.edit().remove(KEY_RECENT).apply()
    }

    companion object {
        private const val PREFS_NAME = "recent_search_prefs"
        private const val KEY_RECENT = "recent_searches"
    }
}