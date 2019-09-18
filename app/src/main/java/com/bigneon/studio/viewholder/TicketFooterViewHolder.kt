package com.bigneon.studio.viewholder

import android.view.View
import android.widget.ProgressBar
import com.bigneon.studio.R

/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 03.07.2019..
 ****************************************************/
class TicketFooterViewHolder internal constructor(itemView: View) : TicketBaseViewHolder(itemView) {
    private var listItemTicketLoaderProgressBar: ProgressBar? = null

    init {
        listItemTicketLoaderProgressBar = itemView.findViewById(R.id.list_item_ticket_loader_progress_bar)
    }

    override fun bind(position: Int) {

    }
}