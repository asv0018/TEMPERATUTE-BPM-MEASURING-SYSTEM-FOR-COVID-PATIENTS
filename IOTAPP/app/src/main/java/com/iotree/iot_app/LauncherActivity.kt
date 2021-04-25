package com.iotree.iot_app

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import com.iotree.iot_app.MainActivity.Companion.myListData


class LauncherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)
        supportActionBar?.hide()

        Handler().postDelayed( {
            val mainIntent = Intent(this@LauncherActivity, MainActivity::class.java)
            this.startActivity(mainIntent)
            this.finish()
        }, 3000)

    }
}