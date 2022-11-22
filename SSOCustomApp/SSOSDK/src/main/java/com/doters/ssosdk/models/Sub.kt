package com.doters.ssosdk.models

data class Sub(
    val customerId: String,
    val user: String,
) {
    constructor() : this("", "")
}
