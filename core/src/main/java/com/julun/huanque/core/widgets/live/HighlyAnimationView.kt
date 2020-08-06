package com.julun.huanque.core.widgets.live

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.julun.huanque.common.bean.beans.AnimModel
import com.julun.huanque.common.constant.AnimationTypes
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.helper.reportCrash
import com.julun.huanque.common.utils.JsonUtil
import com.julun.huanque.common.utils.ULog
import com.julun.huanque.core.R
import org.jetbrains.anko.dimen


/**
 * 豪礼动画或浮层提示视图
 * Created by djp on 2016/11/29.
 */
class HighlyAnimationView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {
    private val logger = ULog.getLogger("HighlyAnimationView")

    private var lastAnim: AnimModel? = null

    // 显示动画时长1秒
    private val SHOW_DURATION = 500L
    // 隐藏动画时长0.6秒
    private val HIDE_DURATION = 500L

    private var headerHeight: Int = 0
    private var playerHeight: Int = 0
    private var bottomHeight: Int = 0

    private val FIXED_ANIM_WIDTH by lazy { dimen(R.dimen.live_layer_anim_height) }
    private val GUARD_ANIM_WIDTH by lazy { dimen(R.dimen.guard_layer_anim_width) }
    private val GUARD_ANIM_HEIGHT by lazy { dimen(R.dimen.guard_layer_anim_height) }
    private  lateinit var createHighlyView: CreateHighlyView

    init {
//        CreateHighlyView.initContext(context)
        createHighlyView= CreateHighlyView(context)
        logger.info("初始化高级动画控件")
    }

    fun initWithHeight(headerHeight: Int, playerHeight: Int, bottomHeight: Int) {
        this.headerHeight = headerHeight
        this.playerHeight = playerHeight
        this.bottomHeight = bottomHeight
        logger.info("初始化高级动画")
    }

    // 守护动画显示位置和其他动画显示位置
    private fun setAnimHeight() {
        var lp = this.layoutParams as FrameLayout.LayoutParams
        if (lastAnim!!.animType == AnimationTypes.OPEN_GUARD) {
            // 守护动画 居中显示
            lp.width = GUARD_ANIM_WIDTH
            lp.height = GUARD_ANIM_HEIGHT
            lp.gravity = Gravity.CENTER
        } else {
            // 普通固定动画(开通守护、升级), 居中显示
            lp.width = FIXED_ANIM_WIDTH
            lp.height = FIXED_ANIM_WIDTH
            lp.gravity = Gravity.CENTER
//            lp.gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
        }
        this.layoutParams = lp
    }

    // 接收通知：消息队列中没有动画了
    fun handleAnimationClearNotify() {
        logger.info("没有动画了")
//        Single.just(AnimModel()).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe { _ ->
//            hideAndRemoveContentViews {
//                this.clearAnimation()
//            }
//        }
        hideAndRemoveContentViews {
            this.clearAnimation()
        }
    }

    // 处理收到的动画事件分发
    fun handleEventNotify(newAnimConfig: AnimModel) {
//        Single.just(newAnimConfig).compose(RunOnMainSchedulerTransformer()).subscribe { model ->
//            this.hideAndRemoveContentViews {
//                logger.info("收到动画事件")
//                lastAnim = model
//                showAnim()
//            }
//        }
        this.hideAndRemoveContentViews {
            logger.info("收到动画事件")
            lastAnim = newAnimConfig
            showAnim()
        }
    }

