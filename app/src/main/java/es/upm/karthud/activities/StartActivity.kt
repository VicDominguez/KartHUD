package es.upm.karthud.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import es.upm.karthud.R

class StartActivity : AppCompatActivity()
{
    private lateinit var firebaseAuth: FirebaseAuth
    private val RC_SING_IN = 1
    private lateinit var authListener : FirebaseAuth.AuthStateListener

    private val providers = arrayListOf(
        AuthUI.IdpConfig.EmailBuilder().build(),
        AuthUI.IdpConfig.GoogleBuilder().build())

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        authListener = FirebaseAuth.AuthStateListener {
            val user = firebaseAuth.currentUser
            if (user != null)
            {
                val intent = Intent(this.applicationContext,MenuActivity::class.java)
                startActivity(intent)
                finish()
            }
            else
            {
                //TODO rehacer esto
                startActivityForResult(AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setIsSmartLockEnabled(false)
                    .setAvailableProviders(providers)
                    .setLogo(R.mipmap.ic_launcher)
                    .build(),
                RC_SING_IN)
            }
        }
    }

    override fun onResume()
    {
        super.onResume()
        firebaseAuth.addAuthStateListener(authListener)
    }

    override fun onPause()
    {
        super.onPause()
        firebaseAuth.removeAuthStateListener(authListener)
    }

}