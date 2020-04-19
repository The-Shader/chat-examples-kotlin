package examples.animal.forest.chat.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import examples.animal.forest.chat.MainActivity


class NetworkReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val conn =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = conn.activeNetworkInfo
        (context as MainActivity).networkAvailable(networkInfo != null && networkInfo.isConnected)
    }
}