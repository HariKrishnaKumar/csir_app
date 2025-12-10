package com.example.glasscalendar

import android.content.Intent
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.glasscalendar.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            binding.bgImage.setRenderEffect(
                RenderEffect.createBlurEffect(12f, 12f, Shader.TileMode.CLAMP)
            )
        }

        binding.buttonLogin.setOnClickListener {
            val email = binding.inputEmail.text.toString().trim()
            val password = binding.inputPassword.text.toString().trim()

            if (email == "1" && password == "1") {
                startActivity(Intent(this, HomeActivity::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                finish()
            } else {
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
            }
        }

        binding.linkPassword.setOnClickListener {
            Toast.makeText(this, "Password reset (TODO)", Toast.LENGTH_SHORT).show()
        }

        binding.linkRegister.setOnClickListener {
            Toast.makeText(this, "Register (TODO)", Toast.LENGTH_SHORT).show()
        }
    }
}
