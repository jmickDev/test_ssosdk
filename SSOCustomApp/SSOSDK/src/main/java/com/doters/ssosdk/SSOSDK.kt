package com.doters.ssosdk

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.net.UrlQuerySanitizer
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
// import com.example.ssocustomapp.R
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.*
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.*


interface SSOAPI {
    @GET("user")
    suspend fun getUserInfo(@Header("Authorization") AuthToken: String) : Response<UserInfo>

    @FormUrlEncoded
    @POST("token/introspection")
    suspend fun tokenintrospection(@HeaderMap headers: Map<String, String>, @Field("token") authToken: String, @Field("token_type_hint") tokenTypeHint: String) : Response<Introspection>

    @FormUrlEncoded
    @POST("token")
    suspend fun refreshToken(@HeaderMap headers: Map<String, String>, @Field("refresh_token") authToken: String, @Field("grant_type") tokenTypeHint: String) : Response<RefresToken>
}

object RetrofitHelper {
    val baseUrl: String = "https://auth-api-gw-test.doters.io/v1/"

    fun getInstance(): Retrofit {
        return Retrofit.Builder().baseUrl(baseUrl)
            //.addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            // we need to add converter factory to
            // convert JSON object to Java object
            .build()
    }
}

class SSOSDK constructor(clientId: String, clientSecret: String) : AppCompatActivity() {

    private val _clientId: String = clientId
    private val _clientSecret: String = clientSecret

    // Instanciación de las customTabs
    private val builder = CustomTabsIntent.Builder()
    private val sanitizer: UrlQuerySanitizer = UrlQuerySanitizer()
    private lateinit var contexto: Context

    private val apiURL: String = "https://auth-api-gw-test.doters.io/v1"
    private val userInfoPath: String = "/user"
    private val refreshTokenPath: String = "/token"
    private val validateTokenPath: String = "/token/introspection"

    private val JSON: MediaType? = MediaType.parse("application/json; charset=utf-8")
    private val client = OkHttpClient()
    //private lateinit var contexto: Context

    // Creando instancia de retrofit para peticiones REST
    //private val SSOApi = RetrofitHelper.getInstance().create(SSOAPI::class.java)


    // Nombre del paquete del navegador de chrome mobile
    private var package_name = "com.android.chrome"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        contexto = applicationContext

