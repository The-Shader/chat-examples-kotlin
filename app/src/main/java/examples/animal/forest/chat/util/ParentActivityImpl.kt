package examples.animal.forest.chat.util

import androidx.fragment.app.Fragment
import com.pubnub.api.PubNub


// tag::INIT-2[]
interface ParentActivityImpl {
    val pubNub: PubNub

    // tag::ignore[]
    fun setTitle(title: String?)
    fun setSubtitle(subtitle: String?)
    fun addFragmentToActivity(fragment: Fragment)
    fun enableBackButton(enable: Boolean)
    fun backPress() // end::ignore[]
}
// end::INIT-2[]