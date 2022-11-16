package SSO_SDK

data class Introspection(
    val active: Boolean,
    val sub: String,
    val client_id: String,
    val exp: Long,
    val iat: Long,
    val iss: String,
    val scope: String,
    val token_type: String
)
