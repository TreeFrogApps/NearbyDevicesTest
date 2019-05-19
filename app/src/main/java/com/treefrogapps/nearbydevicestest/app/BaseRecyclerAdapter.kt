package com.treefrogapps.nearbydevicestest.app

import android.support.v7.util.DiffUtil.Callback
import android.support.v7.util.DiffUtil.calculateDiff
import android.support.v7.widget.RecyclerView
import android.view.View
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject


abstract class BaseRecyclerAdapter<T, VH : BaseRecyclerAdapter.BaseViewHolder<T>> : RecyclerView.Adapter<VH>() {

    private val publishSubject = PublishSubject.create<T>()
    private val dataList = mutableListOf<T>()

    override fun getItemCount(): Int = dataList.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(dataList[position], publishSubject)
    }

    override fun onViewRecycled(holder: VH) {
        super.onViewRecycled(holder)
        holder.unbind()
    }

    fun updateList(data: List<T>) {
        calculateDiff(DiscoveryDiffUtilCallback(dataList, data)).apply {
            dataList.clear()
            dataList.addAll(data)
            dispatchUpdatesTo(this@BaseRecyclerAdapter)
        }
    }

    fun getDataList() = dataList.toList()

    fun addItemToEnd(item : T) {
        dataList.add(item)
        notifyItemInserted(dataList.size)
    }

    fun observeClickEvents(): Observable<T> = publishSubject

    abstract class BaseViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        protected var t: T? = null
        private var publishSubject: PublishSubject<T>? = null

        fun bind(t: T, subject: PublishSubject<T>) {
            this.t = t
            this.publishSubject = subject
            itemView.setOnClickListener(this)
            onBind(t)

        }

        fun unbind() {
            itemView.setOnClickListener(null)
            publishSubject = null
            onUnbind()
        }

        abstract fun onBind(t: T)

        abstract fun onUnbind()

        override fun onClick(v: View?) {
            publishSubject?.let { subject -> t?.let { subject.onNext(it) } }
        }
    }

    class DiscoveryDiffUtilCallback(private val oldItems: List<*>, private val newItems: List<*>) : Callback() {
        override fun areItemsTheSame(p0: Int, p1: Int): Boolean = oldItems[p0] == newItems[p1]
        override fun getOldListSize(): Int = oldItems.size
        override fun getNewListSize(): Int = newItems.size
        override fun areContentsTheSame(p0: Int, p1: Int): Boolean = oldItems[p0] == newItems[p1]
    }
}