package com.example.i_hear_you

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.RecognitionListener
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import com.google.android.material.imageview.ShapeableImageView
import android.widget.ImageView
import java.util.*

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var textView: TextView
    private lateinit var tts: TextToSpeech
    private lateinit var startButton: Button
    private lateinit var resetButton: Button
    private lateinit var logoImageView: ShapeableImageView
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView = findViewById(R.id.textView)
        startButton = findViewById(R.id.startButton)
        resetButton = findViewById(R.id.resetButton)
        logoImageView = findViewById(R.id.logoImageView)




        tts = TextToSpeech(this, this)


        checkPermission()


        initSpeechRecognizer()


        startButton.setOnClickListener {
            startListening()
        }


        resetButton.setOnClickListener {
            resetScreen()
        }
        val heartbeatAnimation = ScaleAnimation(
            0.9f, 1.1f,
            0.9f, 1.1f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 600
            repeatCount = Animation.INFINITE
            repeatMode = Animation.REVERSE
        }

        logoImageView.startAnimation(heartbeatAnimation)
    }

    private fun initSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Toast.makeText(this@MainActivity, "Listening...", Toast.LENGTH_SHORT).show()
            }

            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}

            override fun onError(error: Int) {
                Log.e(TAG, "Speech recognition error: $error")
                handleError(error)
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (matches != null && matches.isNotEmpty()) {
                    handleSpeechResult(matches[0])
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    private fun handleError(errorCode: Int) {
        when (errorCode) {
            SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                Toast.makeText(this, "No speech detected, try again.", Toast.LENGTH_SHORT).show()
            }
            SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> {
                Toast.makeText(this, "Network error, check your connection.", Toast.LENGTH_SHORT).show()
            }
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> {
                Toast.makeText(this, "Recognizer busy, please wait.", Toast.LENGTH_SHORT).show()
            }
            else -> {

            }
        }
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
        }
    }

    private fun startListening() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }
            speechRecognizer.startListening(intent)
        } else {
            Toast.makeText(this, "Permission required to listen.", Toast.LENGTH_SHORT).show()
            checkPermission()
        }
    }

    private fun handleSpeechResult(result: String) {
        val normalizedResult = result.lowercase(Locale.getDefault())
        textView.text = result

        when {
            "blue" in normalizedResult -> changeScreenColor("blue")
            "red" in normalizedResult -> changeScreenColor("red")
            else -> Toast.makeText(this, "Unrecognized command", Toast.LENGTH_SHORT).show()
        }
    }

    private fun changeScreenColor(color: String) {
        when (color) {
            "blue" -> {
                window.decorView.setBackgroundColor(Color.BLUE)
                textView.text = "Blue Screen"
                textView.setTextColor(Color.WHITE)
                textView.textSize = 32f
                speakOut("Here is the blue screen")
            }
            "red" -> {
                window.decorView.setBackgroundColor(Color.RED)
                textView.text = "Red Screen"
                textView.setTextColor(Color.WHITE)
                textView.textSize = 32f
                speakOut("Here is the red screen")
            }
        }
        resetButton.visibility = Button.VISIBLE
        startButton.isEnabled = false
        resetButton.requestFocus()
    }

    private fun resetScreen() {
        speechRecognizer.stopListening()
        Handler().postDelayed({
            window.decorView.setBackgroundColor(Color.WHITE)
            textView.text = "Say 'Blue' or 'Red'"
            textView.setTextColor(Color.BLACK)
            textView.textSize = 24f

            resetButton.visibility = Button.GONE
            startButton.isEnabled = true
        }, 500)
    }

    private fun speakOut(text: String) {
        if (tts.isSpeaking) {
            tts.stop()
        }
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.US
        } else {
            Log.e(TAG, "TextToSpeech initialization failed")
        }
    }

    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        if (::speechRecognizer.isInitialized) {
            speechRecognizer.destroy()
        }
        super.onDestroy()
    }
}
