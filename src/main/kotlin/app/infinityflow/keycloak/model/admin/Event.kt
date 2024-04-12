package app.infinityflow.keycloak.model.admin

data class Event(
    val id: String? = null,
    val time: Long = 0,
    val realmId: String? = null,
    val authDetails: AuthDetails? = null,
    val resourceType: String? = null,
    val operationType: String? = null,
    val resourcePath: String? = null,
    val representation: String? = null,
    val error: String? = null)
