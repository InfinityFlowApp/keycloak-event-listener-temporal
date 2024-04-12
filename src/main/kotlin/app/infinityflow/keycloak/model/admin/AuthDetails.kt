package app.infinityflow.keycloak.model.admin

data class AuthDetails(
    val realmId: String? = null,
    val clientId: String? = null,
    val userId: String? = null,
    val ipAddress: String? = null)
