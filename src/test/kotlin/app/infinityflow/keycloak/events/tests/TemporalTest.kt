package app.infinityflow.keycloak.events.tests

import app.infinityflow.keycloak.events.TemporalEventListenerProviderFactory
import org.keycloak.events.Event
import org.keycloak.events.EventType
import org.keycloak.events.admin.AdminEvent
import org.keycloak.events.admin.AuthDetails
import org.keycloak.events.admin.OperationType
import org.keycloak.events.admin.ResourceType
import java.util.Date
import java.util.UUID
import kotlin.test.Test

class TemporalTest {

    @Test
    fun testConnect() {

        val factory = TemporalEventListenerProviderFactory()
        factory.init(TestScope())
        val provider = factory.create(null)

        val event = createEvent()
        val adminEvent = createAdminEvent()
        val nextAdminEvent = createAdminEvent()
        provider.onEvent(event)
        provider.onEvent(adminEvent, false)
        provider.onEvent(nextAdminEvent, true)

        provider.close()
        factory.close()
    }

    private fun createEvent(): Event {
        val details = mapOf("a" to "b")
        val ev = Event()
        ev.id = UUID.randomUUID().toString()
        ev.type = EventType.LOGIN
        ev.time = Date().toInstant().epochSecond
        ev.realmId = "test"
        ev.clientId = "test"
        ev.details = details
        ev.error = ""
        ev.ipAddress = "127.0.0.1"
        ev.sessionId = "test"
        ev.userId = "test"
        return ev
    }

    private fun createAdminEvent(): AdminEvent {
        val details = AuthDetails()
        details.clientId = "clientId"
        details.userId = "userId"
        details.ipAddress = "127.0.0.1"
        details.realmId = "realmId"
        val ev = AdminEvent()
        ev.id = UUID.randomUUID().toString()
        ev.operationType = OperationType.CREATE
        ev.realmId = "realm"
        ev.resourceTypeAsString = "CREATE"
        ev.time = Date().toInstant().epochSecond
        ev.resourceType = ResourceType.USER
        ev.authDetails = details
        ev.error = "error"
        ev.representation = "representation"
        ev.resourcePath = "resource/path/to/somewhere"
        return ev
    }
}
