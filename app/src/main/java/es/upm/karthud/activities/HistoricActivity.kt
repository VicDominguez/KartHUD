package es.upm.karthud.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import es.upm.karthud.RecyclerAdapter
import es.upm.karthud.databinding.ActivityHistoricBinding
import es.upm.karthud.persistence.IKartHUDDao
import es.upm.karthud.persistence.SessionWithLaps
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HistoricActivity : AppCompatActivity()
{
    private lateinit var binding: ActivityHistoricBinding
    private lateinit var dao : IKartHUDDao
    private lateinit var data : MutableList<SessionWithLaps>

    private fun setupRecyclerView()
    {
        data = ArrayList()
        binding.rvSessionList.setHasFixedSize(true)
        binding.rvSessionList.layoutManager = LinearLayoutManager(this)
        binding.rvSessionList.adapter = RecyclerAdapter(data)
    }


    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoricBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.dao = InitApp.localDb.dao()

        setupRecyclerView()

        CoroutineScope(Dispatchers.IO).launch {
            data = dao.getSessionWithLaps(InitApp.remoteAuthInstance.uid ?: "")
            runOnUiThread {
                binding.rvSessionList.swapAdapter(RecyclerAdapter(data),true)
            }
        }


    }
}