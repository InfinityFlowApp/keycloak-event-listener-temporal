package app.infinityflow.keycloak.workflows.actions

import app.infinityflow.keycloak.model.actions.Event
import io.temporal.workflow.WorkflowInterface
import io.temporal.workflow.WorkflowMethod

@WorkflowInterface
interface KeycloakEventWorkflow {
    @WorkflowMethod
    fun send(event: Event)
}