    // 隐藏当前动画视图
    private fun hideAndRemoveContentViews(callback: () -> Unit = {}) {
        if (this.visibility == View.VISIBLE) {
            // 当前视图显示状态，先隐藏完成后再回调
            var anim = ObjectAnimator.ofFloat(this, View.ALPHA, 1f, 0f)
            anim.duration = HIDE_DURATION
            anim.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    this@HighlyAnimationView.visibility = View.GONE
                    this@HighlyAnimationView.alpha = 0f
                    if (this@HighlyAnimationView.childCount > 0) {
                        this@HighlyAnimationView.removeAllViews()
                    }
                    callback()
                }
            })
            anim.start()
        } else {
            if (this.childCount > 0) {
                this.removeAllViews()
            }
            callback()
        }
    }
    var showTime=3000L//如果有奖励展示时间延长
    // 播放动画：从隐藏到显示 从透明0到不透明1
    private fun playAnim(view: View) {
        logger.info("开始播放 alpha=0")
        this.alpha = 0f
        //添加判断 防止还没移除子view
        var parent = view.parent
        if (parent != null && parent is ViewGroup)
            parent.removeView(view)

        this.addView(view)
        this.visibility = View.VISIBLE
        var anim = ObjectAnimator.ofFloat(this, View.ALPHA, 0f, 1f)
        anim.duration = SHOW_DURATION
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                this@HighlyAnimationView.alpha = 1f
                // 播放4秒后隐藏，不放在缓存计时器中，能更准确掌握时长
                var animHidden = ObjectAnimator.ofFloat(this@HighlyAnimationView, View.ALPHA, 1f, 0f)
                animHidden.startDelay = showTime
                logger.info("$showTime 秒后隐藏")
                animHidden.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        this@HighlyAnimationView.alpha = 0f
                        this@HighlyAnimationView.visibility = View.GONE
                    }
                })
                animHidden.start()
            }
        })
        anim.start()
    }

    private fun showAnim() {

        if (lastAnim == null || StringHelper.isEmpty(lastAnim?.animType.toString())) {
            reportCrash("需要播放高阶动画的时候,发现要播放的动画为null或者播放的动画类型为null , 数据: ${JsonUtil.serializeAsString(lastAnim)}")
            return
        }
        showTime=3000L
        setAnimHeight()
        when (lastAnim!!.animType) {
        // 显示幸运礼物动画
            AnimationTypes.LUCKY -> playAnim(createHighlyView.viewWithLuckGift(lastAnim!!))
        // 显示开通守护动画
            AnimationTypes.OPEN_GUARD -> playAnim(createHighlyView.viewWithOpenGuard(lastAnim!!))
        //显示超级幸运礼物
            AnimationTypes.SUPER_LUCK_GIFT -> playAnim(createHighlyView.viewWithSuperLuckGift(lastAnim!!))
        // GIF动画  已经在AnimationFragment单独处理2017/4/11
//            AnimationTypes.GIF -> showGIFAnim()
        // 显示用户升级动画
            AnimationTypes.USER_UPGRADE -> {
//                if(lastAnim!!.extraObject["grantInfo"]!=null){
//                    showTime=5000L
//                }
                playAnim(createHighlyView.viewWithUser(lastAnim!!))
            }
        // 显示主播升级动画
            AnimationTypes.ANCHOR_UPGRADE -> playAnim(createHighlyView.viewWithAnchor(lastAnim!!))
            AnimationTypes.PNG, AnimationTypes.SWF -> {
            }
            else -> {
                logger.info("动画类型错误 animType=${lastAnim!!.animType}")
            }
        }
    }

    // 显示gif动画
    private fun showGIFAnim() {
        // 暂时只处理GIF动画(忽略height属性，高度自适应，只处理width)
        if (lastAnim == null) {   //如果 lastAnim 为空,直接返回
            reportCrash("播放动画的时候,动画对象无法获取 lastAnim ==null ", NullPointerException(" lastAnim == null "))
            return
        }
        if (lastAnim!!.animType == AnimationTypes.GIF) {
            if (this.childCount > 0) {
                this.removeAllViews()
            }
//            this.showErrorAlertView(123,"没啥意思的测试错误提示")
//            println("刚刚应该弹出提示框的")
            try {
                this.addView(createHighlyView.viewWithGIF(lastAnim!!))
                this.alpha = 1f
                this.visibility = View.VISIBLE
            } catch (e: Exception) {
                e.printStackTrace()
                //抓住异常
                reportCrash("播放动画的时候出错,这个错误虽然不会崩溃,但是需要重视!!!!!!!需要重视!!!!!!!重视!!!!!!!", e)
//                throw e
            }

        } else {
            logger.info("未能解析的动画类型")
            reportCrash("未能解析的动画类型", Exception("接收到的动画类型是 <${lastAnim?.animType}> ,未能识别"))
        }
    }

    fun clearData() {
        lastAnim = null
        this.clearAnimation()
        this.removeAllViews()
    }

    fun destoryResources() {
        clearData()
        createHighlyView.destoryResource()
        logger.info("清空所有数据 ${this.childCount}")
    }

}