package examples.animal.forest.chat


import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.example.chatexample.BuildConfig
import com.example.chatexample.R
import com.pubnub.api.PNConfiguration
import com.pubnub.api.PubNub
import com.pubnub.api.enums.PNLogVerbosity
import com.pubnub.api.enums.PNReconnectionPolicy
import examples.animal.forest.chat.fragments.ChatFragment
import examples.animal.forest.chat.prefs.Prefs
import examples.animal.forest.chat.services.NetworkReceiver
import examples.animal.forest.chat.util.ParentActivityImpl
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : ParentActivity(), ParentActivityImpl {
    // The BroadcastReceiver that tracks network connectivity changes.
    private var networkReceiver: NetworkReceiver? = NetworkReceiver()

    // tag::INIT-1.1[]
    override lateinit var pubNub // a field of MainActivity.java
            : PubNub

    // end::INIT-1.1[]
    val channel = "demo-animal-forest"
    private var chatFragment: ChatFragment? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // tag::ignore[]
        setSupportActionBar(toolbar)
        initializePubNub()
        chatFragment = ChatFragment.newInstance(channel).apply {
            addFragment(this)
        }
        initReceiver()
        // end::ignore[]
    }

    private fun initReceiver() {
        // Registers BroadcastReceiver to track network connection changes.
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        networkReceiver = NetworkReceiver()
        this.registerReceiver(networkReceiver, filter)
    }

    override fun provideLayoutResourceId(): Int {
        return R.layout.activity_main
    }

    private fun initializePubNub() {
        // tag::KEYS-2[]
        val pubKey: String = BuildConfig.PUB_KEY
        val subKey: String = BuildConfig.SUB_KEY
        // end::KEYS-2[]

        // tag::INIT-1.2[]
        val pnConfiguration = PNConfiguration()
        pnConfiguration.publishKey = pubKey
        pnConfiguration.subscribeKey = subKey
        pnConfiguration.uuid = Prefs.get()?.uuid() // uuid is stored in SharedPreferences
        pnConfiguration.logVerbosity = PNLogVerbosity.BODY
        pnConfiguration.reconnectionPolicy = PNReconnectionPolicy.LINEAR
        pnConfiguration.maximumReconnectionRetries = 10
        pubNub = PubNub(pnConfiguration)
        // end::INIT-1.2[]
    }

    // end::INIT-3[]
    override fun setTitle(title: String?) {
        toolbar.title = title
    }

    override fun setSubtitle(subtitle: String?) {
        if (!TextUtils.isEmpty(subtitle)) {
            toolbar.subtitle = subtitle
        }
    }

    override fun addFragmentToActivity(fragment: Fragment) {
        super.addFragment(fragment)
    }

    override fun enableBackButton(enable: Boolean) {
        supportActionBar?.setDisplayHomeAsUpEnabled(enable)
        supportActionBar?.setDisplayShowHomeEnabled(enable)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            backPress()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun backPress() {
        onBackPressed()
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount <= 1) {
            finish()
        } else {
            super.onBackPressed()
        }
    }

    fun networkAvailable(available: Boolean) {
        if (available) chatFragment?.onConnected()
    }

    // tag::INIT-4[]
    override fun onDestroy() {
        pubNub.unsubscribeAll()
        pubNub.forceDestroy()
        super.onDestroy()
        // tag::ignore[]
        if (networkReceiver != null) {
            this.unregisterReceiver(networkReceiver)
        }
        // end::ignore[]
    } // end::INIT-4[]
}
