package com.bigneon.studio.viewholder

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.bigneon.studio.R
import com.bigneon.studio.rest.model.EventModel
import com.squareup.picasso.Picasso

/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 22.03.2019..
 ****************************************************/
class EventViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.list_item_event, parent, false)) {
    private var nameTextView: TextView? = null
    private var imageImageView: ImageView? = null
    private var eventRightArrow: ImageView? = null
    private var eventLoadingProgressBarPercent: TextView? = null
    private var eventLoadingProgressBar: ProgressBar? = null

    init {
        nameTextView = itemView.findViewById(R.id.event_name)
        imageImageView = itemView.findViewById(R.id.event_image)
        eventRightArrow = itemView.findViewById(R.id.event_right_arrow)
        eventLoadingProgressBarPercent = itemView.findViewById(R.id.event_loading_progress_bar_percent)
        eventLoadingProgressBar = itemView.findViewById(R.id.event_loading_progress_bar)
    }

    fun bind(event: EventModel) {
        nameTextView?.text = event.name

        // load the image with Picasso
        Picasso
            .get() // give it the context
            .load(event.promoImageURL) // load the image
            .into(imageImageView) // select the ImageView to load it into

        when (event.progress) {
            0 -> {
                eventRightArrow?.visibility = View.VISIBLE
                eventLoadingProgressBarPercent?.visibility = View.GONE
                eventLoadingProgressBar?.visibility = View.GONE
            }
            in 1..99 -> {
                eventRightArrow?.visibility = View.GONE
                eventLoadingProgressBarPercent?.visibility = View.VISIBLE
                eventLoadingProgressBar?.visibility = View.VISIBLE

                eventLoadingProgressBarPercent?.text = event.progress.toString()
                eventLoadingProgressBar?.progress = event.progress
            }
            100 -> {
                eventRightArrow?.visibility = View.VISIBLE
                eventLoadingProgressBarPercent?.visibility = View.GONE
                eventLoadingProgressBar?.visibility = View.GONE
            }
        }
    }
}