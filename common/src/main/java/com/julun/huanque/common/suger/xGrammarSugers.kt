package com.julun.huanque.common.suger

import android.app.Activity
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.julun.huanque.common.base.AppBaseDialog
import java.util.*
import kotlin.collections.HashMap

//下面的是为自动关闭 弹出框而做的扩展

private val DIALOG_HOLDER = HashMap<Activity, Vector<AppBaseDialog>>()

var Activity.dialogsHolder: Vector<AppBaseDialog>
    set(value) {
        DIALOG_HOLDER[this] = value
    }
    get() {
        var vector: Vector<AppBaseDialog>? = DIALOG_HOLDER[this]
        if (vector == null) {
            vector = Vector()
            DIALOG_HOLDER[this] = vector
        }
        return vector
    }

fun Activity.holdDialog(dialog: AppBaseDialog) {
    val holder: Vector<AppBaseDialog> = this.dialogsHolder
    holder.add(dialog)
    DIALOG_HOLDER[this] = holder
}

fun Activity.hideDialogs() {
    dialogsHolder.forEach {
        it.dismiss()
    }
    DIALOG_HOLDER.remove(this)
}

/**
 *
 * 后台的分页数据 单页返回的内容基本不会重复 但是多页时由于数据变化 在下一页时可能会返回前面已经返回的元素 所以这个通用的方法用来在adapter新增数据时
 * 做去重操作 如果新增列表[newList]的元素已经存在于adapter中直接过滤之
 *
 *  注意：(对象[T]必须重写equals和hashcode两方法)
 */
fun <T> BaseQuickAdapter<T, BaseViewHolder>.addNoDuplicate(newList: MutableList<T>) {
    this.addData(newList.removeDuplicate(this.data))
}
