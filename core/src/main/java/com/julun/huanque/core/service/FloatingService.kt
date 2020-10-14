package com.julun.huanque.core.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Outline
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.RequiresApi
import com.alibaba.android.arouter.launcher.ARouter
import com.julun.huanque.common.base.dialog.MyAlertDialog
import com.julun.huanque.common.bean.events.FloatingCloseEvent
import com.julun.huanque.common.bean.forms.ProgramIdForm
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.constant.ParamConstant
import com.julun.huanque.common.constant.PlayerFrom
import com.julun.huanque.common.constant.SPParamKey
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.common.manager.ActivitiesManager
import com.julun.huanque.common.manager.UserHeartManager
import com.julun.huanque.common.net.RequestCaller
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.LiveRoomService
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.dp2px
import com.julun.huanque.common.suger.dp2pxf
import com.julun.huanque.common.utils.SPUtils
import com.julun.huanque.common.utils.ScreenUtils
import com.julun.huanque.common.utils.SharedPreferencesUtils
import com.julun.huanque.common.utils.permission.PermissionUtils
import com.julun.huanque.core.R
import com.julun.huanque.core.manager.FloatingManager
import com.julun.huanque.core.ui.live.PlayerActivity
import com.julun.huanque.core.widgets.SingleVideoView
import com.julun.huanque.core.widgets.SurfaceVideoViewOutlineProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.dip


/**
 *@创建者   dong
 *@创建时间 2020/8/5 16:39
 *@描述 悬浮窗使用的service
 */
class FloatingService : Service(), View.OnClickListener, RequestCaller {

    private var windowManager: WindowManager? = null
    private var layoutParams: WindowManager.LayoutParams = WindowManager.LayoutParams()

    //距离屏幕的间距
    private val mMargin = 40

    //x最小值
    private val xMin = mMargin

    //X最大值
    private var xMax = 0

    //y最小值
    private val yMin = 0

    //y最大值
    private var yMax = 0

    //直播间ID
    private var mProgramId = 0L
    private var display: View? = null
    private var videoView: SingleVideoView? = null
    private var ivClose: ImageView? = null
    private var ivQuiet: ImageView? = null

    //是否是竖屏模式
    private var mVertical = false

    private var startTime: Long = 0
    private var endTime: Long = 0

    private var isclick = false

    private var show = true

    //点击跳转直播间标识位
    private var jumpToPlayer = false

    //直播间草稿数据
    private var mDraft: String = ""

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        // 设置图片格式，效果为背景透明
        layoutParams.format = PixelFormat.RGBA_8888
//        Log.i("悬浮窗", "Build.VERSION.SDK_INT" + Build.VERSION.SDK_INT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // android 8.0及以后使用
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            // android 8.0以前使用
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE
        }
        layoutParams.gravity = Gravity.LEFT or Gravity.TOP
        //该flags描述的是窗口的模式，是否可以触摸，可以聚焦等
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
//        // 设置视频的播放窗口大小
//        layoutParams.width = 400
//        layoutParams.height = 550
        layoutParams.x = 660
        layoutParams.y = dip(53)
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mVertical = intent?.getBooleanExtra(ParamConstant.FLOATING_VERTICAL, false) ?: false
        if (mVertical) {
            layoutParams.width = dip(130)
            layoutParams.height = dip(231)
            yMax = ScreenUtils.getScreenHeight() - dip(231)
            xMax = ScreenUtils.getScreenWidth() - mMargin - dip(130)
        } else {
            layoutParams.width = dip(213)
            layoutParams.height = dip(160)
            yMax = ScreenUtils.getScreenHeight() - dip(160)
            xMax = ScreenUtils.getScreenWidth() - mMargin - dip(213)
        }

        if (show) {
            showFloatingWindow()
        }
        val coverUrl = intent?.getStringExtra(ParamConstant.PIC) ?: ""
        val playInfo = intent?.getStringExtra(ParamConstant.PLAY_INFO) ?: ""
//        videView?.play(playInfo, false)
        videoView?.showCover(coverUrl)
