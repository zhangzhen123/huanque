package com.julun.huanque.common.suger

import android.app.Activity
import com.julun.huanque.common.base.AppBaseDialog
import java.util.*
import kotlin.collections.HashMap

//下面的是为自动关闭 弹出框而做的扩展

private val DIALOG_HOLDER = HashMap<Activity, Vector<AppBaseDialog>>()

var Activity.dialogsHolder:Vector<AppBaseDialog>
    set(value){
        DIALOG_HOLDER[this] = value
    }
    get() {
        var vector: Vector<AppBaseDialog>? = DIALOG_HOLDER[this]
        if(vector == null){
            vector = Vector()
            DIALOG_HOLDER[this] = vector
        }
        return vector!!
    }

fun Activity.holdDialog(dialog:AppBaseDialog){
    val holder: Vector<AppBaseDialog> = this.dialogsHolder
    holder.add(dialog)
    DIALOG_HOLDER[this] = holder
}

fun Activity.hideDialogs(){
    dialogsHolder.forEach {
        it.dismiss()
    }
    DIALOG_HOLDER.remove(this)
}
