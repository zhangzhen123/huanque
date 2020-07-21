package com.julun.huanque.common.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.julun.huanque.common.R
import com.julun.huanque.common.bean.beans.RoomUserChatExtra
import com.julun.huanque.common.bean.message.CustomMessage
import com.julun.huanque.common.bean.message.CustomSimulateMessage
import com.julun.huanque.common.constant.MeetStatus
import com.julun.huanque.common.database.HuanQueDatabase
import com.julun.huanque.common.init.CommonInit
import io.rong.message.ImageMessage
import io.rong.message.TextMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import java.lang.ref.SoftReference

/**
 *@创建者   dong
 *@创建时间 2019/5/7 8:56
 *@描述  全局的utils类  用于存放一些不成体系的工具方法
 */
object GlobalUtils {

    /**
     * 将string转化为map  string需要符合 "***-***" 这样的格式
     */
    fun channel2Map(str: String): MutableMap<String, String> {
        val map = mutableMapOf<String, String>()
        val array = str.split("-")
        val count = array.size / 2
        (0 until count).forEach {
            val index = it * 2
            if (ForceUtils.isIndexNotOutOfBounds(index, array) && ForceUtils.isIndexNotOutOfBounds(index, array)) {
                map.put(array[index], array[index + 1])
            }
        }
        return map
    }


    /**
     * 将融云之类的跳转的Uri携带的参数解析为Map
     */
    fun uri2Map(str: String): MutableMap<String, String> {
//        val map = mutableMapOf<String, String>()
//        val array = str.split("?")
//        if(ForceUtils.isIndexNotOutOfBounds(1,array)){
//            val paramStr = array[1]
//            val paramArray = paramStr.split("&")
//
//        }
        val split: List<String>? = str?.split("?")

        var map: MutableMap<String, String> = mutableMapOf()
        split?.forEachIndexed { index, str ->
            if (index == 0) return@forEachIndexed
            val params: List<String> = str.split("&")
            params.forEach {
                val sub = it.split("=")
                if (sub.size > 1)
                    map.put(sub[0], sub[1])
            }
        }
        return map
    }

//    /**
//     * 获取MD5的值
//     * 该方法为专用方法，其它功能请勿使用
//     */
//    fun getMD5(str: String): String {
//        val temp = MD5Util.EncodePassword(str + BusiConstant.MsgTag.PREFIX) + BusiConstant.MsgTag.TAG
//        return MD5Util.EncodePassword(temp)
//    }

    /**
     * 获取String
     */
    fun getString(@StringRes rId: Int): String {
        return CommonInit.getInstance().getApp().resources.getString(rId)
    }

    /**
     * 获取颜色
     */
    @ColorInt
    fun getColor(@ColorRes cId: Int): Int {
        return try {
            ContextCompat.getColor(CommonInit.getInstance().getApp(), cId)
        } catch (e: Exception) {
            e.printStackTrace()
            Color.WHITE
        }
    }

    /**
     * 颜色进行初始化 解析失败。返回Color.WHITE
     * @param colorStr string类型的色值
     */
    fun formatColor(colorStr: String): Int {
        return try {
            Color.parseColor(colorStr)
        } catch (e: Exception) {
            e.printStackTrace()
            Color.WHITE
        }
    }

    /**
     * 配置一个字符串的色值，再配置一个异常的色值，如果都是异常，那么就使用默认的白色
     */
    @ColorInt
    fun formatColor(colorStr: String, @ColorRes errorColor: Int): Int {
        if (colorStr.isEmpty()) {
            return getColor(errorColor)
        }
        return try {
            Color.parseColor(colorStr)
        } catch (e: Exception) {
            e.printStackTrace()
            getColor(errorColor)
        }
    }

    /**
     * 获取Drawable
     */
    fun getDrawable(@DrawableRes dId: Int) = CommonInit.getInstance().getApp().resources.getDrawable(dId)


    /**
     * 获取Context对应的Activity
     */
    fun getActivity(context: Context): Activity? {
        var mContext = context
        while (mContext is ContextWrapper) {
            if (mContext is Activity) {
                return mContext
            }
            mContext = mContext.baseContext
        }
        return null
    }

    /**
     * 拷贝出一个独立的对象
     */
    @Throws(IOException::class, ClassNotFoundException::class)
    fun <T : Serializable> clone(`object`: T): T {
        // 说明：调用ByteArrayOutputStream或ByteArrayInputStream对象的close方法没有任何意义
        // 这两个基于内存的流只要垃圾回收器清理对象就能够释放资源，这一点不同于对外资源(如文件流)的释放
        val baos = ByteArrayOutputStream()
        val oos = ObjectOutputStream(baos)
        oos.writeObject(`object`)

        val bais = ByteArrayInputStream(baos.toByteArray())
        val ois = ObjectInputStream(bais)
        return ois.readObject() as T
    }

    private var refrernce: SoftReference<Handler>? = null

    /**
     * 全局配置一个切换主线程的函数，可以在需要的时候做切换操作
     * @author WanZhiYuan
     * @sence 4.27
     * @date 2020/03/06
     */
    fun switchMain(callback: () -> Unit = {}) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            refrernce = refrernce ?: SoftReference(Handler(Looper.getMainLooper()))
            val handler = refrernce?.get()
            handler?.post {
                callback()
            }
        } else {
            callback()
        }
    }

    /**
     * 获取欢遇标识
     */
    fun getMeetStatusResource(status: String): Int {
        return when (status) {
            MeetStatus.Wait -> {
                //待欢遇
                R.mipmap.icon_huanyu_disable
            }
            MeetStatus.Meet -> {
                //欢遇中
                R.mipmap.icon_huanyu
            }
            else -> {
                0
            }
        }
    }

    /**
     * 获取背景的key(本人ID-对方ID)
     */
    fun getBackgroundKey(friendID: Long) = "${SessionUtils.getUserId()}-$friendID"


    fun getStrangerType(msg: io.rong.imlib.model.Message): Boolean {
        var extra: String = ""
        val conent = msg.content
        when (conent) {
            is ImageMessage -> {
                //图片消息
                extra = conent.extra
            }

            is CustomMessage -> {
                //自定义消息
                extra = conent.extra
            }

            is CustomSimulateMessage -> {
                //模拟消息
                extra = conent.extra
            }
            is TextMessage -> {
                //文本消息
                extra = conent.extra
            }
        }
        var user: RoomUserChatExtra? = null
        try {
            user = JsonUtil.deserializeAsObject(extra, RoomUserChatExtra::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return user?.stranger ?: false
    }


    /**
     * 更新陌生人状态
     */
    fun updataStrangerData(userId: Long, stranger: Boolean) {
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                val chatUser = HuanQueDatabase.getInstance().chatUserDao().querySingleUser(userId) ?: return@withContext
                if (chatUser.stranger != stranger) {
                    chatUser.stranger = stranger
                    HuanQueDatabase.getInstance().chatUserDao().insert(chatUser)
                }
            }
        }
    }
}