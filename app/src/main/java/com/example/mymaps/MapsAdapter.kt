package com.example.mymaps

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mymaps.models.UserMap

private const val TAG="MapsAdapter"
class MapsAdapter(val context: Context, val userMaps: List<UserMap>, private val onClickListener:OnClickListener) : RecyclerView.Adapter<MapsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view= LayoutInflater.from(context).inflate(R.layout.usermap_item,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val userMap=userMaps[position]
        val textViewTitle=holder.itemView.findViewById<TextView>(R.id.tvUserMapItem)
        textViewTitle.text=userMap.title
        holder.itemView.setOnClickListener{
            Log.i(TAG,"on click maps adapter $position")
            onClickListener.onItemClick(position)
        }
    }

    override fun getItemCount(): Int =userMaps.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val recMaps=itemView.findViewById<RecyclerView>(R.id.rvMap)
    }

    interface OnClickListener{
        fun onItemClick(position:Int)
    }
}
