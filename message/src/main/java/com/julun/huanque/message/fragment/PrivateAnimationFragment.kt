package com.julun.huanque.message.fragment

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.DialogInterface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.*
import android.view.animation.BounceInterpolator
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.bean.beans.ChatGift
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.common.utils.ScreenUtils
import com.julun.huanque.common.utils.StatusBarUtil
import com.julun.huanque.message.R
import com.julun.huanque.message.viewmodel.PrivateAnimationViewModel
import com.plattysoft.leonids.ParticleSystem
import com.trello.rxlifecycle4.android.FragmentEvent
import com.trello.rxlifecycle4.kotlin.bindUntilEvent
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import kotlinx.android.synthetic.main.fragment_private_animation.*
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

/**
 *@创建者   dong
 *@创建时间 2020/9/2 17:31
 *@描述 私信  用户播放动画的Fragment
 */
class PrivateAnimationFragment : BaseDialogFragment(), DialogInterface.OnKeyListener {

    private val mPrivateAnimationViewModel: PrivateAnimationViewModel by activityViewModels()

    //顶部栏高度
    private val mBarHeight = StatusBarUtil.getStatusBarHeight(CommonInit.getInstance().getContext())

    override fun getLayoutId() = R.layout.fragment_private_animation

    //不需要动画
    override fun needEnterAnimation() = false

    override fun initViews() {

    }

    override fun onResume() {
        super.onResume()
        sdv_gift_icon.hide()
        tv_gift_name.hide()
        sdv_gift.hide()
        con_root?.alpha = 1.0f

        initViewModel()
    }

    private fun initViewModel() {
        mPrivateAnimationViewModel.animationEndFlag.observe(this, Observer {
            if (it == true) {
                mPrivateAnimationViewModel.animationEndFlag.value = null
                mPrivateAnimationViewModel.dismissState.value = true
            }
        })

        mPrivateAnimationViewModel.voiceCompleteFlag.observe(this, Observer {
            if (it == true) {
                mPrivateAnimationViewModel.voiceCompleteFlag.value = null
                mPrivateAnimationViewModel.dismissState.value = true
            }
        })
        mPrivateAnimationViewModel.dismissState.observe(this, Observer {
            if (it == true) {
                mPrivateAnimationViewModel.dismissState.value = null
                startDismissAnimation()
            }
        })

        mPrivateAnimationViewModel.giftData.observe(this, Observer {
            if (it != null) {
                //开始播放动画
                playAnimaiton(it)
                //清空礼物效果
                mPrivateAnimationViewModel.giftData.value = null
            }
        })

    }

