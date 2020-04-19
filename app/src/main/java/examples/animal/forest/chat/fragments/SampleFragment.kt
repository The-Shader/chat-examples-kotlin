package examples.animal.forest.chat.fragments


import com.pubnub.api.PubNub
import com.pubnub.api.callbacks.SubscribeCallback
import com.pubnub.api.models.consumer.PNStatus
import com.pubnub.api.models.consumer.pubsub.PNMessageResult
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult
import com.pubnub.api.models.consumer.pubsub.PNSignalResult
import com.pubnub.api.models.consumer.pubsub.message_actions.PNMessageActionResult
import com.pubnub.api.models.consumer.pubsub.objects.PNMembershipResult
import com.pubnub.api.models.consumer.pubsub.objects.PNSpaceResult
import com.pubnub.api.models.consumer.pubsub.objects.PNUserResult
import examples.animal.forest.chat.util.PNFragmentImpl


class SampleFragment : ParentFragment(), PNFragmentImpl {
    // tag::FRG-4.1[]
    private var pubNubListener // field of Fragment
            : SubscribeCallback = object : SubscribeCallback() {

        override fun membership(pubnub: PubNub, pnMembershipResult: PNMembershipResult) {}
        override fun message(pubnub: PubNub, pnMessageResult: PNMessageResult) {}
        override fun messageAction(pubnub: PubNub, pnMessageActionResult: PNMessageActionResult) {}
        override fun presence(pubnub: PubNub, pnPresenceEventResult: PNPresenceEventResult) {}
        override fun signal(pubnub: PubNub, pnSignalResult: PNSignalResult) {}
        override fun space(pubnub: PubNub, pnSpaceResult: PNSpaceResult) {}
        override fun status(pubnub: PubNub, pnStatus: PNStatus) {}
        override fun user(pubnub: PubNub, pnUserResult: PNUserResult) {}
    }

    // end::FRG-4.1[]
    override fun provideLayoutResourceId(): Int {
        return 0
    }

    override fun setViewBehaviour(viewFromCache: Boolean) {}
    override fun setScreenTitle(): String? {
        return null
    }

    // tag::FRG-4.2[]
    override fun onReady() {
    }


    override fun provideListener(): SubscribeCallback {
        return pubNubListener
    } // end::FRG-4.2[]
}
