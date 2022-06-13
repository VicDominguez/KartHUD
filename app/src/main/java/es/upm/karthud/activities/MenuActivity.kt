/**
 * Activity que implementa el menú de inicio
 * @author: Victor Manuel Dominguez Rivas y Juan Luis Moreno Sancho
 */

package es.upm.karthud.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import es.upm.karthud.R
import es.upm.karthud.Utils
import es.upm.karthud.databinding.ActivityMenuBinding

class MenuActivity : AppCompatActivity()
{
    private lateinit var binding: ActivityMenuBinding

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)

        setContentView(binding.root)

        //texto de bienvenida
        binding.welcomeUser.text = getString(R.string.welcome_user,
            Utils.remoteAuthInstance.currentUser?.displayName ?: "")

        //botones
        binding.selectTrackButton.setOnClickListener{
            it.context.startActivity(Intent(it.context, SelectCircuitActivity::class.java))
        }

        binding.viewButton.setOnClickListener{
            it.context.startActivity(Intent(it.context, HistoricActivity::class.java))
        }

        //procedimiento de logout y redireccion al activity de inicio
        binding.changeUserButton.setOnClickListener{ view ->
                AuthUI.getInstance()
                    .signOut(view.context)
                    .addOnCompleteListener {
                        startActivity(Intent(view.context, StartActivity::class.java))
                        finish()
                    }
            }

        //para cerrar la app
        binding.closeMenuButton.setOnClickListener { finish() }
    }
}