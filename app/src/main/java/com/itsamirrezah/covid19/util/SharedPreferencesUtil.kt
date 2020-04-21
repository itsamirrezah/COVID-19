package com.itsamirrezah.covid19.util

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

class SharedPreferencesUtil(context: Context) {

    private var pref: SharedPreferences? = null

    init {
        pref = PreferenceManager.getDefaultSharedPreferences(context)
    }

    var isFirstRun: Boolean
        get() = pref!!.getBoolean("IS_FIRST_RUN", true)
        set(value) = pref!!.edit().putBoolean("IS_FIRST_RUN", value).apply()

    companion object {
        var instance: SharedPreferencesUtil? = null

        fun getInstance(context: Context): SharedPreferencesUtil {
            if (instance == null) {
                instance = SharedPreferencesUtil(context)
            }
            return instance as SharedPreferencesUtil
        }
    }
}