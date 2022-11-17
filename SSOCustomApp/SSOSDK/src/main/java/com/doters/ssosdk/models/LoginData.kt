package com.doters.ssosdk.models

data class LoginData(
    val access_token:String,
    val expires_in:Int,
    val id_token:String,
    val refresh_token:String,
    val scope:String,
    val token_type:String,
    val state:String,
    val error:String,
    val errorDescription:String
) {
    constructor() : this("", 0, "", "", "", "", "", "", "")
}
