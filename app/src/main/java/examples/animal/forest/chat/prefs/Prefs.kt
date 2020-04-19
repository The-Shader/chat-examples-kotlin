package examples.animal.forest.chat.prefs

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import examples.animal.forest.chat.model.Users
import examples.animal.forest.chat.util.Helper

class Prefs private constructor(appContext: Context) {
    private val prefs: SharedPreferences
    fun pubKey(): String? {
        return prefs.getString(KEY_PUB, null)
    }

    @SuppressLint("ApplySharedPref")
    fun pubKey(pubKey: String?) {
        prefs.edit().putString(KEY_PUB, pubKey).commit()
    }

    fun subKey(): String? {
        return prefs.getString(KEY_SUB, null)
    }

    @SuppressLint("ApplySharedPref")
    fun subKey(subKey: String?) {
        prefs.edit().putString(KEY_SUB, subKey).commit()
    }

    @SuppressLint("ApplySharedPref")
    fun uuid(): String? {
        if (!prefs.contains(KEY_UUID)) {
            prefs.edit()
                .putString(KEY_UUID, Helper.getRandomElement(Users.all()).uuid)
                .commit()
        }
        return prefs.getString(KEY_UUID, null)
    }

    @SuppressLint("ApplySharedPref")
    fun uuid(uuid: String?) {
        prefs.edit().putString(KEY_UUID, uuid).commit()
    }

    @SuppressLint("ApplySharedPref")
    fun clearAllData() {
        prefs.edit().clear().commit()
    }

    companion object {
        private const val TAG = "Prefs"
        private var sUniqueInstance: Prefs? = null
        private const val PREFS_NAME = "global_preferences"
        private const val KEY_PUB = "pub"
        private const val KEY_SUB = "sub"
        private const val KEY_UUID = "uuid"
        fun get(): Prefs? {
            checkNotNull(sUniqueInstance) { "Prefs is not initialized, call initialize method first." }
            return sUniqueInstance
        }

        fun initialize(appContext: Context?) {
            if (appContext == null) {
                throw NullPointerException("Provided application context is null")
            }
            if (sUniqueInstance == null) {
                sUniqueInstance = Prefs(appContext)
            }
        }
    }

    init {
        prefs = appContext.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
    }
}
