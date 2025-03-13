package com.example.remano.activities

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.widget.Toolbar
import com.example.remano.R
import com.example.remano.firebase.FirestoreClass
import com.google.firebase.auth.FirebaseAuth

class SignUpActivity : BaseActivity() {

    private lateinit var etPassword: EditText
    private lateinit var ivPasswordToggle: ImageView
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)

        setupActionBar()
        setupPasswordToggle()
        applyCustomFonts()

        findViewById<TextView>(R.id.tv_signin_redirect).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        findViewById<Button>(R.id.btn_register_signup).setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        val etName = findViewById<EditText>(R.id.et_signup_name)
        val etEmail = findViewById<EditText>(R.id.et_signup_email)
        val etPassword = findViewById<EditText>(R.id.et_signup_password)

        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (validateForm(name, email, password)) {
            showProgressDialog(getString(R.string.please_wait))
            FirestoreClass().signUpUser(this, name, email, password)
        }
    }

    private fun validateForm(name: String, email: String, password: String): Boolean {
        return when {
            TextUtils.isEmpty(name) -> {
                showErrorSnackBar("Please enter name.")
                false
            }
            TextUtils.isEmpty(email) -> {
                showErrorSnackBar("Please enter email.")
                false
            }
            TextUtils.isEmpty(password) -> {
                showErrorSnackBar("Please enter password.")
                false
            }
            else -> true
        }
    }

    fun userRegisteredSuccessSignUp() {
        Toast.makeText(this, "You have successfully registered.", Toast.LENGTH_SHORT).show()
        hideProgressDialog()
        FirebaseAuth.getInstance().signOut()
        finish()
    }

    private fun applyCustomFonts() {
        val poppinsRegular = Typeface.createFromAsset(assets, "poppins_regular.ttf")
        val poppinsSemiBold = Typeface.createFromAsset(assets, "poppins_semibold.ttf")

        val textViews = listOf(R.id.tv_signup_title, R.id.et_signup_name, R.id.et_signup_email, R.id.et_signup_password)
        for (id in textViews) {
            findViewById<TextView>(id).typeface = poppinsRegular
        }
        findViewById<TextView>(R.id.tv_signin_redirect).typeface = poppinsSemiBold
    }

    private fun setupActionBar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar_sign_up_activity)
        val tvTitle: TextView = findViewById(R.id.tv_signup_title)
        tvTitle.typeface = Typeface.createFromAsset(assets, "poppins_medium.ttf")
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)

        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupPasswordToggle() {
        etPassword = findViewById(R.id.et_signup_password)
        ivPasswordToggle = findViewById(R.id.iv_password_toggle)

        val poppinsRegular = Typeface.createFromAsset(assets, "poppins_regular.ttf")
        etPassword.typeface = poppinsRegular

        etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        ivPasswordToggle.setImageResource(R.drawable.ic_eye_closed)

        ivPasswordToggle.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            etPassword.inputType = if (isPasswordVisible) {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            ivPasswordToggle.setImageResource(if (isPasswordVisible) R.drawable.ic_eye else R.drawable.ic_eye_closed)
            etPassword.typeface = poppinsRegular
            etPassword.setSelection(etPassword.text.length)
        }
    }
}
