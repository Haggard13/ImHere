package com.ehDev.imHere.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ehDev.imHere.R
import com.ehDev.imHere.db.entity.ScheduleEntity
import kotlinx.android.synthetic.main.schedule_item_view.view.*

private const val MINUTES = 3

class ScheduleAdapter(private val schedule: List<ScheduleEntity>) : RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {

        val layout = LayoutInflater.from(parent.context).inflate(R.layout.schedule_item_view, parent, false)
        return ScheduleViewHolder(layout)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {

        holder.bind(schedule[position])
    }

    inner class ScheduleViewHolder(private val layout: View) : RecyclerView.ViewHolder(layout) {

        private val classNumberTV = layout.class_number_tv
        private val classNameTV = layout.class_name_tv
        private val classTypeTV = layout.class_type_tv
        private val auditoryTV = layout.auditory_tv
        private val lecturerTV = layout.lecturer_tv
        private val pairTime = layout.time_tv

        fun bind(scheduleItem: ScheduleEntity) {

            classNumberTV.text = scheduleItem.number.toString()
            classNameTV.text = scheduleItem.name
            classTypeTV.text = scheduleItem.type
            auditoryTV.text = scheduleItem.auditorium
            lecturerTV.text = scheduleItem.lecturer
            pairTime.text = scheduleItem.date.split(',')[2] + ":" + scheduleItem.date.split(',')[MINUTES]
        }
    }

    override fun getItemCount(): Int = schedule.size
}