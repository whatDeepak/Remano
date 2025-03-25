package com.example.remano.firebase

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Canvas
import android.graphics.Paint
import android.util.Log
import android.widget.Toast
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialException
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.NavHostFragment
import com.example.remano.activities.BaseActivity
import com.example.remano.activities.IntroActivity
import com.example.remano.activities.MainActivity
import com.example.remano.activities.SignInActivity
import com.example.remano.activities.SignUpActivity
import com.example.remano.fragments.HomeFragment
import com.example.remano.model.User
import com.example.remano.utils.Constants
import com.google.firebase.auth.*
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.remano.R
import com.google.firebase.storage.storage
import java.io.ByteArrayOutputStream
import android.graphics.Typeface

class FirestoreClass: BaseActivity() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    val db = Firebase.firestore

    fun signInWithGoogle(activity: Activity) {
        val webClientId = activity.getString(com.example.remano.R.string.default_web_client_id)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(webClientId)
            .setFilterByAuthorizedAccounts(true)//first pass true
            .setFilterByAuthorizedAccounts(false)//then false
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val credentialManager = CredentialManager.create(activity)

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = credentialManager.getCredential(activity, request) // Returns GetCredentialResponse
                val credential = response.credential // Extract the actual credential
                handleSignIn(activity, credential)
            } catch (e: Exception) {
                Log.e("GoogleSignIn", "Sign-in failed: ${e.localizedMessage}")
                e.printStackTrace()
            }
        }
    }

    private fun handleSignIn(activity: Activity, credential: Credential) {
        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            firebaseAuthWithGoogle(activity, googleIdTokenCredential.idToken)
        } else {
            Log.w("GoogleSignIn", "Credential is not of type Google ID!")
        }
    }

    private fun firebaseAuthWithGoogle(activity: Activity, idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("GoogleSignIn", "signInWithCredential:success")

                    val user = auth.currentUser
                    // Get user details from Google sign-in
                    val name = user?.displayName
                    val email = user?.email

                    // Set the username based on the email (you can customize this logic)
                    val username = email?.substringBefore("@") ?: ""

                    val image = user?.photoUrl?.toString() ?: ""

                    // Check if the user already exists in the database
                    checkIfUserExistsInDatabase(activity, email, name, username, image)
                } else {
                    Log.w("GoogleSignIn", "signInWithCredential:failure", task.exception)
                }
            }
    }

    fun signInUser(activity: SignInActivity, email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val currentUserID = auth.currentUser?.uid
                    if (currentUserID != null) {
                        getUserDetails(activity, currentUserID)
                    }
                } else {
                    Log.w("Sign in", "signInWithEmail:failure", task.exception)
                    activity.hideProgressDialog()
                    activity.showErrorSnackBar("Authentication failed.")
                }
            }
    }

    fun signUpUser(activity: SignUpActivity, name: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser: FirebaseUser = task.result!!.user!!
                    val user = User(firebaseUser.uid, name, email)
                    registerUser(activity, user)
                } else {
                    activity.hideProgressDialog()
                    activity.showErrorSnackBar("Registration failed: ${task.exception?.message}")
                }
            }
    }

    fun sendPasswordResetEmail(activity: SignInActivity, email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                Toast.makeText(activity, "Please check your email to reset your password!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(activity, exception.message, Toast.LENGTH_SHORT).show()
            }
    }


    private fun getUserDetails(activity: SignInActivity, userId: String) {
        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)
                if (user != null) {
                    activity.signInSuccess(user)
                } else {
                    activity.hideProgressDialog()
                    activity.showErrorSnackBar("User data not found.")
                }
            }
            .addOnFailureListener {
                activity.hideProgressDialog()
                activity.showErrorSnackBar("Failed to retrieve user details.")
            }
    }


    fun checkIfUserExistsInDatabase(activity: Activity, email: String?, name: String?, username: String?, image: String) {
        // Check if the user already exists based on email
        db.collection(Constants.USERS)
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // User doesn't exist, proceed with registration
//                    val profileImage = image ?: generateDefaultProfilePicture(activity, email ?: "") { generatedImageUrl ->
//                        Log.d("Generated Image URL", generatedImageUrl)
//                    }
                    val userInfo =
                        getCurrentUserID()?.let { User(it, name ?: "", email ?: "", username?:"", image?:"") }
                    if (userInfo != null) {
                        registerUser(activity, userInfo)
                    }
                } else {
                    // User with the same email already exists, start MainActivity
                    activity.startActivity(Intent(activity, MainActivity::class.java))
                    activity.finish()
                }
            }
            .addOnFailureListener { e ->
                // Handle the failure to check user existence
                Log.e(TAG, "Error checking user existence", e)
            }
    }


    /**
     * A function to make an entry of the registered user in the firestore database.
     */
    fun registerUser(activity: Activity, userInfo: User) {
        val currentUserID = getCurrentUserID()

        // Check if the user already exists based on email
        db.collection(Constants.USERS)
            .whereEqualTo("email", userInfo.email) // Assuming "email" is the field representing the email in your User model
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // No user with the same email found, proceed with creating a new document
                    if (currentUserID != null) {
                        db.collection(Constants.USERS)
                            .document(currentUserID)
                            .set(userInfo, SetOptions.merge())
                            .addOnSuccessListener {
                                if (activity is IntroActivity) {
                                    (activity as IntroActivity).userRegisteredSuccessIntro()
                                }
                                if (activity is SignUpActivity) {
                                    (activity as SignUpActivity).userRegisteredSuccessSignUp()
                                }
                            }
                            .addOnFailureListener { e ->
                                handleRegistrationFailure(activity, e)
                            }
                    }
                } else {
                    // User with the same email already exists, update the existing document
                    val existingUserId = documents.documents[0].id
                    db.collection(Constants.USERS)
                        .document(existingUserId)
                        .set(userInfo, SetOptions.merge())
                        .addOnSuccessListener {
                            if (activity is IntroActivity) {
                                (activity as IntroActivity).userRegisteredSuccessIntro()
                            }
                            if (activity is SignUpActivity) {
                                (activity as SignUpActivity).userRegisteredSuccessSignUp()
                            }
                        }
                        .addOnFailureListener { e ->
                            handleRegistrationFailure(activity, e)
                        }
                }
            }
            .addOnFailureListener { e ->
                handleRegistrationFailure(activity, e)
            }
    }

    private fun handleRegistrationFailure(activity: Activity, e: Exception) {
        if (activity is IntroActivity) {
            (activity as IntroActivity).hideProgressDialog()
        }
        if (activity is SignUpActivity) {
            (activity as SignUpActivity).hideProgressDialog()
        }

        Log.e(
            activity.javaClass.simpleName,
            "Error writing document",
            e
        )
    }

    fun signOut(activity: Activity) {
        auth.signOut()

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val clearRequest = ClearCredentialStateRequest()
                val credentialManager = CredentialManager.create(activity)
                credentialManager.clearCredentialState(clearRequest)
            } catch (e: ClearCredentialException) {
                Log.e("GoogleSignIn", "Couldn't clear user credentials: ${e.localizedMessage}")
            }
        }
    }

    fun generateDefaultProfilePicture(activity: Activity, email: String, callback: (String) -> Unit) {
        val storageRef = Firebase.storage.reference
        val profileRef = storageRef.child("profile_pictures/${email}.png")

        val defaultProfileBitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(defaultProfileBitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Set background color to FF6868
        paint.color = Color.parseColor("#FF6868")
        canvas.drawRect(0f, 0f, 200f, 200f, paint)

        // Set text color and font
        paint.color = Color.WHITE
        paint.textSize = 60f
        paint.textAlign = Paint.Align.CENTER

        // Try to load Poppins-Bold font
        val typeface = try {
            Typeface.createFromAsset(activity.assets, "poppins_bold.ttf")
        } catch (e: Exception) {
            Typeface.DEFAULT_BOLD  // Fallback if font is not found
        }
        paint.typeface = typeface

        // Draw the first letter of the email
        val xPos = 100f  // Center X
        val yPos = 125f - (paint.descent() + paint.ascent()) / 2  // Center Y
        canvas.drawText(email.first().uppercaseChar().toString(), xPos, yPos, paint)

        val baos = ByteArrayOutputStream()
        defaultProfileBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val data = baos.toByteArray()

        profileRef.putBytes(data)
            .addOnSuccessListener {
                profileRef.downloadUrl.addOnSuccessListener { uri ->
                    callback(uri.toString())  // Call the callback with the uploaded image URL
                }.addOnFailureListener {
                    Log.e("ProfilePic", "Failed to get download URL")
                    callback("")  // Return empty string on failure
                }
            }
            .addOnFailureListener {
                Log.e("ProfilePic", "Failed to upload profile picture")
                callback("")  // Return empty string on failure
            }
    }


    /**
     * A function to SignIn using firebase and get the user details from Firestore Database.
     */
    fun loadUserData(context: Context) {
        getCurrentUserID()?.let { userId ->
            db.collection(Constants.USERS)
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    Log.e(context.javaClass.simpleName, document.toString())

                    val loggedInUser = document.toObject(User::class.java)

                    loggedInUser?.let { user ->
                        when (context) {
                            is MainActivity -> {
                                val navHostFragment = context.supportFragmentManager
                                    .findFragmentById(R.id.fragmentContainerView) as? NavHostFragment

                                val currentFragment = navHostFragment?.childFragmentManager?.fragments?.firstOrNull()
                                if (currentFragment is HomeFragment) {
                                    currentFragment.updateUserDetails(user)
                                }
                            }

//                            is ProfileActivity -> {
//                                context.updateUserDetails(user)
//                            }
//
//                            is AccountActivity -> {
//                                context.setUserDataInUI(user)
//                            }
                        }
                    } ?: Log.e(context.javaClass.simpleName, "User document is null or conversion failed")
                }
                .addOnFailureListener { e ->
                    when (context) {
                        is SignInActivity -> context.hideProgressDialog()
//                        is ProfileActivity -> context.hideProgressDialog()
//                        is AccountActivity -> context.hideProgressDialog()
                        is FragmentActivity -> Log.e("UserData", "Error fetching user data")
                    }

                    Log.e(
                        context.javaClass.simpleName,
                        "Error while getting logged-in user details",
                        e
                    )
                }
        } ?: Log.e(context.javaClass.simpleName, "getCurrentUserID() returned null")
    }

    fun getUserData(onSuccess: (User) -> Unit, onFailure: () -> Unit) {
        getCurrentUserID()?.let {
            db.collection(Constants.USERS)
                .document(it)
                .get()
                .addOnSuccessListener { document ->
                    val loggedInUser = document.toObject(User::class.java)
                    if (loggedInUser != null) {
                        onSuccess(loggedInUser)
                    } else {
                        onFailure()
                    }
                }
                .addOnFailureListener {
                    onFailure()
                }
        }
    }




    companion object {
        private const val RC_SIGN_IN = 9001
        private const val TAG = "GoogleSignIn"
    }
}
