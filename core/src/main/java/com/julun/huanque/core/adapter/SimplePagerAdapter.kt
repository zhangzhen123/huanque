package com.julun.huanque.core.adapter

import androidx.viewpager.widget.PagerAdapter
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import java.util.*


/**
 * Created by nirack on 16-11-16.
 */
abstract class SimplePagerAdapter<DATA,VIEW:View>(private val itemLayoutId: Int) : PagerAdapter() {
    private val datas = ArrayList<DATA>()
    private val views = SparseArray<VIEW>()
    private var layoutInflater: LayoutInflater? = null

    override fun getCount(): Int {
        return datas.size
    }
    fun getData(): ArrayList<DATA> {
        return datas
    }
    fun getItemAt(position: Int): DATA {
        return datas[position]
    }
    fun getViewAt(position: Int): VIEW? {
        return views[position]
    }
    fun clear(notify: Boolean = false): Unit {
        datas.clear()
        if(notify) notifyDataSetChanged()
    }

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view == obj
    }

    fun addAndNotify(list: MutableCollection<DATA> = mutableListOf(), vararg items: DATA) {
        datas.addAll(items.filterTo(list,{true}))
        notifyDataSetChanged()
    }

    fun addItem(item: DATA, notify: Boolean = false) {
        datas.add(item)
        if (notify) {
            notifyDataSetChanged()
        }
    }



    override fun destroyItem(container: ViewGroup, position: Int, childItem: Any) {
        //啥都不干
//        container.removeViewAt(position)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        var view: VIEW? = views.get(position)
        if (view == null) {
            if (layoutInflater == null) {
                layoutInflater = LayoutInflater.from(container.context)
            }
            view = layoutInflater!!.inflate(itemLayoutId, null) as VIEW
            views.put(position,view)
        }else{
            if (container == view.parent) {
                container.removeView(view)
            }
        }
        renderItem(view, getItemAt(position))
        container.addView(view)
        return view
    }

    protected abstract fun renderItem(view: VIEW, itemAt: DATA)
}
