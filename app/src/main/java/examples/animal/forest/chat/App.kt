package examples.animal.forest.chat

import android.app.Application
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import examples.animal.forest.chat.prefs.Prefs
import kotlin.math.sin
import kotlin.system.exitProcess

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        sInstance = this
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        Prefs.initialize(this)
    }

    fun clearCacheAndRestart(activity: AppCompatActivity) {
        Thread(Runnable {
            Prefs.get()?.clearAllData()
            activity.runOnUiThread { get().restartApplication(activity) }
        }).start()
    }

    private fun restartApplication(activity: AppCompatActivity) {
        val i = baseContext.packageManager
            .getLaunchIntentForPackage(baseContext.packageName)
        i!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        i.putExtra("restart", true)
        activity.finish()
        activity.startActivity(i)
        exitProcess(0)
    }

    companion object {
        private var sInstance: App? = null

        @Synchronized
        fun get(): App {
            if (sInstance == null) {
                sInstance =
                    App()
            }
            return sInstance!!
        }
    }
}