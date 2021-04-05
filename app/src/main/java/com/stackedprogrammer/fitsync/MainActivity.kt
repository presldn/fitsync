package com.stackedprogrammer.fitsync

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.facebook.drawee.view.SimpleDraweeView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

class MainActivity : AppCompatActivity() {

    private val tag = "MainActivity"

    private lateinit var mGoogleSignInClient: GoogleSignInClient

    private val reqCode: Int = 123

    private lateinit var loginButton: Button
    private lateinit var profileImage: SimpleDraweeView
    private lateinit var emailAddress: TextView
    private lateinit var fullName: TextView

    private val auth by lazy {
        FirebaseAuth.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        loginButton = findViewById(R.id.login)
        profileImage = findViewById(R.id.profile_image)
        emailAddress = findViewById(R.id.profile_email_address)
        fullName = findViewById(R.id.profile_full_name)


        loginButton.setOnClickListener {
            if (GoogleSignIn.getLastSignedInAccount(this) != null) {
                logout()
            } else {
                signInGoogle()
            }
        }


    }

    private fun logout() {
        auth.signOut()
        Toast.makeText(this, "Logged out.", Toast.LENGTH_LONG).show()
        updateUI(auth.currentUser)

    }

    private fun signInGoogle() {
        val signInIntent: Intent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, reqCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == reqCode) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleResult(task)
        }
    }

    private fun handleResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)!!
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
        }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            loginButton.text = getString(R.string.logout)
            setImageProfile(user.photoUrl)

            fullName.text = user.displayName

            emailAddress.visibility = View.VISIBLE
            emailAddress.text = user.email

        } else {
            emailAddress.visibility = View.GONE
            loginButton.text = getString(R.string.login)
        }
    }

    private fun setImageProfile(photoUrl: Uri?) {
        if (photoUrl != null) {
            profileImage.setImageURI(photoUrl.toString())
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(tag, "signInWithCredential:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(tag, "signInWithCredential:failure", task.exception)
                    updateUI(null)
                }
            }
    }

    override fun onStart() {
        super.onStart()

        val user = auth.currentUser
        updateUI(user)

        if (GoogleSignIn.getLastSignedInAccount(this) != null) {
            Toast.makeText(this, "Already logged in.", Toast.LENGTH_SHORT).show()
        }
    }

}