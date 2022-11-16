package com.doters.ssosdk

import com.google.gson.annotations.SerializedName

data class VerifyTokenModel(
    @SerializedName("token") var token: String,
    @SerializedName("token_type_hint") var tokenTypeHint: String,
)
