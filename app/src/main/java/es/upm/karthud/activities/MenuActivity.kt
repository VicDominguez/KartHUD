package es.upm.karthud.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import es.upm.karthud.databinding.ActivityMenuBinding


class MenuActivity : AppCompatActivity()
{
    private lateinit var binding: ActivityMenuBinding

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.startButton.setOnClickListener{
            val intent = Intent(it.context, HUDActivity::class.java)
            it.context.startActivity(intent)
        }

        binding.viewButton.setOnClickListener{
            val intent = Intent(it.context, HistoricActivity::class.java)
            it.context.startActivity(intent)
        }

        binding.closeMenuButton.setOnClickListener { finish() }
    }
}