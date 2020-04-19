package examples.animal.forest.chat.util

import com.pubnub.api.callbacks.SubscribeCallback


// tag::FRG-3[]
interface PNFragmentImpl {
    fun provideListener(): SubscribeCallback
}
// end::FRG-3[]
