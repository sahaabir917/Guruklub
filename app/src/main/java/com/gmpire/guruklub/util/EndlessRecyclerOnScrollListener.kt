package com.gmpire.guruklub.util

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


abstract class EndlessRecyclerOnScrollListener() : RecyclerView.OnScrollListener() {


    var TAG = EndlessRecyclerOnScrollListener::class.java.simpleName

    private var previousTotal = 0 // The total number of items in the dataset after the last load
    private var loading = true // True if we are still waiting for the last set of data to load.
    private var visibleThreshold =5 // The minimum amount of items to have below your current scroll position before loading more.
    private var firstVisibleItem: Int = 0
    private var visibleItemCount: Int = 0
    private var totalItemCount: Int = 0

    private var currentPage = 1

    private var mLinearLayoutManager: LinearLayoutManager? = null
    //private var mGridLayoutManager: GridLayoutManager? = null

    constructor(linearLayoutManager: LinearLayoutManager) : this() {
        this.mLinearLayoutManager = linearLayoutManager
    }

//    constructor(mGridLayoutManager: GridLayoutManager) : this() {
//        this.mGridLayoutManager = mGridLayoutManager
//    }

    fun resetState() {
        previousTotal = 0
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
    }

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {

        visibleItemCount = recyclerView.childCount
        totalItemCount = mLinearLayoutManager?.itemCount ?: 0
        firstVisibleItem =mLinearLayoutManager?.findFirstVisibleItemPosition() ?: 0

        if (loading) {
            if (totalItemCount > previousTotal) {
                loading = false;
                previousTotal = totalItemCount;
            }
        }
        if (!loading && (totalItemCount - visibleItemCount)
                <= (firstVisibleItem + visibleThreshold)) {
            // End has been reached

            // Do something
            currentPage++;

            onLoadMore(currentPage)

            loading = true
        }
    }

    abstract fun onLoadMore(currentPage: Int)
}