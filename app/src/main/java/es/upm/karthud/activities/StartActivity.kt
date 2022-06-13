/**
 * Activity principal que no se muestra al usuario. Llama al menú de inicio si el usuario está logeado
 * y si no llama a FirebaseAuthUI para autentificarse
 * @author: Victor Manuel Dominguez Rivas y Juan Luis Moreno Sancho
 */

package es.upm.karthud.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import es.upm.karthud.R
import es.upm.karthud.Utils.remoteAuthInstance

class StartActivity : AppCompatActivity()
{
    private lateinit var authListener : FirebaseAuth.AuthStateListener

    //proveedores de autentificacion utilizados
    private val providers = arrayListOf(
        AuthUI.IdpConfig.EmailBuilder().build(),
        AuthUI.IdpConfig.GoogleBuilder().build())

    //para sustituir al activity for result que está depreciado
    private val activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){}

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        authListener = FirebaseAuth.AuthStateListener {
            if (remoteAuthInstance.currentUser != null)
            {
                //si tenemos usuario, arrancamos el menu prinicpal
                startActivity(Intent(this.applicationContext,MenuActivity::class.java))
                finish()
            }
            else
            {
                //si no lo tenemos, abrimos el menu de login
                val intent = AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setIsSmartLockEnabled(false)
                    .setAvailableProviders(providers)
                    .setLogo(R.mipmap.ic_launcher)
                    .build()
                activityResultLauncher.launch(intent)
            }
        }
    }

    /**
     * Si se ha reiniciado el activity, añadimos el listener
     */
    override fun onResume()
    {
        super.onResume()
        remoteAuthInstance.addAuthStateListener(authListener)
    }

    /**
     * Si se ha parado el activity, quitamos el listener
     */
    override fun onPause()
    {
        super.onPause()
        remoteAuthInstance.removeAuthStateListener(authListener)
    }
}