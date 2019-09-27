package com.bigneon.studio.rest.model

import com.google.gson.annotations.SerializedName

/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 21.03.2019..
 ****************************************************/
class EventModel {
    internal var id: String? = null

    internal var name: String? = null

    @SerializedName("promo_image_url")
    internal var promoImageURL: String? = null

    @SerializedName("door_time")
    internal var doorTime: String? = null

    internal var venue: VenueModel? = null


    var progress: Int = 0
}