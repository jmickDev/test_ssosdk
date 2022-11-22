package com.doters.ssosdk.models

data class Introspection(
    val active: Boolean,
    val sub: String,
    val clientId: String,
    val exp: Long,
    val iat: Long,
    val iss: String,
    val scope: String,
    val tokenType: String
) {
    constructor() : this(false, "", "", 0, 0, "", "", "")
}
