package com.bigneon.doorperson.rest.response

import com.bigneon.doorperson.rest.model.EventModel
import com.bigneon.doorperson.rest.model.TicketModel
import com.bigneon.doorperson.rest.model.UserModel

/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 02.04.2019..
 ****************************************************/
class TicketResponse {
    internal var event: EventModel? = null

    internal var user: UserModel? = null

    internal var ticket: TicketModel? = null

}