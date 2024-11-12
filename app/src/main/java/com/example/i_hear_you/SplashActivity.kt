package com.example.i_hear_you

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageView
import android.animation.ObjectAnimator
import android.view.animation.LinearInterpolator
import android.graphics.Color

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        window.decorView.setBackgroundColor(Color.BLACK)

        val logo: ImageView = findViewById(R.id.logoImageView)
        logo.setImageResource(R.drawable.ihearyoulogo)


        ObjectAnimator.ofFloat(logo, "rotation", 0f, 360f).apply {
            duration = 2000
            repeatCount = ObjectAnimator.INFINITE
            interpolator = LinearInterpolator()
            start()
        }


        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 2000)
    }
}
