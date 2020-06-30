package com.julun.huanque.common.suger

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.utils.ImageUtils
import java.util.*

/**
 * 视图相关的一些扩展
 * Created by nirack on 16-12-12.
 */

/** 视图是否可见 **/
fun View.isVisible(): Boolean = this.visibility == View.VISIBLE

//TextView 粗体
fun TextView.bold(): TextView {
    paint.isFakeBoldText = true
    return this
}

//TextView 计算文字占用的宽度
fun TextView.calculateTextLength(text:String): Float {
    val measureText: Float = paint.measureText(text)
    return measureText
}

//下面两个提供更快捷的方法显示或者隐藏view,并返回其自身
fun View.hide(): View {
    this.visibility = View.GONE
    return this
}
fun <T: View> View.hideAsRealType():T  {
    this.visibility = View.GONE
    return this as T
}
//不显示,但是占位
fun View.inVisiable(): View {
    this.visibility = View.INVISIBLE
    return this
}

fun <T: View> View.inVisiableAsRealType():T  {
    this.visibility = View.INVISIBLE
    return this as T
}

fun View.show(): View {
    this.visibility = View.VISIBLE
    return this
}
//显示并且返回原本的类型
fun <T: View> View.showAsRealType():T  {
    this.visibility = View.VISIBLE
    return this as T
}

/**
 * 显示或者隐藏
 * @param condition 如果条件满足,则显示,否则隐藏
 */
fun View.showIf(condition:Boolean):View  = if (condition) { this.show() } else { this.hide() }
fun <T: View> View.showIfAsRealType(condition:Boolean):T  = if (condition) { this.show() } else { this.hide() } as T

//以下四个方法用于隐藏或者显示viewgroup里的子view,参数为 id或者 view,y意义参考函数名
fun ViewGroup.showOnly(id:Int):Unit  {
    (0..this.childCount-1).map { getChildAt(it) }.forEach { it.showIf(it.id == id) }
}

fun ViewGroup.showOnly(view: View):Unit  {
    (0..this.childCount-1).map { getChildAt(it) }.forEach {
        it.showIf(it.id == view.id)
    }
}

fun ViewGroup.hideOnly(id:Int):Unit  {
    (0..this.childCount-1).map { getChildAt(it) }.forEach { it.showIf(it.id != id)  }
}

fun ViewGroup.hideOnly(view: View):Unit  {
    (0..this.childCount-1).map { getChildAt(it) }.forEach { it.showIf(it.id != view.id) }
}

/**
 * 防止连续过快点击造成的重复提交
 */
fun android.view.View.onClickNew(l: (v: android.view.View?) -> Unit) {
    setOnClickListener(object : NoDoubleClickListener(){
        override fun onNoDoubleClick(v: View) {
            l(v)
        }
    })
}
fun android.view.View.onClick( l: (v: android.view.View?) -> Unit) {
    setOnClickListener(l)
}
fun android.view.View.onTouch( l: (v: android.view.View, event: android.view.MotionEvent) -> Boolean) {
    setOnTouchListener(l)
}
/**
 * SimpleDraweeView的加载图片资源的扩展
 */
fun SimpleDraweeView.loadImage(url:String,width:Float=50f,height: Float=50f){
    ImageUtils.loadImage(this,url,width,height)
}

/**
 * 加载资源 不裁剪
 */
fun SimpleDraweeView.loadImageNoResize(url:String){
    ImageUtils.loadImageNoResize(this,url)
}

fun SimpleDraweeView.loadImageInPx( url: String,width:Int,height: Int){
    ImageUtils.loadImageInPx(this,url,width,height)
}

fun SimpleDraweeView.loadImageLocal(placeHolderResId: Int){
    ImageUtils.loadImageLocal(this, placeHolderResId)
}

abstract class NoDoubleClickListener : View.OnClickListener {
    private var lastClickTime: Long = 0

    override fun onClick(v: View) {
        val currentTime = Calendar.getInstance().timeInMillis
        if (currentTime - lastClickTime >= MIN_CLICK_DELAY_TIME) {
            lastClickTime = currentTime
            onNoDoubleClick(v)
        }
    }
    abstract fun onNoDoubleClick(v: View)

    companion object {

        const val MIN_CLICK_DELAY_TIME = 500
    }
}
