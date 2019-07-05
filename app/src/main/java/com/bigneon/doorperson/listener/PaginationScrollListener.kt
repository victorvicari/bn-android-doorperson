package com.bigneon.doorperson.listener

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.bigneon.doorperson.config.AppConstants
import com.bigneon.doorperson.util.AppUtils


/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 03.07.2019..
 ****************************************************/
abstract class PaginationScrollListener(private val layoutManager: LinearLayoutManager) :
    RecyclerView.OnScrollListener() {
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        val visibleItemCount = layoutManager.childCount
        val totalItemCount = layoutManager.itemCount
        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

        AppUtils.ticketListItemPosition = firstVisibleItemPosition

        AppUtils.ticketListItemOffset =
            if (recyclerView.layoutManager?.findViewByPosition(AppUtils.ticketListItemPosition) != null) recyclerView.layoutManager?.findViewByPosition(
                AppUtils.ticketListItemPosition
            )!!.top else 0

        if (!isLoading() && !isLastPage()) {
            if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                && firstVisibleItemPosition >= 0
                && totalItemCount >= AppConstants.PAGE_LIMIT
            ) {
                loadMoreItems()
            }
        }
    }

    protected abstract fun loadMoreItems()

    abstract fun isLastPage(): Boolean

    abstract fun isLoading(): Boolean

}