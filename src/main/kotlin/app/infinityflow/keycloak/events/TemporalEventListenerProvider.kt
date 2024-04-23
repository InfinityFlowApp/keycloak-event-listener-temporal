package app.infinityflow.keycloak.events

import app.infinityflow.keycloak.model.admin.AuthDetails
import app.infinityflow.keycloak.workflows.actions.KeycloakEventWorkflow
import app.infinityflow.keycloak.workflows.admin.KeycloakAdminEventWorkflow
import io.temporal.client.WorkflowClient
import io.temporal.client.WorkflowOptions
import org.jboss.logging.Logger
import org.keycloak.events.Event
import org.keycloak.events.EventListenerProvider
import org.keycloak.events.admin.AdminEvent

internal class TemporalEventListenerProvider(workflowClient: WorkflowClient, taskQueue: String) : EventListenerProvider {

    private val _logger = Logger.getLogger(TemporalEventListenerProvider::class.java)
    private val _workflowClient = workflowClient
    private val _taskQueue = taskQueue

    /**
     * @inherit
     */
    override fun close() {
        if (_logger.isDebugEnabled) {
            _logger.debugf("Closing %s", TemporalEventListenerProvider::class)
        }
    }

    /**
     * @inherit
     */
    override fun onEvent(event: Event?) {
        if (event == null) {
            return
        }

        if (_logger.isDebugEnabled) {
            _logger.debugf("onEvent(id=%s,time=%d,type=%s)", event.id, event.time, event.type)
        }

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

        val eventWorkflowStub = _workflowClient
            .newUntypedWorkflowStub(
                "KeycloakEventWorkflow",
                WorkflowOptions
                    .newBuilder()
                    .setTaskQueue(_taskQueue)
                    .setWorkflowId(event.id)
                    .build())

        try {
            if (_logger.isDebugEnabled) {
                _logger.debugf("Starting workflow %s", eventWorkflowStub)
            }

            eventWorkflowStub.start(eventParameter)

            if (_logger.isDebugEnabled)
            {
                _logger.debugf("Started workflow (workflowId=%s, runId=%s)", execution.workflowId, execution.runId)
            }
        } catch (e: Exception) {
            _logger.errorf(e, "Error while starting workflow %s", eventWorkflowStub)
        }
    }

    /**
     * @inherit
     */
    override fun onEvent(event: AdminEvent?, includeRepresentation: Boolean) {
        if (event == null) {
            return
        }

        if (_logger.isDebugEnabled) {
            _logger.debugf("onAdminEvent(id=%s,time=%d,resourceType=%s,operationType=%s)", event.id, event.time, event.resourceTypeAsString, event.operationType.name)
        }

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

        val adminEventWorkflowStub = _workflowClient
            .newUntypedWorkflowStub(
                "KeycloakAdminEventWorkflow",
                WorkflowOptions
                    .newBuilder()
                    .setTaskQueue(_taskQueue)
                    .setWorkflowId(event.id)
                    .build())

        try {
            if (_logger.isDebugEnabled) {
                _logger.debugf("Starting workflow %s", adminEventWorkflowStub)
            }

            adminEventWorkflowStub.start(eventParameter)

            if (_logger.isDebugEnabled)
            {
                _logger.debugf("Started workflow (workflowId=%s, runId=%s)", execution.workflowId, execution.runId)
            }
        } catch (e: Exception) {
            _logger.errorf(e, "Error while starting workflow %s", adminEventWorkflowStub)
        }
    }
}
