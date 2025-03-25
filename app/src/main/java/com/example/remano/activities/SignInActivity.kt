package com.example.remano.activities

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.util.Log
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.widget.Toolbar
import com.example.remano.R
import com.example.remano.firebase.FirestoreClass
import com.example.remano.model.User
import com.google.android.material.textfield.TextInputLayout

class SignInActivity : BaseActivity() {

    private lateinit var etPassword: EditText
    private lateinit var ivPasswordToggle: ImageView
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_in)

        setupActionBar()
        setupPasswordToggle()
        applyCustomFonts()

        val btnSignIn = findViewById<Button>(R.id.btn_login_signin)
        btnSignIn.setOnClickListener { signInRegisteredUser() }

        val btnSignUpIntro = findViewById<TextView>(R.id.et_signup)
        btnSignUpIntro.setOnClickListener {
            // Launch the sign-up screen.
            startActivity(Intent(this@SignInActivity, SignUpActivity::class.java))
        }

        val btnForgot = findViewById<TextView>(R.id.forgot_signin)
        val emailForgot = findViewById<EditText>(R.id.et_signin_email)

        btnForgot.setOnClickListener {
            val email = emailForgot.text.toString().trim()

            if (email.isNotEmpty()) {
                FirestoreClass().sendPasswordResetEmail(this, email)
            } else {
                Toast.makeText(this, "Please enter your email address", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun signInRegisteredUser() {
        val etEmail = findViewById<EditText>(R.id.et_signin_email)
        val etPassword = findViewById<EditText>(R.id.et_signin_password)

        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (validateForm(email, password)) {
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().signInUser(this, email, password)
        }
    }

    fun signInSuccess(user: User) {
        hideProgressDialog()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun validateForm(email: String, password: String): Boolean {
        return if (TextUtils.isEmpty(email)) {
            showErrorSnackBar("Please enter email.")
            false
        } else if (TextUtils.isEmpty(password)) {
            showErrorSnackBar("Please enter password.")
            false
        } else {
            true
        }
    }

    private fun applyCustomFonts() {
        val poppinsRegular = Typeface.createFromAsset(assets, "poppins_regular.ttf")
        val poppinsSemiBold = Typeface.createFromAsset(assets, "poppins_semibold.ttf")

        val textViews = listOf(R.id.tv_signin_title, R.id.et_signin_email, R.id.et_signin_password)
        for (id in textViews) {
            findViewById<TextView>(id).typeface = poppinsRegular
        }

        findViewById<TextView>(R.id.forgot_signin).typeface = poppinsSemiBold
        findViewById<TextView>(R.id.et_signup).typeface = poppinsSemiBold
    }

    private fun setupActionBar() {
        val toolbarSignInActivity = findViewById<Toolbar>(R.id.toolbar_sign_in_activity)
        val tvTitle: TextView = findViewById(R.id.tv_signin_title)
        val customTypeface = Typeface.createFromAsset(assets, "poppins_medium.ttf")
        tvTitle.typeface = customTypeface
        setSupportActionBar(toolbarSignInActivity)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)

        toolbarSignInActivity.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupPasswordToggle() {
        etPassword = findViewById(R.id.et_signin_password)
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
