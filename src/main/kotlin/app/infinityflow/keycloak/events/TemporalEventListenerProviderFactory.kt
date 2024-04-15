package app.infinityflow.keycloak.events

import io.temporal.client.WorkflowClient
import io.temporal.client.WorkflowClientOptions
import io.temporal.serviceclient.SimpleSslContextBuilder
import io.temporal.serviceclient.WorkflowServiceStubs
import io.temporal.serviceclient.WorkflowServiceStubsOptions
import java.io.FileInputStream
import org.jboss.logging.Logger
import org.keycloak.Config
import org.keycloak.events.EventListenerProvider
import org.keycloak.events.EventListenerProviderFactory
import org.keycloak.models.KeycloakSession
import org.keycloak.models.KeycloakSessionFactory

class TemporalEventListenerProviderFactory : EventListenerProviderFactory {
    private val _logger = Logger.getLogger(TemporalEventListenerProviderFactory::class.java)
    private var _workflowClient: WorkflowClient? = null
    private var _server = "localhost:7233"
    private var _namespace = "default"
    private var _taskQueue = "keycloak"
    private var _clientCert = ""
    private var _clientCertKey = ""

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

        _server = config?.get("server") ?: server
        _namespace = config?.get("namespace") ?: namespace
        _taskQueue = config?.get("task-queue") ?: taskQueue
        _clientCert = config?.get("client-cert") ?: clientCert
        _clientCertKey = config?.get("client-cert-key") ?: clientCertKey

        val workflowServiceStubsOptionsBuilder = WorkflowServiceStubsOptions.newBuilder()
            .setTarget(server)

        if (clientCert.isNotEmpty() && clientCertKey.isNotEmpty()) {
            FileInputStream(clientCert).use { certInputStream ->
                FileInputStream(clientCertKey).use { keyInputStream ->
                    val sslContext = SimpleSslContextBuilder.forPKCS8(certInputStream, keyInputStream).build()
                    workflowServiceStubsOptionsBuilder.setSslContext(sslContext)
                }
            }
        }

        val workflowServiceStubs = WorkflowServiceStubs.newServiceStubs(workflowServiceStubsOptionsBuilder.build())
        
        workflowClient = WorkflowClient.newInstance(workflowServiceStubs, WorkflowClientOptions.newBuilder()
            .setNamespace(namespace)
            .build())
    }

    override fun postInit(factory: KeycloakSessionFactory?) {
        if (_logger.isDebugEnabled) {
            _logger.debugf("Global Initialization %s", TemporalEventListenerProviderFactory::class.java)
        }
    }

    override fun close() {
        if (_logger.isDebugEnabled) {
            _logger.debugf("Closing %s", TemporalEventListenerProviderFactory::class)
        }

        workflowClient?.close()  // Close the workflow client if it's not null
        _workflowClient = null
    }

    override fun getId(): String = "temporal"
}
