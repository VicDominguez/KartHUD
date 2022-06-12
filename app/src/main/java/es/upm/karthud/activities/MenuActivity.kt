package es.upm.karthud.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import es.upm.karthud.R
import es.upm.karthud.databinding.ActivityMenuBinding


class MenuActivity : AppCompatActivity()
{
    private lateinit var binding: ActivityMenuBinding

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.welcomeUser.text = getString(R.string.welcome_user,
            InitApp.remoteAuthInstance.currentUser?.displayName ?: "")

        binding.selectTrackButton.setOnClickListener{
            val intent = Intent(it.context, SelectCircuitActivity::class.java)
            it.context.startActivity(intent)
        }

        binding.viewButton.setOnClickListener{
            val intent = Intent(it.context, HistoricActivity::class.java)
            it.context.startActivity(intent)
        }

        binding.changeUserButton.setOnClickListener{ view ->
                AuthUI.getInstance()
                    .signOut(view.context)
                    .addOnCompleteListener {
                        Toast.makeText(view.context, getString(R.string.logout_ok), Toast.LENGTH_SHORT).show()
                        val intent = Intent(view.context, StartActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
            }

        binding.closeMenuButton.setOnClickListener { finish() }
    }
}