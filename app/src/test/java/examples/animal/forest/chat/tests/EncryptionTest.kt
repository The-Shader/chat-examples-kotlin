package examples.animal.forest.chat.tests

import com.pubnub.api.PNConfiguration
import com.pubnub.api.PubNub
import com.pubnub.api.PubNubException
import examples.animal.forest.chat.tests.TestHarness
import org.junit.Assert
import org.junit.Test
import java.util.*

class EncryptionTest : TestHarness() {
    @Test
    fun testEnableEncryption() {
        // tag::ENCR-1[]
        val pnConfiguration = PNConfiguration()
        pnConfiguration.subscribeKey = SUB_KEY
        pnConfiguration.publishKey = PUB_KEY
        pnConfiguration.isSecure = true
        val pubNub = PubNub(pnConfiguration)
        // end::ENCR-1[]
        Assert.assertNotNull(pubNub)
        Assert.assertTrue(pubNub.configuration.isSecure)
    }

    @Test
    @Throws(PubNubException::class)
    fun testCipherKey() {
        val expectedCipherKey = UUID.randomUUID().toString()
        val expectedString = UUID.randomUUID().toString()

        // tag::ENCR-2[]
        val pnConfiguration = PNConfiguration()
        pnConfiguration.subscribeKey = SUB_KEY
        pnConfiguration.publishKey = PUB_KEY
        // tag::ignore[]
        /*
        // end::ignore[]
        pnConfiguration.setCipherKey("myCipherKey");
        // tag::ignore[]
        */
        // end::ignore[]
        // tag::ignore[]
        pnConfiguration.cipherKey = expectedCipherKey
        // end::ignore[]
        val pubNub = PubNub(pnConfiguration)
        // end::ENCR-2[]
        Assert.assertNotNull(pubNub)
        Assert.assertEquals(expectedCipherKey, pubNub.configuration.cipherKey)
        val encrypted = pubNub.encrypt(expectedString, expectedCipherKey)
        val decrypted = pubNub.decrypt(encrypted, expectedCipherKey)
        Assert.assertEquals(expectedString, decrypted)
    }

    @Test
    fun testSendSignedMessage() {
        // tag::ENCR-3[]
        // in progress
        // end::ENCR-3[]
    }
}
