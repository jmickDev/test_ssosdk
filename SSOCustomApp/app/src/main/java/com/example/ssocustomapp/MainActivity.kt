package com.example.ssocustomapp

import com.doters.ssosdk.Introspection
import com.doters.ssosdk.RefresToken
import com.doters.ssosdk.SSOSDK
import com.doters.ssosdk.UserInfo

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.ssocustomapp.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    // params de urls de login y logout
    private val redirectUri: String = "dosso://"
    private val clientId: String = "viva-web"
    private val clientSecret: String = "GlMTbnwjRA"
    private val language: String = "es-MX"
    private val state: String = "A245FG"

    private var ssosdk = SSOSDK(clientId, clientSecret)

    // URL para carga del SSO Login
    private var SSO_url = "https://auth-test.doters.io/?clientId="+clientId+"&clientSecret="+clientSecret+"&language="+language+"&redirectUri="+redirectUri+"&state="+state
    // URL para carga del SSO Logout
    private var SSO_url_logout = "https://auth-api-gw-test.doters.io/v1/logout?post_logout_redirect_uri="+redirectUri+"logout&client_id="+clientId
    // dosso.solemti.net
    var parsedlData: Map<String, String>? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Aqui se espera recibir los query params del redirecturi despues del login
        val dlData: Uri? = intent.data
        parsedlData = ssosdk.parseURI(dlData)

        binding.root.findViewById<TextView>(R.id.callback_response).movementMethod = ScrollingMovementMethod()

        if(!parsedlData.isNullOrEmpty()) {
            binding.root.findViewById<Button>(R.id.button_logout).isEnabled = true
            binding.root.findViewById<Button>(R.id.button_logout).isClickable = true

            binding.root.findViewById<Button>(R.id.userInfo_btn).isEnabled = true
            binding.root.findViewById<Button>(R.id.userInfo_btn).isClickable = true

            binding.root.findViewById<Button>(R.id.verify_btn).isEnabled = true
            binding.root.findViewById<Button>(R.id.verify_btn).isClickable = true

            binding.root.findViewById<Button>(R.id.refresh_btn).isEnabled = true
            binding.root.findViewById<Button>(R.id.refresh_btn).isClickable = true

            binding.root.findViewById<TextView>(R.id.callback_response).setText(parsedlData.toString())
        }

        // Handler del botón login
        binding.root.findViewById<Button>(R.id.button_login).setOnClickListener {
            ssosdk.loginSSO(SSO_url, applicationContext)
        }

        // Handler del botón logout
        binding.root.findViewById<Button>(R.id.button_logout).setOnClickListener {
            ssosdk.logoutSSO(SSO_url_logout, applicationContext)
        }

        // Handler del botón getuserInfo
        binding.root.findViewById<Button>(R.id.userInfo_btn).setOnClickListener {
            val authToken: String? = this.parsedlData!!.get("access_token")

            if (authToken != null) {
                ssosdk.getUserInfo(authToken, object : SSOSDK.UserInfoCallback {
                    override fun processFinish(success: Boolean, data: UserInfo?) {
                        if(success) {
                            val responseStr = data.toString()
                            binding.root.findViewById<TextView>(R.id.callback_response).setText(responseStr)
                        } else {
                            println("=====> No se obtuvieron datos de usuario!!!")
                        }
                    }
                })
            }
        }

        // Handler del botón VerifyToken
        binding.root.findViewById<Button>(R.id.verify_btn).setOnClickListener {
            val authToken: String? = this.parsedlData!!.get("access_token")

            if (authToken != null) {
                ssosdk.verifyToken(authToken, object : SSOSDK.IntrospectionCallback {
                    override fun processFinish(success: Boolean, data: Introspection?) {
                        if(success) {
                            val responseStr = data.toString()
                            binding.root.findViewById<TextView>(R.id.callback_response).setText(responseStr)
                        } else {
                            println("=====> Token invalido!!!")
                        }
                    }
                })
            }
        }

        // Handler del botón refreshToken
        binding.root.findViewById<Button>(R.id.refresh_btn).setOnClickListener {
            val refreshToken: String? = this.parsedlData!!.get("refresh_token")

            if (refreshToken != null) {
                ssosdk.refreshToken(refreshToken, object : SSOSDK.RefreshTokenCallback {
                    override fun processFinish(success: Boolean, data: RefresToken?) {
                        if(success) {
                            val responseStr = data.toString()
                            binding.root.findViewById<TextView>(R.id.callback_response).setText(responseStr)
                        } else {
                            println("=====> No fue posible actualizar token!!!")
                        }
                    }
                })
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}