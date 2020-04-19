package examples.animal.forest.chat.pubnub


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


class PubNubListener : SubscribeCallback() {
    override fun status(pubnub: PubNub, status: PNStatus) {}
    override fun message(pubnub: PubNub, message: PNMessageResult) {}
    override fun presence(pubnub: PubNub, presence: PNPresenceEventResult) {}
    override fun signal(pubnub: PubNub, pnSignalResult: PNSignalResult) {}
    override fun user(pubnub: PubNub, pnUserResult: PNUserResult) {}
    override fun messageAction(pubnub: PubNub, pnMessageActionResult: PNMessageActionResult) {}
    override fun membership(pubnub: PubNub, pnMembershipResult: PNMembershipResult) {}
    override fun space(pubnub: PubNub, pnSpaceResult: PNSpaceResult) {}
}