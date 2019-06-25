package com.bigneon.doorperson.rest.response

import com.bigneon.doorperson.rest.model.PageModel
import com.bigneon.doorperson.rest.model.TicketModel

/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 21.03.2019..
 ****************************************************/
class TicketsResponse {
    internal var data: ArrayList<TicketModel>? = null
    internal var paging: PageModel? = null
}