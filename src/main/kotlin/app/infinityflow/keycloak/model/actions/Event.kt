package app.infinityflow.keycloak.model.actions

data class Event(
        val id: String?,
        val time: Long = 0,
        val type: String? = null,
        val realmId: String? = null,
        val clientId: String? = null,
        val userId: String? = null,
        val sessionId: String? = null,
        val ipAddress: String? = null,
        val error: String? = null,
        val details: Map<String, String?>? = null)  {
}
