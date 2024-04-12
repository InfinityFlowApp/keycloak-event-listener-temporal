package app.infinityflow.keycloak.workflows.admin

import app.infinityflow.keycloak.model.admin.Event
import io.temporal.workflow.WorkflowInterface
import io.temporal.workflow.WorkflowMethod

@WorkflowInterface
interface KeycloakAdminEventWorkflow {
    @WorkflowMethod
    fun send(event: Event)
}