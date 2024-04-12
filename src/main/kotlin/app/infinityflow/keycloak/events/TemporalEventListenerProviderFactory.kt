package app.infinityflow.keycloak.events

import org.jboss.logging.Logger
import org.keycloak.Config
import org.keycloak.events.EventListenerProvider
import org.keycloak.events.EventListenerProviderFactory
import org.keycloak.models.KeycloakSession
import org.keycloak.models.KeycloakSessionFactory
import java.util.*

class TemporalEventListenerProviderFactory : EventListenerProviderFactory {
    private val _logger = Logger.getLogger(TemporalEventListenerProviderFactory::class.java)
    private val _properties = Properties()
    private var _provider: TemporalEventListenerProvider? = null

    override fun create(session: KeycloakSession?): EventListenerProvider {
        _logger.debug("factory:create TemporalEventListenerProviderFactory")
        _provider = TemporalEventListenerProvider(session, _properties)
        return _provider as TemporalEventListenerProvider
    }

    override fun init(config: Config.Scope?) {
        _logger.debugf("factory:init")
        val cluster = config?.get("cluster", "localhost:7233")
        val taskQueue = config?.get("task-queue", "keycloak")
        val namespace = config?.get("namespace", "default")

        _properties.setProperty("cluster", cluster)
        _properties.setProperty("task-queue", taskQueue)
        _properties.setProperty("namespace", namespace)

        _provider?.init()
    }

    override fun postInit(factory: KeycloakSessionFactory?) {
        _logger.debugf("factory:postInit")
        _provider?.postInit()
    }

    override fun close() {
        _logger.debugf("factory:close")
        _provider?.close()
        _provider = null
    }

    override fun getId(): String {
        _logger.debugf("factory:getId")
        return "event-listener-temporal"
    }
}
