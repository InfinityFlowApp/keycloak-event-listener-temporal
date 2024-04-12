package app.infinityflow.keycloak.events

import app.infinityflow.keycloak.model.admin.AuthDetails
import app.infinityflow.keycloak.workflows.actions.KeycloakEventWorkflow
import app.infinityflow.keycloak.workflows.admin.KeycloakAdminEventWorkflow
import io.temporal.api.enums.v1.WorkflowIdReusePolicy
import io.temporal.client.WorkflowClient
import io.temporal.client.WorkflowClientOptions
import io.temporal.client.WorkflowOptions
import io.temporal.serviceclient.WorkflowServiceStubs
import io.temporal.serviceclient.WorkflowServiceStubsOptions
import org.jboss.logging.Logger
import org.keycloak.events.Event
import org.keycloak.events.EventListenerProvider
import org.keycloak.events.admin.AdminEvent
import org.keycloak.models.KeycloakSession
import java.util.Properties

internal class TemporalEventListenerProvider(session: KeycloakSession?, properties: Properties) : EventListenerProvider {

    private val _logger = Logger.getLogger(TemporalEventListenerProvider::class.java)
    private val _session: KeycloakSession? = session
    private var _properties: Properties = properties

    private var _workflowServiceStubsOptions: WorkflowServiceStubsOptions? = null
    private var _workflowServiceStubs: WorkflowServiceStubs? = null
    private var _workflowClient: WorkflowClient? = null

    fun init() {
        _logger.debugf("init(cluster=%s,workflow=%s)", _properties.getProperty("cluster"), _properties.getProperty("workflow-name"))
        _workflowServiceStubsOptions = WorkflowServiceStubsOptions
            .newBuilder()
            .setTarget(_properties.getProperty("cluster", "localhost:7233"))
            .build()
        _workflowServiceStubs = WorkflowServiceStubs
            .newServiceStubs(_workflowServiceStubsOptions)
        _workflowClient = WorkflowClient
            .newInstance(_workflowServiceStubs, WorkflowClientOptions
                .newBuilder()
                .setNamespace(_properties.getProperty("namespace"))
                .build())
    }

    fun postInit() {
        _logger.debugf("postInit(cluster=%s,workflow=%s)", _properties.getProperty("cluster"), _properties.getProperty("workflow-name"))
    }

    /**
     * @inherit
     */
    override fun close() {
        _logger.debugf("Closing %s", TemporalEventListenerProvider::class)
        _session?.close()
    }

    /**
     * @inherit
     */
    override fun onEvent(event: Event?) {
        if (event == null) {
            return
        }

        _logger.debugf("onEvent(id=%s,time=%i,type=%s)", event.id, event.time, event.type)

        val eventParameter = app.infinityflow.keycloak.model.actions.Event(
            event.id,
            event.time,
            event.type?.toString(),
            event.realmId,
            event.clientId,
            event.userId,
            event.sessionId,
            event.ipAddress,
            event.error,
            event.details
        )

        val eventWorkflowStub = _workflowClient?.newWorkflowStub(
            KeycloakEventWorkflow::class.java,
            WorkflowOptions
                .newBuilder()
                .setTaskQueue(_properties.getProperty("task-queue"))
                .build())
        val execution = WorkflowClient.start(eventWorkflowStub!!::send, eventParameter)
        _logger.debugf("Executing workflow (workflowId=%s, runId=%s)", execution.workflowId, execution.runId)
    }

    /**
     * @inherit
     */
    override fun onEvent(event: AdminEvent?, includeRepresentation: Boolean) {
        if (event == null) {
            return
        }

        _logger.debugf("onAdminEvent(id=%s,time=%i,resourceType=%s,operationType=%s)", event.id, event.time, event.resourceTypeAsString, event.operationType.name)

        var authDetails: AuthDetails? = null
        var representation: String? = null

        if (event.authDetails != null) {
            authDetails = AuthDetails(event.authDetails.realmId, event.authDetails.clientId, event.authDetails.userId)
        }

        if (includeRepresentation) {
            representation = event.representation
        }

        val eventParameter = app.infinityflow.keycloak.model.admin.Event(
            event.id,
            event.time,
            event.realmId,
            authDetails,
            event.resourceTypeAsString,
            event.operationType?.toString(),
            event.resourcePath,
            representation,
            event.error
        )

        val eventEventWorkflowStub = _workflowClient?.newWorkflowStub(
            KeycloakAdminEventWorkflow::class.java,
            WorkflowOptions
                .newBuilder()
                .setWorkflowIdReusePolicy(WorkflowIdReusePolicy.WORKFLOW_ID_REUSE_POLICY_ALLOW_DUPLICATE)
                .setTaskQueue(_properties.getProperty("task-queue"))
                .build())
        val execution = WorkflowClient.start(eventEventWorkflowStub!!::send, eventParameter)
        _logger.debugf("Executing workflow (workflowId=%s, runId=%s)", execution.workflowId, execution.runId)
    }

    init {
        _logger.debugf("Init %s", TemporalEventListenerProvider::class)
    }
}
