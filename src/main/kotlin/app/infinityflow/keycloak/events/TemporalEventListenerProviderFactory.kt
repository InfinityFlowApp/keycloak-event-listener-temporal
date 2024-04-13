package app.infinityflow.keycloak.events

import io.temporal.client.WorkflowClient
import io.temporal.client.WorkflowClientOptions
import io.temporal.serviceclient.WorkflowServiceStubs
import io.temporal.serviceclient.WorkflowServiceStubsOptions
import org.jboss.logging.Logger
import org.keycloak.Config
import org.keycloak.events.EventListenerProvider
import org.keycloak.events.EventListenerProviderFactory
import org.keycloak.models.KeycloakSession
import org.keycloak.models.KeycloakSessionFactory

class TemporalEventListenerProviderFactory : EventListenerProviderFactory {
    private val _logger = Logger.getLogger(TemporalEventListenerProviderFactory::class.java)
    private var _workflowClient: WorkflowClient? = null
    private var _server = ""
    private var _namespace = ""
    private var _taskQueue = ""


    override fun create(session: KeycloakSession?): EventListenerProvider {
        if (_logger.isDebugEnabled) {
            _logger.debugf("Creating %s", TemporalEventListenerProvider::class)
        }
        return TemporalEventListenerProvider(_workflowClient!!, _taskQueue)
    }

    override fun init(config: Config.Scope?) {
        if (_logger.isDebugEnabled) {
            _logger.debugf("Initializing %s", TemporalEventListenerProviderFactory::class)
        }

        _server = config?.get("server") ?: "localhost:7233"
        _namespace = config?.get("namespace") ?: "default"
        _taskQueue = config?.get("task-queue") ?: "keycloak"

        val workflowServiceStubsOptions = WorkflowServiceStubsOptions
            .newBuilder()
            .setTarget(_server)
            .build()
        val workflowServiceStubs = WorkflowServiceStubs
            .newServiceStubs(workflowServiceStubsOptions)
        _workflowClient = WorkflowClient
            .newInstance(workflowServiceStubs, WorkflowClientOptions
                .newBuilder()
                .setNamespace(_namespace)
                .build())
    }

    override fun postInit(factory: KeycloakSessionFactory?) {
        if (_logger.isDebugEnabled) {
            _logger.debugf("Global Initialization %s", TemporalEventListenerProviderFactory::class)
        }
    }

    override fun close() {
        if (_logger.isDebugEnabled) {
            _logger.debugf("Closing %s", TemporalEventListenerProviderFactory::class)
        }
        _workflowClient = null
    }

    override fun getId(): String {
        return "temporal"
    }
}
