package com.ehDev.imHere.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ehDev.imHere.R
import com.ehDev.imHere.db.entity.ScheduleEntity
import kotlinx.android.synthetic.main.schedule_item_view.view.*

private const val MINUTES = 3

class ScheduleRecyclerViewAdapter(

    private val schedule: List<ScheduleEntity>

) : RecyclerView.Adapter<ScheduleRecyclerViewAdapter.ScheduleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {

        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.schedule_item_view, parent, false)
        return ScheduleViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {

        holder.bind(schedule[position])
    }

    override fun getItemCount(): Int = schedule.size

    inner class ScheduleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val classNumberTV = itemView.class_number_tv
        private val classNameTV = itemView.class_name_tv
        private val classTypeTV = itemView.class_type_tv
        private val auditoryTV = itemView.auditory_tv
        private val lecturerTV = itemView.lecturer_tv
        private val pairTime = itemView.time_tv

        fun bind(scheduleItem: ScheduleEntity) {

            classNumberTV.text = scheduleItem.number.toString()
            classNameTV.text = scheduleItem.name
            classTypeTV.text = scheduleItem.type
            auditoryTV.text = scheduleItem.auditorium
            lecturerTV.text = scheduleItem.lecturer
            pairTime.text = scheduleItem.date.split(',')[2] + ":" + scheduleItem.date.split(',')[MINUTES]
        }
    }
}