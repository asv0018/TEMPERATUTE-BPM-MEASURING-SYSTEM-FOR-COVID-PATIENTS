package com.iotree.iot_app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView

data class HistoryData(val temp:Long, val bpm:Long, val date:String, val time:String)

class HistoryAdapter(private val listdata: MutableList<HistoryData>) :
    RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val listItem: View = layoutInflater.inflate(R.layout.item_resource_file, parent, false)
        return ViewHolder(listItem)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val myListData: HistoryData = listdata[position]
        holder.bpmview.text = "${(listdata[position].bpm).toString()} bpm"
        holder.temperatureview.text = "${(listdata[position].temp).toString()} \u2109"
        holder.datetimeview.text = "As tested on ${listdata[position].date.toString()} at ${listdata[position].time.toString()} IST."
        holder.constraintlayout.setOnClickListener { view ->

        }
    }

    override fun getItemCount(): Int {
        return listdata.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var bpmview: TextView = itemView.findViewById(R.id.item_bpm_view) as TextView
        var temperatureview: TextView = itemView.findViewById(R.id.item_temp_view) as TextView
        var datetimeview: TextView = itemView.findViewById(R.id.item_info_view) as TextView
        var constraintlayout : ConstraintLayout = itemView.findViewById(R.id.item_constrint_layout) as ConstraintLayout
    }

}