//        videView?.play("rtmp://aliyun-rtmp.51lm.tv/lingmeng/16611", false)
        mProgramId = intent?.getLongExtra(ParamConstant.PROGRAM_ID, 0) ?: 0
        SharedPreferencesUtils.commitLong(SPParamKey.PROGRAM_ID_IN_FLOATING, mProgramId)
        mDraft = intent?.getStringExtra(ParamConstant.Program_Draft) ?: ""
        UserHeartManager.setProgramId(mProgramId)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @SuppressLint("NewApi")
    private fun showFloatingWindow() {
        if (PermissionUtils.checkFloatPermission(this)) {
            val layoutInflater: LayoutInflater = LayoutInflater.from(this)
            display = layoutInflater.inflate(R.layout.view_floating, null)
            videoView = display?.findViewById(R.id.svv)
            val lp = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT
//            videoView?.let { vv ->
//                val margin = dp2px(1)
//                lp.setMargins(margin, margin, margin, margin)
//                (display as? FrameLayout)?.addView(vv, 0, lp)
//            }
//            sdvCover = display?.findViewById(R.id.sdv_cover)

//            videoView?.let {
//                clipViewCornerByDp(it,12f)
//            }

            ivClose = display?.findViewById(R.id.iv_close)
            ivQuiet = display?.findViewById(R.id.iv_quiet)
            ivClose?.setOnClickListener(this)
            ivQuiet?.setOnClickListener(this)

//            display?.setBackgroundColor(0)
//            display?.getBackground()?.setAlpha(0)
            windowManager?.addView(display, layoutParams)

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                videoView?.outlineProvider = SurfaceVideoViewOutlineProvider(dp2pxf(6));
                videoView?.clipToOutline = true;
            }


            display?.setOnTouchListener(FloatingOnTouchListener())
            display?.findViewById<View>(R.id.view_floating)?.setOnClickListener(this)
            show = false
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.iv_quiet -> {
                //静音
                val quietSelect = ivQuiet?.isSelected ?: false
                ivQuiet?.isSelected = !quietSelect
                if (ivQuiet?.isSelected == true) {
                    videoView?.soundOff()
                } else {
                    videoView?.soundOn()
                }
            }
            R.id.iv_close -> {
                //关闭
                FloatingManager.hideFloatingView()
                if (SPUtils.getBoolean(SPParamKey.First_Close_Floating, true)) {
                    val act = CommonInit.getInstance().getCurrentActivity() ?: return
                    if ("com.julun.huanque.activity.MainActivity".contains(act.localClassName)) {
                        //首次在首页关闭悬浮窗
                        SPUtils.commitBoolean(SPParamKey.First_Close_Floating, false)

                        MyAlertDialog(act).showAlertWithOKAndCancel(
                            "关闭直播间不希望小窗播放，可以在我的>设置>通用中关闭哦",
                            MyAlertDialog.MyDialogCallback(onRight = {
                                ARouter.getInstance().build(ARouterConstant.PLAYER_SETTING_ACTIVITY).navigation()
                            }), "设置提醒", "去设置"
                        )
                    }

                }
            }
            R.id.view_floating -> {
                //跳转直播间
                jumpToPlayer = true
                CommonInit.getInstance().getCurrentActivity()?.let { act ->
                    PlayerActivity.start(act, programId = mProgramId, from = PlayerFrom.FloatWindow,draft = mDraft)
                }

            }
            else -> {
            }
        }
    }


    // touch移动视频窗口 | 事件拦截
    private inner class FloatingOnTouchListener : View.OnTouchListener {
        private var x = 0f
        private var y = 0f
        override fun onTouch(view: View?, event: MotionEvent): Boolean {
            if (view == null) {
                return false
            }
            when (event.getAction()) {
                MotionEvent.ACTION_DOWN -> {
                    x = event.getRawX()
                    y = event.getRawY()
                    isclick = false //当按下的时候设置isclick为false
                    startTime = System.currentTimeMillis()
                }
                MotionEvent.ACTION_MOVE -> {
                    isclick = true //当按钮被移动的时候设置isclick为true
                    val nowX = event.getRawX()
                    val nowY = event.getRawY()
                    val movedX = nowX - x
                    val movedY = nowY - y
                    x = nowX
                    y = nowY
                    var tempX = (layoutParams.x + movedX).toInt()
                    if (tempX < xMin) {
                        tempX = xMin
                    } else if (tempX > xMax) {
                        tempX = xMax
                    }
                    layoutParams.x = tempX

                    var tempY = layoutParams.y + movedY.toInt()
                    if (tempY < yMin) {
                        tempY = yMin
                    } else if (tempY > yMax) {
                        tempY = yMax
                    }
                    layoutParams.y = tempY
                    windowManager?.updateViewLayout(view, layoutParams)
                }
                MotionEvent.ACTION_UP -> {
                    endTime = System.currentTimeMillis()
                    //当从点击到弹起小于半秒的时候,则判断为点击,如果超过则不响应点击事件
                    if (endTime - startTime > 0.1 * 1000L) {
                        isclick = true
                    } else {
                        isclick = false
                    }
                }
            }
            return isclick
        }
    }

    override fun onDestroy() {
        EventBus.getDefault().post(FloatingCloseEvent())
        // 移除浮动框
        SharedPreferencesUtils.commitLong(SPParamKey.PROGRAM_ID_IN_FLOATING, 0)

        if (windowManager != null && display != null) {
            if (!jumpToPlayer && !ActivitiesManager.INSTANCE.hasActivity("com.julun.huanque.core.ui.live.PlayerActivity")) {
                UserHeartManager.setProgramId(null)
                videoView?.stop()
                GlobalScope.launch {
                    kotlin.runCatching {
                        withContext(Dispatchers.IO) {
                            try {
                                Requests.create(LiveRoomService::class.java).leave(ProgramIdForm(mProgramId)).dataConvert()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }
            windowManager?.removeView(display)
        }
        super.onDestroy()
    }

    override fun getRequestCallerId() = StringHelper.uuid()

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun clipViewCornerByDp(view: View, pixel: Float) {
        view.clipToOutline = true
        view.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, pixel)
            }
        }
    }

}