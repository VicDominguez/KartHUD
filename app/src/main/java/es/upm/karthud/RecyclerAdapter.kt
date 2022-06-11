package es.upm.karthud

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

import es.upm.karthud.persistence.SessionWithLaps

class RecyclerAdapter(private var sessions: MutableList<SessionWithLaps>): RecyclerView.Adapter<RecyclerAdapter.ViewHolder>()
{
    class ViewHolder(v: View): RecyclerView.ViewHolder(v)
    {
        val sessionTextView : TextView = v.findViewById(R.id.session_data) as TextView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(layoutInflater.inflate(R.layout.item_session_list, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        holder.sessionTextView.text = sessions[position].toString()
    }

    override fun getItemCount(): Int = sessions.size
}