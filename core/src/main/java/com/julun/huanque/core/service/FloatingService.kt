package com.julun.huanque.core.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.constant.ParamConstant
import com.julun.huanque.common.constant.PlayerFrom
import com.julun.huanque.common.constant.SPParamKey
import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.common.manager.UserHeartManager
import com.julun.huanque.common.suger.dp2pxf
import com.julun.huanque.common.suger.logger
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.common.utils.SharedPreferencesUtils
import com.julun.huanque.common.utils.permission.PermissionUtils
import com.julun.huanque.core.R
import com.julun.huanque.core.manager.FloatingManager
import com.julun.huanque.core.ui.live.PlayerActivity
import com.julun.huanque.core.widgets.SingleVideoView
import com.julun.huanque.core.widgets.SurfaceVideoViewOutlineProvider
import org.jetbrains.anko.dip


/**
 *@创建者   dong
 *@创建时间 2020/8/5 16:39
 *@描述 悬浮窗使用的service
 */
class FloatingService : Service(), View.OnClickListener {

    private var windowManager: WindowManager? = null
    private var layoutParams: WindowManager.LayoutParams = WindowManager.LayoutParams()

    //直播间ID
    private var mProgramId = 0L
    private var display: View? = null
    private var videView: SingleVideoView? = null
    private var ivClose: ImageView? = null
    private var ivQuiet: ImageView? = null

    //是否是竖屏模式
    private var mVertical = false

    private var startTime: Long = 0
    private var endTime: Long = 0

    private var isclick = false

    private var show = true

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        // 设置图片格式，效果为背景透明
        layoutParams.format = PixelFormat.RGB_565
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
        layoutParams.x = 700
        layoutParams.y = dip(53)
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mVertical = intent?.getBooleanExtra(ParamConstant.FLOATING_VERTICAL, false) ?: false
        if (mVertical) {
            layoutParams.width = dip(130)
            layoutParams.height = dip(231)
        } else {
            layoutParams.width = dip(213)
            layoutParams.height = dip(160)
        }

        if (show) {
            showFloatingWindow()
        }
        val coverUrl = intent?.getStringExtra(ParamConstant.PIC) ?: ""
        val playInfo = intent?.getStringExtra(ParamConstant.PLAY_INFO) ?: ""
//        videView?.play(playInfo, false)
        videView?.showCover(coverUrl)
        videView?.play("rtmp://aliyun-rtmp.51lm.tv/lingmeng/16611", false)
        mProgramId = intent?.getLongExtra(ParamConstant.PROGRAM_ID, 0) ?: 0
        SharedPreferencesUtils.commitLong(SPParamKey.PROGRAM_ID_IN_FLOATING, mProgramId)
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
            videView = display?.findViewById(R.id.video_view)
//            sdvCover = display?.findViewById(R.id.sdv_cover)
//            videView?.outlineProvider = SurfaceVideoViewOutlineProvider(dp2pxf(6));
//            videView?.clipToOutline = true;

            ivClose = display?.findViewById(R.id.iv_close)
            ivQuiet = display?.findViewById(R.id.iv_quiet)
            ivClose?.setOnClickListener(this)
            ivQuiet?.setOnClickListener(this)

//            display?.setBackgroundColor(0)
//            display?.getBackground()?.setAlpha(0)
            windowManager?.addView(display, layoutParams)
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
                    videView?.soundOff()
                } else {
                    videView?.soundOn()
                }
            }
            R.id.iv_close -> {
                //关闭
                FloatingManager.hideFloatingView()
            }
            R.id.view_floating -> {
                //跳转直播间
                CommonInit.getInstance().getCurrentActivity()?.let { act ->
                    PlayerActivity.start(act, programId = mProgramId, from = PlayerFrom.FloatWindow)
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
                    layoutParams?.x = (layoutParams?.x + movedX).toInt()
                    layoutParams.y = layoutParams.y + movedY.toInt()
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
        // 移除浮动框
        SharedPreferencesUtils.commitLong(SPParamKey.PROGRAM_ID_IN_FLOATING, 0)
        UserHeartManager.setProgramId(null)
        if (windowManager != null && display != null) {
            videView?.stop()
            windowManager?.removeView(display)
        }
        super.onDestroy()
    }
}