        // Definción de parametros para personalizar el toolbar del contenedor donde se cargara el SSO
        val params = CustomTabColorSchemeParams.Builder()
        // params.setToolbarColor((ContextCompat.getColor(contexto, R.color.sso_primary)))
        builder.setDefaultColorSchemeParams(params.build())
    }

    // Interfaz callback de response de peticion de getUserInfo
    interface UserInfoCallback {
        fun processFinish(success: Boolean, data: UserInfo?)
    }

    interface IntrospectionCallback {
        fun processFinish(success: Boolean, data: Introspection?)
    }

    interface RefreshTokenCallback {
        fun processFinish(success: Boolean, data: RefresToken?)
    }

    // Metodo de SDK para login
    fun loginSSO(redirectURI: String, context: Context){
        loadSSO(redirectURI, context);
    }

    // Metodo de SDK para login
    fun logoutSSO(redirectURI: String, context: Context){
        loadSSO(redirectURI, context);
    }

    fun getUserInfo(authToken: String, callback: UserInfoCallback) {
        println("======> getUserInfo: "+authToken)
        val SSOApi = RetrofitHelper.getInstance().create(SSOAPI::class.java)
        GlobalScope.launch {
            val response = SSOApi.getUserInfo("Bearer " + authToken)
            if (response != null) {
                // Checking the results
                if(response.isSuccessful) {
                    println("=======> userInfo response: " + response.body().toString())
                    callback.processFinish(true, response.body())
                }else {
                    println("=======> error response: " + response.code())
                    callback.processFinish(false, null)
                }
            } else {
                println("=======> userInfo response: " + response)
                callback.processFinish(false, null)
            }
            //this.rest_request("get", this.apiURL+this.userInfoPath, authToken, "", callback)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshToken(refreshToken: String, callback: RefreshTokenCallback) {
        val basicToken: String = generateBasicToken(this._clientId, this._clientSecret)
        println("======> refreshToken: "+basicToken+" : "+refreshToken)

        val SSOApi = RetrofitHelper.getInstance().create(SSOAPI::class.java)

        /*val bodyJsonStr: String = ("{'token':'" + refreshToken +"',"
                + "'grant_type':'refresh_token'}")*/
        val headers: Map<String, String> = mapOf("Authorization" to "Basic " + basicToken, "Content-Type" to "application/x-www-form-urlencoded")

        GlobalScope.launch {
            val response = SSOApi.refreshToken(headers, refreshToken, "refresh_token")
            if (response != null) {
                // Checking the results
                if(response.isSuccessful) {
                    println("=======> userInfo response: " + response.body().toString())
                    callback.processFinish(true, response.body())
                }else {
                    println("=======> error response: " + (response.errorBody()?.string() ?: "Nada"))
                    callback.processFinish(false, null)
                }
            } else {
                println("=======> userInfo response: " + response)
                callback.processFinish(false, null)
            }
            //this.rest_request("get", this.apiURL+this.userInfoPath, authToken, "", callback)
        }

        /*val response = this.rest_request("post", this.apiURL+this.refreshTokenPath, basicToken, bodyJsonStr, callback)
        return response*/
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun verifyToken(authToken: String, callback: IntrospectionCallback) {
        val basicToken: String = generateBasicToken(this._clientId, this._clientSecret)
        println("======> verifyToken: "+basicToken+" : "+authToken)

        val SSOApi = RetrofitHelper.getInstance().create(SSOAPI::class.java)

        // val body = VerifyTokenModel(authToken, "access_token")
        /*val body: RequestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("token", authToken)
            .addFormDataPart("token_type_hint", "access_token")
            .build()*/
        /* val bodyJsonStr: String = ("{'token':'" + authToken +"',"
                + "'token_type_hint':'access_token'}")
        val body: RequestBody = RequestBody.create(MediaType.parse("application/json"), bodyJsonStr)*/

        val headers: Map<String, String> = mapOf(
            "Authorization" to "Basic " + basicToken,
            "Content-Type" to "application/x-www-form-urlencoded"
        )

        GlobalScope.launch {
            val response = SSOApi.tokenintrospection(headers, authToken, "access_token")
           if (response != null) {
                // Checking the results
                if(response.isSuccessful) {
                    println("=======> userInfo response: " + response.body().toString())
                    callback.processFinish(true, response.body())
                }else {
                    println("=======> error response: " + (response.errorBody()?.string() ?: "Nada"))
                    callback.processFinish(false, null)
                }
            } else {
                println("=======> userInfo response: " + response)
                callback.processFinish(false, null)
            }
            //this.rest_request("get", this.apiURL+this.userInfoPath, authToken, "", callback)
        }

        /*val response = this.rest_request("post", this.apiURL+this.validateTokenPath, basicToken, bodyJsonStr, callback)
        return response*/
    }

    // Funcion proncipal con logica para carga de customTabs
    private fun loadSSO(redirectURI: String, contexto2: Context){
        // Ejecucion de custom tabs
        contexto = contexto2
        val customBuilder = builder.build()
        val params = CustomTabColorSchemeParams.Builder()
        // params.setToolbarColor((ContextCompat.getColor(contexto, R.color.sso_primary)))
        builder.setDefaultColorSchemeParams(params.build())

        if (this.isPackageInstalled(package_name)) {
            // if chrome is available use chrome custom tabs
            println("======> Abriendo SSO con custom tabs");

            //customTabsLogin = true
            customBuilder.intent.setPackage(package_name)
            customBuilder.intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            customBuilder.launchUrl(contexto, Uri.parse(redirectURI))
        } else {
            println("======> No se encuentra instalado el navegador chrome!!!!")
            // if not available use WebView to launch the url
        }
        //------------------
    }

    // ============================== Utils =================================
    // Metodo para parsear queryParams de URI recibida por el deepLink
    fun parseURI(URI: Uri?): Map<String, String>? {
        var response: Map<String, String>? = null

        if(URI != null) {
            println("======> onCreate -> get data: " +  URI)

            try {
                sanitizer.setAllowUnregisteredParamaters(true);
                sanitizer.parseUrl(URI.toString());

                var accessToken: String = ""
                var expiresIn: String = ""
                var idToken: String = ""
                var refreshToken: String = ""
                var scope: String = ""
                var tokenType: String = ""
                var state: String = ""

                if (sanitizer.getValue("access_token") != null) {
                    accessToken = sanitizer.getValue("access_token")
                }
                if (sanitizer.getValue("expires_in") != null) {
                    expiresIn = sanitizer.getValue("expires_in")
                }
                if (sanitizer.getValue("id_token") != null) {
                    idToken = sanitizer.getValue("id_token")
                }
                if (sanitizer.getValue("refresh_token") != null) {
                    refreshToken = sanitizer.getValue("refresh_token")
                }
                if (sanitizer.getValue("scope") != null) {
                    scope = sanitizer.getValue("scope")
                }
                if (sanitizer.getValue("token_type") != null) {
                    tokenType = sanitizer.getValue("token_type")
                }
                if (sanitizer.getValue("state") != null) {
                    state = sanitizer.getValue("state")
                }


                response = mapOf(
                    "access_token" to accessToken,
                    "expires_in" to expiresIn,
                    "id_token" to idToken,
                    "refresh_token" to refreshToken,
                    "scope" to scope,
                    "token_type" to tokenType,
                    "state" to state
                );
            } catch (e: Exception) {
                println("======> Error al obtener params: " +  e)
            }
        } else {
            response = mapOf(
                "error" to "URI incorrecta"
            );
        }

        return response
    }

    // Funcion para validar si se tiene instalada una app con el package name
    private fun Context.isPackageInstalled(packageName: String): Boolean {
        // check if chrome is installed or not
        return try {
            contexto.packageManager.getPackageInfo(packageName, 0)
            // packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    // Funcion general para hacer peticiones REST
    private fun rest_request(type: String, url: String, authToken: String, bodyStr: String, callback: Callback): Call {
        var request: Request? = null

        if(type == "get") {
            request = Request.Builder()
                .header("Authorization", "Bearer "+authToken)
                .url(url)
                .build()
        } else if (type == "post") {
            val body: RequestBody = RequestBody.create(JSON, bodyStr)
            request = Request.Builder()
                .header("Authorization", "Basic "+authToken)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .url(url)
                .post(body)
                .build()
        }
        /*else {
            println("Tipo de peticion no soportada: " + type + ", favor de definirla")
            return null
        }*/

        // client.newCall(request).execute().use { response -> return response.body()?.string()!! }
        val call = client.newCall(request)
        call.enqueue(callback)
        return call

        /*client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) = println(response.body()?.string())
        })*/
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun generateBasicToken(clientId: String, clientSecret: String): String {
        val decodeBasicToken: String = clientId + ":" + clientSecret
        val basicTokenBA: ByteArray = decodeBasicToken.toByteArray()
        val basicTokenEncode = Base64.getEncoder().encodeToString(basicTokenBA)

        return basicTokenEncode
    }
}