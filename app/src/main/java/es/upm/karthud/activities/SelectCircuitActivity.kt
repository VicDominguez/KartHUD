package es.upm.karthud.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import es.upm.karthud.databinding.ActivitySelectCircuitBinding
import es.upm.karthud.track.Checkpoint
import es.upm.karthud.track.Circuit
import es.upm.karthud.track.Coord


class SelectCircuitActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener
{
    private lateinit var binding: ActivitySelectCircuitBinding
    private val arrayAdapter: ArrayAdapter<Circuit> by lazy { ArrayAdapter(applicationContext, android.R.layout.simple_spinner_dropdown_item)}

    private var circuit: Circuit? = null
    
    private fun obtainTracks()
    {
        val query: Query = InitApp.remoteDbReference.child("tracks")
        var name: String
        var values: Map<String,Any>
        var beacon1Map: Map<String, String>
        var beacon2Map: Map<String, String>
        var beacon1Latitude : Double
        var beacon1Longitude : Double
        var beacon2Latitude : Double
        var beacon2Longitude : Double
        var newCircuit : Circuit

        query.addListenerForSingleValueEvent(object : ValueEventListener
        {
            override fun onDataChange(dataSnapshot: DataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    for (track in dataSnapshot.children)
                    {
                        name = track?.key ?: ""
                        values = track.value as HashMap<String, Any>
                        beacon1Map = values["beacon1"] as HashMap<String, String>
                        beacon2Map = values["beacon2"] as HashMap<String, String>
                        beacon1Latitude = beacon1Map["latitude"]?.toDouble() ?: 0.0
                        beacon1Longitude = beacon1Map["longitude"]?.toDouble() ?: 0.0
                        beacon2Latitude = beacon2Map["latitude"]?.toDouble() ?: 0.0
                        beacon2Longitude = beacon2Map["longitude"]?.toDouble() ?: 0.0
                        newCircuit = Circuit(name, Checkpoint(
                            Coord(beacon1Latitude, beacon1Longitude),
                            Coord(beacon2Latitude, beacon2Longitude)))

                        arrayAdapter.add(newCircuit)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("SelectCircuitFailed", error.message)}
        })
    }
    
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectCircuitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.startButton.setOnClickListener{
            if(circuit == null)
                Toast.makeText(this, "Debes seleccionar un circuito", Toast.LENGTH_SHORT).show()
            else
            {
                val intent = Intent(it.context, HUDActivity::class.java)
                intent.putExtra("circuit",circuit)
                it.context.startActivity(intent)
            }

        }

        binding.goBackButton.setOnClickListener {finish()}

        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinner.adapter = arrayAdapter
        binding.spinner.onItemSelectedListener = this
        
        obtainTracks()
    }

    override fun onStart() {
        super.onStart()

        Log.d("Select","onStart")
    }

    override fun onItemSelected(parent: AdapterView<*>?, v: View?, position: Int, id: Long) {
        circuit = arrayAdapter.getItem(position)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        circuit = null
    }


}