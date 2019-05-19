package com.bigneon.doorperson.viewholder

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bigneon.doorperson.R
import com.bigneon.doorperson.rest.model.EventModel
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

    init {
        nameTextView = itemView.findViewById(R.id.event_name)
        imageImageView = itemView.findViewById(R.id.event_image)
    }

    fun bind(event: EventModel) {
        nameTextView?.text = event.name

        // load the image with Picasso
        Picasso
            .get() // give it the context
            .load(event.promoImageURL) // load the image
            .into(imageImageView) // select the ImageView to load it into
    }
}