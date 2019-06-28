package com.bigneon.doorperson.adapter

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup


/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 28.06.2019..
 ****************************************************/
abstract class BaseAdapter<T> : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items: MutableList<T> = ArrayList()
    protected var onItemClickListener2: OnItemClickListener? = null
    protected var onReloadClickListener2: OnReloadClickListener? = null
    protected var isFooterAdded = false

    val isEmpty: Boolean
        get() = itemCount == 0

    interface OnItemClickListener {
        fun onItemClick(position: Int, view: View)
    }

    interface OnReloadClickListener {
        fun onReloadClick()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        var viewHolder: RecyclerView.ViewHolder? = null

        when (viewType) {
            HEADER -> viewHolder = createHeaderViewHolder(parent)
            ITEM -> viewHolder = createItemViewHolder(parent)
            FOOTER -> viewHolder = createFooterViewHolder(parent)
            else -> {
            }
        }

        return viewHolder
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            HEADER -> bindHeaderViewHolder(viewHolder)
            ITEM -> bindItemViewHolder(viewHolder, position)
            FOOTER -> bindFooterViewHolder(viewHolder)
            else -> {
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    // region Abstract Methods
    protected abstract fun createHeaderViewHolder(parent: ViewGroup): RecyclerView.ViewHolder?

    protected abstract fun createItemViewHolder(parent: ViewGroup): RecyclerView.ViewHolder

    protected abstract fun createFooterViewHolder(parent: ViewGroup): RecyclerView.ViewHolder

    protected abstract fun bindHeaderViewHolder(viewHolder: RecyclerView.ViewHolder)

    protected abstract fun bindItemViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int)

    protected abstract fun bindFooterViewHolder(viewHolder: RecyclerView.ViewHolder)

    protected abstract fun displayLoadMoreFooter()

    protected abstract fun displayErrorFooter()

    abstract fun addFooter()

    fun getItem(position: Int): T? {
        return items[position]
    }

    fun add(item: T) {
        items.add(item)
        notifyItemInserted(items.size - 1)
    }

    fun addAll(items: List<T>) {
        for (item in items) {
            add(item)
        }
    }

    private fun remove(item: T?) {
        val position = items.indexOf(item)
        if (position > -1) {
            items.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun clear() {
        isFooterAdded = false
        while (itemCount > 0) {
            remove(getItem(0))
        }
    }

    fun isLastPosition(position: Int): Boolean {
        return position == items.size - 1
    }

    fun removeFooter() {
        isFooterAdded = false

        val position = items.size - 1
        val item = getItem(position)

        if (item != null) {
            items.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun updateFooter(footerType: FooterType) {
        when (footerType) {
            FooterType.LOAD_MORE -> displayLoadMoreFooter()
            FooterType.ERROR -> displayErrorFooter()
            else -> {
            }
        }
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener) {
        this.onItemClickListener2 = onItemClickListener
    }

    fun setOnReloadClickListener(onReloadClickListener: OnReloadClickListener) {
        this.onReloadClickListener2 = onReloadClickListener
    }

    enum class FooterType {
        LOAD_MORE,
        ERROR
    }

    companion object {
        protected const val HEADER = 0
        protected const val ITEM = 1
        protected const val FOOTER = 2
    }
}