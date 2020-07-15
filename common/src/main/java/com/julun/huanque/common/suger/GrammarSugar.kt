package com.julun.huanque.common.suger

import android.animation.Animator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.annotation.ColorInt
import androidx.fragment.app.DialogFragment
import androidx.core.content.FileProvider
import androidx.appcompat.app.AppCompatActivity
import com.facebook.drawee.generic.RoundingParams
import com.julun.huanque.common.utils.ULog
import com.facebook.drawee.span.DraweeSpanStringBuilder
import com.facebook.widget.text.span.BetterImageSpan
import com.julun.huanque.common.helper.DensityHelper
import com.julun.huanque.common.utils.SortUtils
import com.julun.huanque.common.utils.fresco.DraweeHolderBuilder
import java.io.File
import java.io.Serializable
import java.lang.reflect.Field
import java.util.*


/**
 * define some grammar sugar for android development
 * Created by nirack on 16-10-10.
 */


fun Any.logger(message: String) {
    ULog.i("I${javaClass.simpleName}", message)
}

/**
 * dp转px相关扩展函数
 */
fun Any.dp2px(value: Float): Int {
    return DensityHelper.dp2px(value)
}

fun Any.dp2px(value: Int): Int {
    return DensityHelper.dp2px(value)
}

fun Any.dp2pxf(value: Int): Float {
    return DensityHelper.dp2pxf(value)
}

/**
 * px转dp相关扩展函数
 */
fun Any.px2dp(value: Float): Int {
    return DensityHelper.px2dp(value)
}


/**
 * 将一个list分割,按照每个
 */
fun <T> List<T>.sliceBySubLength(size: Int = this.size - 1): List<List<T>> {
    val result: MutableList<List<T>> = mutableListOf()
    val maxIndex: Int = this.size / size + if (this.size % size > 0) 1 else 0
    for (index in 0..maxIndex - 1) {
        var toIndex = (index + 1) * size
        if (toIndex >= this.size) {
            toIndex = this.size
        }
        val sub = this.subList(index * size, toIndex)
        result.add(sub)
    }
    return result
}

/**
 * subList返回的是一个SubList集合 无法强转成MutableList 这里做了修改
 */
fun <T> List<out T>.sliceBySubLengthNew(size: Int = this.size - 1): List<List<T>> {
    val result: MutableList<List<T>> = mutableListOf()
    var maxIndex: Int = this.size / size + if (this.size % size > 0) 1 else 0
    for (index in 0..maxIndex - 1) {
        val subList = mutableListOf<T>()

        var toIndex = (index + 1) * size
        if (toIndex >= this.size) {
            toIndex = this.size
        }
        val sub = this.subList(index * size, toIndex)
        subList.addAll(sub)
        result.add(subList)
    }
    return result
}

fun Context.installApk(targetDownloadFile: File): Unit {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.flags = FLAG_ACTIVITY_NEW_TASK
    intent.addFlags(FLAG_GRANT_READ_URI_PERMISSION or FLAG_ACTIVITY_NEW_TASK)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        val uriForFile: Uri =
                FileProvider.getUriForFile(this, "${this.packageName}.fileprovider", targetDownloadFile)
        //添加这一句表示对目标应用临时授权该Uri所代表的文件
        intent.setDataAndType(uriForFile, "application/vnd.android.package-archive")
    } else {
        val fromFile: Uri = Uri.fromFile(targetDownloadFile)
        intent.setDataAndType(fromFile, "application/vnd.android.package-archive")
    }
    startActivity(intent)
//    huanqueApp.newActivity?.finish()
//    android.os.Process.killProcess(android.os.Process.myPid())
}


/**
 * 取代 if(someObject != null){}else{}
 */
fun <T> safeRunNullOrNonNull(
        objectPassedBy: T?,
        whenItsNotNull: (objectPassedBy: T) -> Unit = {},
        whenItsNull: () -> Unit = {}
): Unit {
    if (objectPassedBy != null) {
        whenItsNotNull(objectPassedBy)
    } else {
        whenItsNull()
    }
}


fun Any.anyhow(callback: () -> Unit = {}): Unit {
    callback()
}

fun DraweeSpanStringBuilder.setImageSpan(
        context: Context,
        url: String,
        position: Int,
        widthPx: Int,
        heigthPx: Int
) {
//    ULog.i("当前的span图片url:"+url)
    setImageSpan(
            DraweeHolderBuilder.createHolder(context, url),
            position,
            widthPx,
            heigthPx,
            true,
            BetterImageSpan.ALIGN_CENTER
    )
}

fun DraweeSpanStringBuilder.setImageSpan(
        context: Context,
        url: String,
        start: Int,
        end: Int,
        widthPx: Int,
        heigthPx: Int
) {
    setImageSpan(
            DraweeHolderBuilder.createHolder(context, url),
            start,
            end,
            widthPx,
            heigthPx,
            true,
            BetterImageSpan.ALIGN_CENTER
    )
}

