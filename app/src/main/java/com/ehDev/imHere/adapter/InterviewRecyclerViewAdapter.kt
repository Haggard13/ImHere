package com.ehDev.imHere.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ehDev.imHere.R
import com.ehDev.imHere.db.entity.InterviewEntity
import kotlinx.android.synthetic.main.interview_item_view.view.*

class InterviewRecyclerViewAdapter(

    private val interviews: List<InterviewEntity>,
    private val callback: () -> Unit

) : RecyclerView.Adapter<InterviewRecyclerViewAdapter.InterviewViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InterviewViewHolder {

        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.interview_item_view, parent, false)
        return InterviewViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: InterviewViewHolder, position: Int) {

        holder.bind(interviews[position])
    }

    override fun getItemCount(): Int = interviews.size

    inner class InterviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val interviewerTV = itemView.interviewer_tv
        private val intervieweeTV = itemView.interviewee_tv
        private val interviewTimeTV = itemView.interview_time_tv
        private val interviewReferenceTV = itemView.interview_reference_tv

        fun bind(interviewItem: InterviewEntity) {

            interviewerTV.text = interviewItem.interviewer
            intervieweeTV.text = interviewItem.interviewee
            interviewTimeTV.text = interviewItem.time
            interviewReferenceTV.text = interviewItem.interviewReference
            itemView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) callback.invoke()
            }
        }
    }
}