package com.example.ssocustomapp

import com.doters.ssosdk.models.Introspection
import com.doters.ssosdk.models.RefresToken
import com.doters.ssosdk.models.UserInfoData
import com.doters.ssosdk.models.LoginData
import com.doters.ssosdk.SSOSDK

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
    private val scheme: String = "dosso://"
    private val url: String = "https://auth-test.doters.io"
    private val APIurl: String = "https://auth-api-gw-test.doters.io"
    private val clientId: String = "viva-web"
    private val clientSecret: String = "GlMTbnwjRA"
    private val language: String = "es-MX"
    private val state: String = "A245FG"

    private var ssosdk = SSOSDK(scheme, url, APIurl, language, clientId, clientSecret, state)

    var parsedlData: LoginData = LoginData()

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
        if(dlData != null) parsedlData = ssosdk.parseURI(dlData)!!

        binding.root.findViewById<TextView>(R.id.callback_response).movementMethod = ScrollingMovementMethod()

        if(parsedlData != null) {
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
            ssosdk.signIn(applicationContext)
        }

        // Handler del botón logout
        binding.root.findViewById<Button>(R.id.button_logout).setOnClickListener {
            ssosdk.logOut(applicationContext)
        }

        // Handler del botón getuserInfo
        binding.root.findViewById<Button>(R.id.userInfo_btn).setOnClickListener {
            val accessToken: String? = this.parsedlData.access_token

            if (accessToken != null) {
                ssosdk.UserInfo(accessToken, object : SSOSDK.UserInfoCallback {
                    override fun processFinish(success: Boolean, data: UserInfoData?) {
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
            val accessToken: String? = this.parsedlData.access_token

            if (accessToken != null) {
                ssosdk.TokenIntrospection(accessToken, object : SSOSDK.IntrospectionCallback {
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
            val refreshToken: String? = this.parsedlData.refresh_token

            if (refreshToken != null) {
                ssosdk.RefreshToken(refreshToken, object : SSOSDK.RefreshTokenCallback {
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