fun DraweeSpanStringBuilder.setImageSpan(
        context: Context,
        imageResId: Int,
        start: Int,
        end: Int,
        widthPx: Int,
        heigthPx: Int
) {
//    ULog.i("当前的span图片Id:"+imageResId)
    setImageSpan(
            DraweeHolderBuilder.createHolder(context, imageResId),
            start,
            end,
            widthPx,
            heigthPx,
            true,
            BetterImageSpan.ALIGN_CENTER
    )
}

/**
 * 设置一个圆形图片
 */
fun DraweeSpanStringBuilder.setCircleImageSpan(context: Context, url: String, @ColorInt borderRedId: Int = 0, borderWidth: Float = 0f, position: Int, widthPx: Int, heigthPx: Int) {
    setImageSpan(DraweeHolderBuilder.createCircleHolder(context, url, borderRedId, borderWidth), position, widthPx, heigthPx, true, BetterImageSpan.ALIGN_CENTER)
}

/**
 * 设置一个圆形图片
 */
fun DraweeSpanStringBuilder.setCircleImageSpan(context: Context, url: String, params: RoundingParams, position: Int, widthPx: Int, heigthPx: Int) {
    setImageSpan(DraweeHolderBuilder.createCircleHolder(context, url, params), position, widthPx, heigthPx, true, BetterImageSpan.ALIGN_CENTER)
}

//Int的几个扩展
/**
 * 是否是奇数
 */
fun Int.isOdd(): Boolean {
    return this % 2 != 0
}

/**
 * 是否是偶数
 */
fun Int.isEven(): Boolean = !this.isOdd()


/**
 * 扩展一些函数作为语法糖
 * Created by nirack on 16-10-31.
 */


object ColorHax {
    private val random: Random = Random()
    private val HAX_PAIRS: List<Pair<Int, String>> =
            (0..15).toMutableList().map { if (it < 10) it to "$it" else it to "${'A' + (it - 10)}" }

    fun randomColor(): String {
        return "#${(1..6).map { HAX_PAIRS[random.nextInt(16)].second }
                .joinToString(separator = "")}"
    }
}

/**
 * 默认的动画监听器
 * 子类可以选择性的只重写需要的方法
 */
interface DefaultAnimatorListener : Animator.AnimatorListener {
    override fun onAnimationRepeat(animation: Animator) {
    }

    override fun onAnimationEnd(animation: Animator) {
    }

    override fun onAnimationCancel(animation: Animator) {
    }

    override fun onAnimationStart(animation: Animator) {
    }
}

/**
 * 提供便捷的方式组成Bundle对象
 */
fun Iterable<Pair<String, out Serializable>>.bundle(): Bundle {
    val bundle = Bundle()
    forEach {
        bundle.putSerializable(it.first, it.second)
    }
    return bundle
}


fun Map<String, out Serializable>.bundle(): Bundle {
    val intent = Bundle()
    this.forEach {
        intent.putSerializable(it.key, it.value)
    }
    return intent
}

fun DialogFragment.show(activity: AppCompatActivity, tag: String) {
    this.show(activity.supportFragmentManager, tag)
}

fun android.app.DialogFragment.show(activity: Activity, tag: String) {
    this.show(activity.fragmentManager, tag)
}

object Anys {

    /**
     * 判断两个对象是否都为空
     */
    fun bothNull(obj1: Any?, obj2: Any?): Boolean = obj1 == null && obj2 == null

    /**
     * 判断两个对象只有一个为空
     */
    fun oneNullAndOnlyOne(obj1: Any?, obj2: Any?): Boolean =
            (obj1 == null && obj2 != null) || (obj1 != null && obj2 == null)

    /**
     * 全部为空
     */
    fun allNull(vararg objs: Any?): Boolean = objs.filter { it != null }.size == 0

    /**
     * 条件成立,返回第一个值,否则返回第二个
     * @param condition 判断的条件
     * @param valueToUseIfTrue 如果条件为真的值
     * @param valueToUseIfFalse 如果条件为假的值
     */
    fun <T> ifTrueOrElse(condition: Boolean, valueToUseIfTrue: T, valueToUseIfFalse: T): T {
        return if (condition) valueToUseIfTrue else valueToUseIfFalse
    }

    fun <T> ifTrueOrElse(
            conditionFunc: () -> Boolean,
            valueToUseIfTrue: T,
            valueToUseIfFalse: T
    ): T {
        return ifTrueOrElse(conditionFunc(), valueToUseIfTrue, valueToUseIfFalse)
    }

    /**
     * @see ifTrueOrElse
     */
    fun <T> ifTrueOrElse(
            condition: Boolean,
            functionToInvokeIfTrue: () -> T,
            functionToInvokeIfFalse: () -> T
    ): T {
        return ifTrueOrElse(condition, functionToInvokeIfTrue(), functionToInvokeIfFalse())
    }

