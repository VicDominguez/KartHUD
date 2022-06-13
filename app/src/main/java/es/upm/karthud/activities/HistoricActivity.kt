/**
 * Activity que muestra el historial de tandas
 * @author: Victor Manuel Dominguez Rivas y Juan Luis Moreno Sancho
 */


package es.upm.karthud.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import es.upm.karthud.adapters.SessionLapsAdapter
import es.upm.karthud.databinding.ActivityHistoricBinding
import es.upm.karthud.persistence.IAppDao
import es.upm.karthud.persistence.AppDatabase
import es.upm.karthud.persistence.SessionWithLaps
import es.upm.karthud.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HistoricActivity : AppCompatActivity()
{
    private lateinit var binding: ActivityHistoricBinding
    private lateinit var dao : IAppDao
    private lateinit var data : MutableList<SessionWithLaps>

    private fun setupRecyclerView()
    {
        data = ArrayList()
        binding.rvSessionList.setHasFixedSize(true)
        binding.rvSessionList.layoutManager = LinearLayoutManager(this)
        binding.rvSessionList.adapter = SessionLapsAdapter(data)
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoricBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.dao = AppDatabase.getDatabase(applicationContext).dao()

        setupRecyclerView()

        //obtenemos los datos mediante una corrutina, y cuando los tengamos los mostramos
        CoroutineScope(Dispatchers.IO).launch {
            data = dao.getSessionWithLaps(Utils.remoteAuthInstance.uid ?: "")
            runOnUiThread {
                //mas eficiente que datasetChanged
                binding.rvSessionList.swapAdapter(SessionLapsAdapter(data),true)
            }
        }


    }
}