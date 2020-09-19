package com.julun.huanque.common.utils

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.julun.huanque.common.R
import com.julun.huanque.common.bean.ChatUser
import com.julun.huanque.common.bean.beans.ChatBubble
import com.julun.huanque.common.bean.beans.PlayInfo
import com.julun.huanque.common.bean.beans.RoomUserChatExtra
import com.julun.huanque.common.bean.beans.UserInfo
import com.julun.huanque.common.bean.message.CustomMessage
import com.julun.huanque.common.bean.message.CustomSimulateMessage
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.constant.SPParamKey
import com.julun.huanque.common.database.HuanQueDatabase
import com.julun.huanque.common.helper.DensityHelper.Companion.dp2px
import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.common.suger.dp2pxf
import io.rong.imlib.model.MessageContent
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
    //特权表情的集合列表
    private val privilegeMap = hashMapOf<String, String>()

    init {
        // 加载特权表情
        privilegeMap.put("[约吗]", "config/expression-privilege/yuema.gif")
        privilegeMap.put("[等撩]", "config/expression-privilege/dengliao.gif")
        privilegeMap.put("[送花]", "config/expression-privilege/songhua.gif")
        privilegeMap.put("[么么哒]", "config/expression-privilege/memeda.gif")
        privilegeMap.put("[色眯眯]", "config/expression-privilege/semimi.gif")
        privilegeMap.put("[晚安]", "config/expression-privilege/wanan.gif")
        privilegeMap.put("[乞讨]", "config/expression-privilege/qitao.gif")
        privilegeMap.put("[耍酷]", "config/expression-privilege/shuaku.gif")
        privilegeMap.put("[在吗]", "config/expression-privilege/zaima.gif")
        privilegeMap.put("[太难了]", "config/expression-privilege/tainanle.gif")
        privilegeMap.put("[生气]", "config/expression-privilege/shengqi.gif")
        privilegeMap.put("[委屈]", "config/expression-privilege/weiqv.gif")
        privilegeMap.put("[哈哈哈]", "config/expression-privilege/hahaha.gif")
        privilegeMap.put("[约架]", "config/expression-privilege/yuejia.gif")
        privilegeMap.put("[咬你]", "config/expression-privilege/yaoni.gif")
        privilegeMap.put("[挑衅]", "config/expression-privilege/tiaoxin.gif")
    }

    /**
     * 将string转化为map  string需要符合 "***-***" 这样的格式
     */
    fun channel2Map(str: String): MutableMap<String, String> {
        val map = mutableMapOf<String, String>()
        val array = str.split("-")
        val count = array.size / 2
        (0 until count).forEach {
            val index = it * 2
            if (ForceUtils.isIndexNotOutOfBounds(index, array) && ForceUtils.isIndexNotOutOfBounds(
                    index,
                    array
                )
            ) {
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
    fun getDrawable(@DrawableRes dId: Int) =
        CommonInit.getInstance().getApp().resources.getDrawable(dId)


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
     * 获取背景的key(本人ID-对方ID)
     */
    fun getBackgroundKey(friendID: Long) = "${SessionUtils.getUserId()}-$friendID"


    fun getStrangerType(msg: io.rong.imlib.model.Message): Boolean {
        var extra: String = ""
        val conent = msg.content
        when (conent) {
            is ImageMessage -> {
                //图片消息
                extra = conent.extra ?: ""
            }

            is CustomMessage -> {
                //自定义消息
                extra = conent.extra ?: ""
            }

            is CustomSimulateMessage -> {
                //模拟消息
                extra = conent.extra ?: ""
            }
            is TextMessage -> {
                //文本消息
                extra = conent.extra ?: ""
            }
        }
        if (extra.isEmpty()) {
            return false
        }
        var user: RoomUserChatExtra? = null
        try {
            user = JsonUtil.deserializeAsObject(extra, RoomUserChatExtra::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return getStrangerBoolean(user?.targetUserObj?.stranger ?: "")
    }


    /**
     * 更新陌生人状态
     */
    fun updateStrangerData(userId: Long, stranger: Boolean) {
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                val chatUser = HuanQueDatabase.getInstance().chatUserDao().querySingleUser(userId)
                    ?: return@withContext
                if (chatUser.stranger != stranger) {
                    chatUser.stranger = stranger
                    HuanQueDatabase.getInstance().chatUserDao().insert(chatUser)
                }
            }
        }
    }

    /**
     * 复制文案
     */
    fun copyToSharePlate(context: Context, text: String, attentionContent: String = "内容已复制到剪切板") {
        val myClipboard: ClipboardManager =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        myClipboard.setPrimaryClip(ClipData.newPlainText("text", text))
        if (attentionContent.isNotEmpty()) {
//            ToastUtils.showCustom(attentionContent, Toast.LENGTH_SHORT, Gravity.CENTER_VERTICAL or Gravity.BOTTOM)
            Toast.makeText(context, attentionContent, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 消息extra添加字段
     */
    fun addExtra(extra: String?, key: String, value: Any): HashMap<String, Any> {
        var map = hashMapOf<String, Any>()
        try {
            if (extra?.isNotEmpty() == true) {
                val tempMap = JsonUtil.toJsonMap(extra) as? HashMap
                if (tempMap != null) {
                    map = tempMap
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        map.put(key, value)
        return map
    }

    /**
     * 获取发言数量
     * @param isAnchor 是否是主播
     * @param info 用户信息
     */
    fun speakCount(isAnchor: Boolean, info: UserInfo?): Int {
        var count = 0
        if (isAnchor || info?.officalManager == true) {
            count = 60
        } else if (info?.roomGuard == true || (info?.royalLevel ?: 0) > 0) {
            //上神45字
            if (info?.royalLevel ?: 0 >= 9) {
                count = 45
            } else {
                //守护或贵族
                count = 40
            }
        } else {
            val level = info?.userLevel ?: 1
            when {
                level in 1..5 -> count = 10
                level in 6..9 -> count = 15
                level >= 10 -> count = 20
            }
        }
        return count
    }

    /**
     * 获取需要刷新的消息ID
     */
    fun getNeedRefreshMessageIdSet(): HashSet<String> {
        val defaultSet = HashSet<String>()
        val oriSet =
            SharedPreferencesUtils.getStringSet(SPParamKey.EXCEPTION_MESSAGE_LIST, defaultSet)
        return if (oriSet is HashSet<String>) {
            oriSet
        } else {
            defaultSet
        }
    }

    /**
     * 移除单个需要刷新的消息ID
     */
    fun removeSingleRefreshMessageId(msgId: Int) {
        val idSet = getNeedRefreshMessageIdSet()
        idSet.remove("$msgId")
        SharedPreferencesUtils.commitStringSet(SPParamKey.EXCEPTION_MESSAGE_LIST, idSet)
    }


    /**
     * 添加单个需要刷新的CallId
     */
    fun addSingleRefreshMessageId(msgId: Int) {
        val oriSet = getNeedRefreshMessageIdSet()
        oriSet.add("$msgId")
        SharedPreferencesUtils.commitStringSet(SPParamKey.EXCEPTION_MESSAGE_LIST, oriSet)
    }


    /**
     * 获取播放地址
     */
    fun getPlayUrl(info: PlayInfo): String {
        val type = info.type
        return if (type == PlayInfo.Url) {
            //URL格式
            info.rtmp
        } else {
            val string = getString(R.string.stream_template)
            String.format(string, info.domain, info.streamKey)
        }
    }

    /**
     * 从消息里面获取用户数据
     */
    fun getUserInfoFromMessage(lastMessage: MessageContent): ChatUser? {
        var extra = ""
        when (lastMessage) {
            is ImageMessage -> {
                lastMessage.extra?.let {
                    extra = it
                }
            }

            is CustomMessage -> {
                lastMessage.extra?.let {
                    extra = it
                }
            }

            is CustomSimulateMessage -> {
                lastMessage.extra?.let {
                    extra = it
                }
            }
            is TextMessage -> {
                //文本消息
                lastMessage.extra?.let {
                    extra = it
                }
            }
        }
        if (extra.isEmpty()) {
            return null
        }
        var user: RoomUserChatExtra? = null
        try {
            user = JsonUtil.deserializeAsObject(extra, RoomUserChatExtra::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        var currentUser: ChatUser? = null
        if (user != null) {
            if (user.senderId == SessionUtils.getUserId()) {
                //本人发送消息，取targetUserObj
                currentUser = ChatUser().apply {
                    headPic = user.targetUserObj?.headPic ?: ""
                    nickname = user.targetUserObj?.nickname ?: ""
                    intimateLevel = user.targetUserObj?.intimateLevel ?: 0
                    //欢遇标识
                    meetStatus = user.targetUserObj?.meetStatus ?: ""
                    //用户ID
                    userId = user.targetUserObj?.userId ?: 0
                    //用户性别
                    sex = user.targetUserObj?.sex ?: ""
                    stranger = getStrangerBoolean(user.targetUserObj?.stranger ?: "")
                }
            } else {
                //对方发送消息
                currentUser = ChatUser().apply {
                    headPic = user.headPic
                    nickname = user.nickname
                    intimateLevel = user.targetUserObj?.intimateLevel ?: 0
                    //欢遇标识
                    meetStatus = user.targetUserObj?.meetStatus ?: ""
                    //用户ID
                    userId = user.senderId
                    //用户性别
                    sex = user.sex
                    stranger = getStrangerBoolean(user.targetUserObj?.stranger ?: "")
                }
            }

        }
        return currentUser
    }

    fun getEarphoneLinkStatus(): Boolean {
        val audioManager = CommonInit.getInstance().getCurrentActivity()
            ?.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            for (device in devices) {
                val deviceType: Int = device.type
                if (deviceType == AudioDeviceInfo.TYPE_WIRED_HEADSET || deviceType == AudioDeviceInfo.TYPE_WIRED_HEADPHONES || deviceType == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP || deviceType == AudioDeviceInfo.TYPE_BLUETOOTH_SCO
                ) {
                    return true
                }
            }
        } else {
            return audioManager.isWiredHeadsetOn || audioManager.isBluetoothScoOn || audioManager.isBluetoothA2dpOn
        }
        return false
//        val am = CommonInit.getInstance().getCurrentActivity()?.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
//        am?.mode = AudioManager.MODE_IN_COMMUNICATION
////获取当前使用的麦克风，设置媒体播放麦克风
//        if (am?.isWiredHeadsetOn == true) {
//            logger("Voice 有线耳机已连接")
////            if (showToast) {
////                Toast.makeText(this, "有线耳机已连接", Toast.LENGTH_SHORT).show()
////            }
//            return true
//        } else {
//            logger("Voice 有线耳机未连接")
////            if (showToast) {
////                Toast.makeText(this, "有线耳机未连接", Toast.LENGTH_SHORT).show()
////            }
//        }
//
//        val adapter = BluetoothAdapter.getDefaultAdapter()
//        val connectionState = adapter.getProfileConnectionState(BluetoothProfile.HEADSET)
//
//        if (BluetoothProfile.STATE_CONNECTED == connectionState) {
//            logger("Voice 蓝牙耳机已连接")
////            if (showToast) {
////                Toast.makeText(this, "蓝牙耳机已连接", Toast.LENGTH_SHORT).show()
////            }
//            return true
//        } else if (BluetoothProfile.STATE_DISCONNECTED == connectionState) {
//            logger("Voice 蓝牙耳机未连接")
//            return false
//        } else {
//            logger("Voice 蓝牙耳机未连接")
//            return false
//        }
    }

    /**
     * 获取陌生人String状态
     */
    fun getStrangerString(stranger: Boolean): String {
        if (stranger) {
            return BusiConstant.True
        } else {
            return BusiConstant.False
        }
    }

    /**
     * 获取陌生人Boolean状态
     */
    fun getStrangerBoolean(stranger: String) = stranger == BusiConstant.True

    /**
     * 获取特权表情动图的地址
     */
    fun getPrivilegeUrl(key: String) = privilegeMap[key] ?: ""


    /**
     * 获取气泡的Drawable
     * @param left 是否是居右显示
     */
    fun getBubbleDrawable(bubble: ChatBubble, left: Boolean): Drawable {
        val borderColor = getColorArray(bubble.bdc)
        val borderDrawable = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, borderColor)
        borderDrawable.shape = GradientDrawable.RECTANGLE
        val floatArray = if (left) {
            floatArrayOf(
                dp2pxf(2),
                dp2pxf(2),
                dp2pxf(23),
                dp2pxf(23),
                dp2pxf(23),
                dp2pxf(23),
                dp2pxf(23),
                dp2pxf(23)
            )
        } else {
            floatArrayOf(
                dp2pxf(23),
                dp2pxf(23),
                dp2pxf(2),
                dp2pxf(2),
                dp2pxf(23),
                dp2pxf(23),
                dp2pxf(23),
                dp2pxf(23)
            )
        }
        borderDrawable.cornerRadii = floatArray


        val solidColor = getColorArray(bubble.bgc)
        val solidDrawable = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, solidColor)
        solidDrawable.shape = GradientDrawable.RECTANGLE
        solidDrawable.setStroke(dp2px(1), Color.TRANSPARENT)
        solidDrawable.cornerRadii = floatArray
        val drawableArray = arrayOf(borderDrawable, solidDrawable)
        return LayerDrawable(drawableArray)
    }

    /**
     * 获取颜色数组
     */
    private fun getColorArray(colors: String): IntArray {
        val colorArray = IntArray(2)
        val colorStrArray = colors.split("-")
        if (ForceUtils.isIndexNotOutOfBounds(0, colorStrArray)) {
            colorArray[0] = formatColor(colorStrArray[0])
        } else {
            colorArray[0] = Color.WHITE
        }

        if (ForceUtils.isIndexNotOutOfBounds(1, colorStrArray)) {
            colorArray[1] = formatColor(colorStrArray[1])
        } else {
            colorArray[1] = Color.WHITE
        }
        return colorArray
    }

    /**
     * 获取新手礼包保存的key
     */
    fun getNewUserKey(userId: Long) = "NewUser-$userId"


}