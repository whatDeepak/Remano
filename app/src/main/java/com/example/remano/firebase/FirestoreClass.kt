package com.example.remano.firebase

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialException
import com.example.remano.activities.BaseActivity
import com.example.remano.activities.IntroActivity
import com.example.remano.activities.MainActivity
import com.example.remano.activities.SignInActivity
import com.example.remano.activities.SignUpActivity
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

                    // Check if the user already exists in the database
                    checkIfUserExistsInDatabase(activity, email, name, username)
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


    fun checkIfUserExistsInDatabase(activity: Activity, email: String?, name: String?, username: String?) {
        // Check if the user already exists based on email
        db.collection(Constants.USERS)
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // User doesn't exist, proceed with registration
                    val userInfo =
                        getCurrentUserID()?.let { User(it, name ?: "", email ?: "", username?:"") }
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

    companion object {
        private const val RC_SIGN_IN = 9001
        private const val TAG = "GoogleSignIn"
    }
}
