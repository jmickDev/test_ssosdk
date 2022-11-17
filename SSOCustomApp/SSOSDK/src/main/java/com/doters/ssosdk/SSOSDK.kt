package com.doters.ssosdk

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging

import com.doters.ssosdk.models.Introspection
import com.doters.ssosdk.models.RefresToken
import com.doters.ssosdk.models.UserInfoData
import com.doters.ssosdk.restServices.RetrofitHelper
import com.doters.ssosdk.restServices.SSOAPI
import com.doters.ssosdk.commons.Utils
import com.doters.ssosdk.models.LoginData

class SSOSDK constructor(scheme: String, url: String, APIurl: String, language: String, clientId: String, clientSecret: String,  state: String) : AppCompatActivity() {

    private val logger = KotlinLogging.logger {}

    private val _scheme: String = scheme
    private val _url: String = url
    private val _APIurl: String = APIurl
    private val _language: String = language
    private val _clientId: String = clientId
    private val _clientSecret: String = clientSecret
    private val _state: String = state

    // URL para carga del SSO Login
    private var SSO_url = _url+"/?clientId="+_clientId+"&clientSecret="+_clientSecret+"&language="+_language+"&redirectUri="+_scheme+"&state="+_state
    // URL para carga del SSO Logout
    private var SSO_url_logout = _APIurl+"/v1/logout?post_logout_redirect_uri="+_scheme+"logout&client_id="+_clientId

    private val sdkUtils: Utils = Utils()

    // Instanciación de las customTabs
    private val builder = CustomTabsIntent.Builder()

    private lateinit var contexto: Context

    // Nombre del paquete del navegador de chrome mobile
    private var package_name = "com.android.chrome"

    // Interfaz callback de response de peticion de getUserInfo
    interface UserInfoCallback {
        fun processFinish(success: Boolean, data: UserInfoData?)
    }

    interface IntrospectionCallback {
        fun processFinish(success: Boolean, data: Introspection?)
    }

    interface RefreshTokenCallback {
        fun processFinish(success: Boolean, data: RefresToken?)
    }

    // Metodo de SDK para login
    fun signIn(context: Context){
        logger.info { "Opening the doters sso to do the LogIn" }
        loadSSO(this.SSO_url, context);
    }

    // Metodo de SDK para login
    fun logOut(context: Context){
        logger.info { "Opening the doters sso to do the LogOut" }
        loadSSO(this.SSO_url_logout, context);
    }

    fun UserInfo(accessToken: String, callback: UserInfoCallback) {
        val SSOApi = RetrofitHelper.getInstance(this._APIurl).create(SSOAPI::class.java)

        GlobalScope.launch {
            val response = SSOApi.getUserInfo("Bearer " + accessToken)
            if (response != null) {
                // Checking the results
                if(response.isSuccessful) {
                    callback.processFinish(true, response.body())
                }else {
                    logger.error { "Request to get user info failed, " + (response.errorBody()?.string() ?: "without error info")}
                    callback.processFinish(false, null)
                }
            } else {
                logger.error { "Request to get user info without response"}
                callback.processFinish(false, null)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun RefreshToken(refreshToken: String, callback: RefreshTokenCallback) {
        val basicToken: String = sdkUtils.generateBasicToken(this._clientId, this._clientSecret)
        val headers: Map<String, String> = mapOf("Authorization" to "Basic " + basicToken, "Content-Type" to "application/x-www-form-urlencoded")

        val SSOApi = RetrofitHelper.getInstance(this._APIurl).create(SSOAPI::class.java)

        GlobalScope.launch {
            val response = SSOApi.refreshToken(headers, refreshToken, "refresh_token")
            if (response != null) {
                // Checking the results
                if(response.isSuccessful) {
                    callback.processFinish(true, response.body())
                } else {
                    logger.error { "Request to refresh token failed, " + (response.errorBody()?.string() ?: "without error info")}
                    callback.processFinish(false, null)
                }
            } else {
                logger.error { "Request to refresh token without response"}
                callback.processFinish(false, null)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun TokenIntrospection(accessToken: String, callback: IntrospectionCallback) {
        val basicToken: String = sdkUtils.generateBasicToken(this._clientId, this._clientSecret)
        val headers: Map<String, String> = mapOf(
            "Authorization" to "Basic " + basicToken,
            "Content-Type" to "application/x-www-form-urlencoded"
        )

        val SSOApi = RetrofitHelper.getInstance(this._APIurl).create(SSOAPI::class.java)

        GlobalScope.launch {
            val response = SSOApi.tokenintrospection(headers, accessToken, "access_token")
           if (response != null) {
                // Checking the results
                if(response.isSuccessful) {
                    callback.processFinish(true, response.body())
                }else {
                    logger.error { "Request to verify token failed, " + (response.errorBody()?.string() ?: "without error info")}
                    callback.processFinish(false, null)
                }
            } else {
                logger.error { "Request to verify token without response"}
                callback.processFinish(false, null)
            }
        }
    }

    fun parseURI(uri: Uri): LoginData?{
        return sdkUtils.parseURI(uri)
    }

    // Funcion proncipal con logica para carga de customTabs
    private fun loadSSO(redirectURI: String, contexto2: Context){
        // Ejecucion de custom tabs
        contexto = contexto2
        val customBuilder = builder.build()
        val params = CustomTabColorSchemeParams.Builder()
        // params.setToolbarColor((ContextCompat.getColor(contexto, R.color.sso_primary)))
        builder.setDefaultColorSchemeParams(params.build())

        customBuilder.intent.setPackage(package_name)
        customBuilder.intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        customBuilder.launchUrl(contexto, Uri.parse(redirectURI))
    }
}