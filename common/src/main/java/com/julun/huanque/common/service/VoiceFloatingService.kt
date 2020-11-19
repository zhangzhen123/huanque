package com.julun.huanque.common.service

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Outline
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.*
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.alibaba.android.arouter.launcher.ARouter
import com.julun.huanque.common.R
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.constant.ConmmunicationUserType
import com.julun.huanque.common.constant.ParamConstant
import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.common.interfaces.VoiceChangeListener
import com.julun.huanque.common.manager.HuanViewModelManager
import com.julun.huanque.common.suger.dp2px
import com.julun.huanque.common.suger.logger
import com.julun.huanque.common.utils.TimeUtils
import com.julun.huanque.common.utils.permission.PermissionUtils
import com.julun.huanque.common.viewmodel.VoiceChatViewModel
import com.luck.picture.lib.tools.ScreenUtils
import org.jetbrains.anko.dip


/**
 *@创建者   dong
 *@创建时间 2020/8/5 16:39
 *@描述 悬浮窗使用的service
 */
class VoiceFloatingService : Service(), View.OnClickListener {

    private var windowManager: WindowManager? = null
    private var layoutParams: WindowManager.LayoutParams = WindowManager.LayoutParams()

    //距离屏幕的间距
    private val mMargin = 40

    private var display: View? = null

    private var isclick = false

    private var startTime: Long = 0
    private var endTime: Long = 0

    private var show = true

    private var mTvTime: TextView? = null

    private val mVoiceChatViewModel = HuanViewModelManager.mVoiceChatViewModel


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
        layoutParams.width = dp2px(74)
        layoutParams.height = dp2px(78)

        CommonInit.getInstance().getCurrentActivity()?.let { act ->
            layoutParams.x = ScreenUtils.getScreenWidth(act) - layoutParams.width
            layoutParams.y = ScreenUtils.getScreenHeight(act) - dp2px(120) - ScreenUtils.getStatusBarHeight(act)
        }

    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (show) {
            showFloatingWindow()
            //显示当前状态
            val currentState = mVoiceChatViewModel?.currentVoiceState?.value ?: ""
            val duration = mVoiceChatViewModel.duration.value ?: 0
            showView(currentState, duration)
            mVoiceChatViewModel.mVoiceChangeListener = object : VoiceChangeListener {
                override fun voiceChange(state: String, duration: Long) {
                    showView(state, duration)
                }

            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    /**
     * 显示当前视图
     */
    private fun showView(state: String, duration: Long) {
        if (state == VoiceChatViewModel.VOICE_ACCEPT) {
            //显示通话时长
            mTvTime?.text = TimeUtils.countDownTimeFormat1(duration)
        } else {
            //显示等待接通
            mTvTime?.text = "等待接通"
        }
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @SuppressLint("NewApi")
    private fun showFloatingWindow() {
        if (PermissionUtils.checkFloatPermission(this)) {
            val layoutInflater: LayoutInflater = LayoutInflater.from(this)
            display = layoutInflater.inflate(R.layout.view_voice_floating, null)
            mTvTime = display?.findViewById(R.id.tv_time)

            windowManager?.addView(display, layoutParams)

            display?.setOnTouchListener(FloatingOnTouchListener())
            display?.setOnClickListener(this)
            show = false
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.rl_voice_service -> {
                //跳转语音通话页面
                val bundle = Bundle()
                bundle.putString(ParamConstant.From_Floating, BusiConstant.True)
                ARouter.getInstance().build(ARouterConstant.VOICE_CHAT_ACTIVITY).with(bundle)
                    .navigation()
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
//                    if (tempX < xMin) {
//                        tempX = xMin
//                    } else if (tempX > xMax) {
//                        tempX = xMax
//                    }
                    layoutParams.x = tempX
                    logger("movedX = $movedX，event.getRawX() = ${event.getRawX()},tempX = $tempX")

                    var tempY = layoutParams.y + movedY.toInt()
//                    if (tempY < yMin) {
//                        tempY = yMin
//                    } else if (tempY > yMax) {
//                        tempY = yMax
//                    }
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
                    if (isclick) {
                        animationToSide()
                    }
                }
            }
            return isclick
        }
    }

    //缘分View 移动动画
    private var sideAnimator: ValueAnimator? = null

    /**
     * 缘分图标  动画移动到屏幕边侧
     */
    private fun animationToSide() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && mVoiceChatViewModel.mVoiceChangeListener != null) {
            val act = CommonInit.getInstance().getCurrentActivity() ?: return
            val screenMiddleX = ScreenUtils.getScreenWidth(act) / 2
            if (display?.isAttachedToWindow != true) {
                return
            }
            val viewMiddleX = layoutParams.x + (display?.width ?: 0) / 2
            val targetX = if (viewMiddleX > screenMiddleX) {
                //View在屏幕右侧
                ScreenUtils.getScreenWidth(act)
            } else {
                //View在屏幕左侧
                0
            }
            sideAnimator?.cancel()
            sideAnimator = ValueAnimator.ofInt(layoutParams.x, targetX)

            sideAnimator?.addUpdateListener {
                layoutParams.x = (it.animatedValue as? Int) ?: 0
                windowManager?.updateViewLayout(display!!, layoutParams)
            }
            sideAnimator?.duration = 200
            sideAnimator?.start()
        }

    }

    override fun onDestroy() {
        if (display != null) {
            windowManager?.removeView(display)
        }
        mVoiceChatViewModel?.mVoiceChangeListener = null
        super.onDestroy()
    }


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