    override fun onStart() {
        super.onStart()
        dialog?.setOnKeyListener(this)
        setDialogSize(Gravity.CENTER, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
//        updateParams()
        //不需要半透明遮罩层
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    }

    /**
     * 设置布局
     */
    private fun updateParams() {
        val window = dialog?.window ?: return
        val params = window.attributes
        params.gravity = Gravity.CENTER
        params.width = ViewGroup.LayoutParams.MATCH_PARENT
        params.height = ScreenUtils.getScreenHeight() - mBarHeight
        window.attributes = params
    }

    /**
     * 播放动画
     */
    private fun playAnimaiton(gift: ChatGift) {
        val specialParams = gift.specialParams
        when (gift.specialType) {
            ChatGift.Sound -> {
                //音效类型
                sdv_gift_icon.show()
                tv_gift_name.show()

                sdv_gift_icon.loadImage(gift.pic, 80f, 80f)
                tv_gift_name.text = gift.giftName

                mPrivateAnimationViewModel.startPlayer()
                startScaleAnimaiton(sdv_gift_icon)
            }
            ChatGift.Screen -> {
                //飘屏类型
                val picArray = specialParams.pics.split(",")
                if (picArray.isEmpty()) {
                    mPrivateAnimationViewModel.dismissState.value = true
                    return
                }
                val count = if (specialParams.count == 0) {
                    20
                } else {
                    specialParams.count
                }
                if (specialParams.screenType == ChatGift.BothSide) {
                    //两侧飘屏
                    val picUrl = getRandomPicUrl(picArray)
                    ImageUtils.requestImageForBitmap(picUrl, {
                        val bitmapDrawable = BitmapDrawable(resources, it)
                        activity?.runOnUiThread { startSlideAnimation(bitmapDrawable, count / 2) }
                    })

                } else if (specialParams.screenType == ChatGift.TopDown) {
                    //从上往下飘屏
                    val totalCount = count
                    Observable.interval(0, 50, TimeUnit.MILLISECONDS)
                        .doOnSubscribe { removeCount = 0 }
                        .take(totalCount.toLong())
                        .observeOn(AndroidSchedulers.mainThread())
                        .bindUntilEvent(this, FragmentEvent.DESTROY_VIEW)
                        .subscribe({ startAnimation(totalCount, getRandomPicUrl(picArray)) }, {})
                }
            }
            ChatGift.Animation -> {
                //动画类型
                //播放音效
                mPrivateAnimationViewModel.startPlayer()
                //播放动画
                sdv_gift.show()
                ImageUtils.showAnimator(sdv_gift, specialParams.webpUrl)
            }
            else -> {
                //普通礼物动画
                sdv_gift_icon.show()
                tv_gift_name.show()

                sdv_gift_icon.loadImage(gift.pic, 80f, 80f)
                tv_gift_name.text = gift.giftName
                startScaleAnimaiton(sdv_gift_icon)
                //两秒以后关闭
                Observable.timer(2, TimeUnit.SECONDS)
                    .bindUntilEvent(this, FragmentEvent.DESTROY_VIEW)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ mPrivateAnimationViewModel.dismissState.value = true }, { it.printStackTrace() })
            }
        }
    }

    private var mAnimator: AnimatorSet? = null

    /**
     * 开始缩放动画
     */
    private fun startScaleAnimaiton(targetView: View) {
        mAnimator?.cancel()
        mAnimator = AnimatorSet()
        val xScaleAnimation = ObjectAnimator.ofFloat(targetView, "scaleX", 0.9f, 1.3f)
        val yScaleAnimation = ObjectAnimator.ofFloat(targetView, "scaleY", 0.9f, 1.3f)

        xScaleAnimation.apply {
            duration = 750
            interpolator = LinearInterpolator()
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
        }
        yScaleAnimation.apply {
            duration = 750
            interpolator = LinearInterpolator()
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
        }
        mAnimator?.playTogether(xScaleAnimation, yScaleAnimation)
        mAnimator?.start()
    }


    /**
     * 获取随机图片
     */
    private fun getRandomPicUrl(picArray: List<String>): String {
        if (picArray.isEmpty()) {
            return ""
        }
        val picSize = picArray.size
        return if (picSize == 1) {
            picArray[0]
        } else {
            val index = (Math.random() * (picSize - 1)).roundToInt()
            picArray[index]
        }

    }

    private var removeCount = 0

    /**
     * 开始掉落动画（单个视图）
     */
    private fun startAnimation(total: Int, url: String) {
        val border = 270
        val iv = SimpleDraweeView(context).apply {
            setImageURI(StringHelper.getOssImgUrl(url))
        }
//        val tempX = ScreenUtils.getScreenWidth() * (0.8 * Math.random() + 0.1)
        val tempX = (ScreenUtils.getScreenWidth() - border) * Math.random()
        val params = FrameLayout.LayoutParams(border, border)
        con_root.addView(iv, params)
        iv.x = tempX.toFloat()

        //开始动画
        val targetY = ScreenUtils.getScreenHeight() - mBarHeight * 2
//        val targetY = con_root.bottom
        logger.info("Private mBarHeight = ${mBarHeight},targetY = $targetY,screenHeight = ${ScreenUtils.getScreenHeight()}，con_root.bottom = ${con_root.bottom},con_root.height = ${con_root.height}")
        val yTranslateAnimator = ObjectAnimator.ofFloat(iv, "translationY", -300f, targetY.toFloat())

        val removeTranslateAnimator = ObjectAnimator.ofFloat(iv, "translationY", targetY.toFloat(), (targetY + border).toFloat())


        val time = System.currentTimeMillis()
        val index = time % 3

        val rotationAnimator: ObjectAnimator? = when (index) {
            0L -> {
                //正向转
                ObjectAnimator.ofFloat(iv, "rotation", 0f, 360f)
            }
            1L -> {
                //反向转
                ObjectAnimator.ofFloat(iv, "rotation", 0f, -360f)
            }
            else -> {
                //不转
                null
            }
        }



        yTranslateAnimator.apply {
            duration = 2500 + (1000 * Math.random()).toLong()
            interpolator = BounceInterpolator()
        }

        rotationAnimator?.apply {
            duration = 2000
            repeatCount = -1
            repeatMode = ValueAnimator.RESTART
            interpolator = LinearInterpolator()
        }

        removeTranslateAnimator.apply {
            duration = 200
            interpolator = LinearInterpolator()
        }


        removeTranslateAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                con_root?.removeView(iv)
                removeCount++
                rotationAnimator?.cancel()
                if (removeCount == total) {
                    //动画播放完成
                    mPrivateAnimationViewModel.animationEndFlag.value = true
                }
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
            }
        })

        val animatorSet = AnimatorSet()
        if (rotationAnimator != null) {
            animatorSet.play(yTranslateAnimator).with(rotationAnimator).before(removeTranslateAnimator)
        } else {
            animatorSet.play(yTranslateAnimator).before(removeTranslateAnimator)
        }
        animatorSet.start()

    }

    /**
     * 播放两侧抛出动画
     */
    private fun startSlideAnimation(drawable: Drawable, count: Int) {
        ParticleSystem(con_root, 100, drawable, 1500)
            .setSpeedModuleAndAngleRange(0.2f, 0.55f, 330, 360)
            .setRotationSpeed(1000f)
            .setAcceleration(0.002f, 90)
            .emit(-200, 400, count, 1000)

        ParticleSystem(con_root, 100, drawable, 1500)
            .setSpeedModuleAndAngleRange(0.2f, 0.55f, 180, 210)
            .setRotationSpeed(1000f)
            .setAcceleration(0.002f, 90)
            .emit(ScreenUtils.getScreenWidth() + 200, 400, count, 1000)
        val intervalTimer = (1000 + 1500).toLong()
        Observable.timer(intervalTimer, TimeUnit.MILLISECONDS)
            .bindUntilEvent(this, FragmentEvent.DESTROY_VIEW)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ mPrivateAnimationViewModel.dismissState.value = true }, { it.printStackTrace() })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        sdv_gift.hide()
        tv_gift_name.hide()
        mPrivateAnimationViewModel.stopPlayer()
        mAnimator?.cancel()
    }

    override fun onKey(dialog: DialogInterface?, keyCode: Int, event: KeyEvent?): Boolean {
        logger.info("Message keyCode = $keyCode")
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        } else {
            //这里注意当不是返回键时需将事件扩散，否则无法处理其他点击事件
            return false;
        }
    }

    private var mAlphaAnimator: Animator? = null

    /**
     * 开始隐藏动画
     */
    private fun startDismissAnimation() {
        mAlphaAnimator = mAlphaAnimator ?: ObjectAnimator.ofFloat(con_root, "alpha", 1.0f, 0.0f).apply {
            duration = 300
            interpolator = LinearInterpolator()
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?) {
                    dismiss()
                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationStart(animation: Animator?) {
                }

            })
        }
        mAlphaAnimator?.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        mPrivateAnimationViewModel.startConsumeGift(this)
    }


}