    /**
     * @see ifTrueOrElse
     */
    fun <T> ifTrueOrElse(
            conditionFunc: () -> Boolean,
            functionToInvokeIfTrue: () -> T,
            functionToInvokeIfFalse: () -> T
    ): T {
        return ifTrueOrElse(conditionFunc(), functionToInvokeIfTrue, functionToInvokeIfFalse)
    }

}

/**
 * 删除部分数据 从零开始
 */
fun <E> LinkedHashSet<E>.removeScope(range: Int) {
    val iter = iterator()
    var count = 0
    while (count < range && iter.hasNext()) {
        iter.next()
        iter.remove()
        count++
    }
}

/**
 * list去重操作 (对象去重必须重写equals和hashcode两方法)
 */
fun <T> ArrayList<T>.removeDuplicate() {
    val set = LinkedHashSet<T>(this.size)
    set.addAll(this)
    this.clear()
    this.addAll(set)
}

/**
 * list合并目标list 保持顺序 并去重(对象去重必须重写equals和hashcode两方法 不然无效)
 */
fun <T> List<T>.mergeNoDuplicate(newList: List<T>): List<T> {
    val set = LinkedHashSet<T>(this.size + newList.size)
    set.addAll(this)
    set.addAll(newList)
    val result = mutableListOf<T>()
    result.addAll(set)
    return result
}

/**
 * list合并目标list 保持顺序
 * 方法同上 但是重复时 新数据会替换老数据
 * 注：：(对象去重必须重写equals和hashcode两方法 不然无效)
 */
fun <T> List<T>.mergeNoDuplicateNew(newList: List<T>): List<T> {
    val set = LinkedHashSet<T>(this.size + newList.size)
    set.addAll(this)
    newList.forEach {
        if (set.contains(it)) {
            set.remove(it)
            set.add(it)
        } else {
            set.add(it)
        }
    }
    val result = mutableListOf<T>()
    result.addAll(set)
    return result
}

/**
 *  newList 要合并的新list
 *
 * 传入多个比较器 从左到右依次多次比较排序
 *
 * 如果某个比较器只比较单个属性可做去重 比如：comparator1比较id ，comparator2比较name,price等等多个属性
 * comparator1比较完后 相同的id已被过滤 那么comparator1实际上是去重，comparator2才是排序
 * 默认去重保留newList
 *
 *
 */

fun <T> List<T>.mergeAndSort(newList: List<T>, vararg comparators: Comparator<T>): List<T> {
    val result = mutableListOf<T>()
    result.addAll(newList)
    result.addAll(this)
    comparators.forEach {
        val set = TreeSet<T>(it)
        set.addAll(result)

        result.clear()
        result.addAll(set)
    }
    return result
}

/**
 * 合并&去重&排序
 * newList 要合并的新list 默认去重保留newList
 *
 * 示例：list1 list2 如果保留list2 用 list1.mergeNoDuplicateAndSort(list2)
 * 如果保留list1 用 list2.mergeNoDuplicateAndSort(list1)
 *
 * keyNames 要去重的属性值 比如:{"id","name"}同时比较id和name都一致时 代表重复
 *
 * comparator 排序器
 *
 */
fun <T> List<T>.mergeNoDuplicateAndSort(
        newList: List<T>,
        keyNames: ArrayList<Field>,
        comparator: Comparator<T>
): List<T> {
    val comparator0 = Comparator { t1: T, t2: T ->
        var ret = 0
        try {
            for (i in keyNames.indices) {
                ret = SortUtils.compareObject<T>(keyNames[i].name, true, t1, t2)
                if (0 != ret) {
                    break
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        ret
    }


    val set = TreeSet<T>(comparator0)
    set.addAll(newList)
    set.addAll(this)
    val result = mutableListOf<T>()
    result.addAll(set)
//TreeSet排序有个问题 比较相同时会做去重
//    val set2 = TreeSet<T>(comparator)
//    set2.addAll(result)
//    result.clear()
//    result.addAll(set2)
    try {
        Collections.sort(result, comparator)
    } catch (e: Exception) {
        e.printStackTrace()
//        reportCrash("排序错误", e)
    }
    return result
}

/**
 * 单纯的列表排序
 */
fun <T> List<T>.sortList(comparator: Comparator<T>): List<T> {
//    val result = mutableListOf<T>()
//    result.addAll(this)
    try {
        Collections.sort(this, comparator)
    } catch (e: Exception) {
        e.printStackTrace()
//        reportCrash("排序错误", e)
    }
    return this
}

//有序的遍历 从0开始
public inline fun <T> List<T>.forEachIndexedOrdered(action: (index: Int, T) -> Unit): Unit {
    var index = 0
    val last = this.size - 1
    for (i in 0..last) {
        val item = this[i]
        action(index++, item)
    }
}
