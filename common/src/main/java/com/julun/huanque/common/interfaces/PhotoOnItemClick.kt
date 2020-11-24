package com.julun.huanque.common.interfaces

import com.chad.library.adapter.base.BaseQuickAdapter

//由于BaseQuickAdapter的点击事件只会在onCreateViewHolder绑定一次 对于复用池的viewHolder这里就会出问题 所以自定义一个item点击事件每次cover重新绑定
interface PhotoOnItemClick {
    fun onItemClick(adapter: BaseQuickAdapter<*, *>, position: Int)
}