package examples.animal.forest.chat


import android.app.Activity
import android.content.Context
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.Toast
import examples.animal.forest.chat.services.NetworkReceiver


class NetworkActivity : Activity() {
    // The BroadcastReceiver that tracks network connectivity changes.
    private var receiver: NetworkReceiver? = NetworkReceiver()
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Registers BroadcastReceiver to track network connection changes.
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        receiver = NetworkReceiver()
        this.registerReceiver(receiver, filter)
    }

    public override fun onDestroy() {
        super.onDestroy()
        // Unregisters BroadcastReceiver when app is destroyed.
        if (receiver != null) {
            unregisterReceiver(receiver)
        }
    }

    // Refreshes the display if the network connection and the
    // pref settings allow it.
    public override fun onStart() {
        super.onStart()

        // Gets the user's network preference settings
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)

        // Retrieves a string value for the preferences. The second parameter
        // is the default value to use if a preference value is not found.
        sPref = sharedPrefs.getString("listPref", "Wi-Fi")
        updateConnectedFlags()
        if (refreshDisplay) {
            loadPage()
        }
    }

    // Checks the network connection and sets the wifiConnected and mobileConnected
    // variables accordingly.
    fun updateConnectedFlags() {
        val connMgr =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeInfo = connMgr.activeNetworkInfo
        if (activeInfo != null && activeInfo.isConnected) {
            wifiConnected =
                activeInfo.type == ConnectivityManager.TYPE_WIFI
            mobileConnected =
                activeInfo.type == ConnectivityManager.TYPE_MOBILE
        } else {
            wifiConnected = false
            mobileConnected = false
        }
    }

    // Uses AsyncTask subclass to download the XML feed from stackoverflow.com.
    fun loadPage() {
        if (sPref == ANY && (wifiConnected || mobileConnected)
            || sPref == WIFI && wifiConnected
        ) {
            // AsyncTask subclass
            Toast.makeText(this, "new DownloadXmlTask().execute(URL);", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "showErrorPage();", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val WIFI = "Wi-Fi"
        const val ANY = "Any"
        private const val URL =
            "http://stackoverflow.com/feeds/tag?tagnames=android&sort;=newest"

        // Whether there is a Wi-Fi connection.
        private var wifiConnected = false

        // Whether there is a mobile connection.
        private var mobileConnected = false

        // Whether the display should be refreshed.
        var refreshDisplay = true

        // The user's current network preference setting.
        var sPref: String? = null
    }
}