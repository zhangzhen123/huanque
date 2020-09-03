package com.julun.huanque.message.fragment

import android.animation.Animator
import android.animation.ObjectAnimator
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.ViewGroup
import android.view.animation.BounceInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.bean.beans.ChatGift
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.suger.dp2px
import com.julun.huanque.common.suger.dp2pxf
import com.julun.huanque.common.utils.GlobalUtils
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.common.utils.ScreenUtils
import com.julun.huanque.message.R
import com.julun.huanque.message.viewmodel.PrivateAnimationViewModel
import com.julun.huanque.message.viewmodel.PrivateConversationViewModel
import com.plattysoft.leonids.ParticleSystem
import com.trello.rxlifecycle4.android.ActivityEvent
import com.trello.rxlifecycle4.android.FragmentEvent
import com.trello.rxlifecycle4.kotlin.bindUntilEvent
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import kotlinx.android.synthetic.main.fragment_private_animation.*
import kotlinx.android.synthetic.main.item_header_conversions.*
import org.jetbrains.anko.imageResource
import java.lang.Math.round
import java.util.concurrent.TimeUnit
import kotlin.math.floor
import kotlin.math.roundToInt

/**
 *@创建者   dong
 *@创建时间 2020/9/2 17:31
 *@描述 私信  用户播放动画的Fragment
 */
class PrivateAnimationFragment : BaseDialogFragment() {

    private val mPrivateConversationViewModel: PrivateConversationViewModel by activityViewModels()

    private val mPrivateAnimationViewModel: PrivateAnimationViewModel by viewModels()

    override fun getLayoutId() = R.layout.fragment_private_animation

    //不需要对象
    override fun needEnterAnimation() = false

    override fun initViews() {

    }

    override fun onResume() {
        super.onResume()
        initViewModel()
    }

    private fun initViewModel() {
        mPrivateConversationViewModel.startAnimationFlag.observe(this, Observer {
            if (it != null) {
                //开始播放动画
                playAnimaiton(it)
                mPrivateConversationViewModel.startAnimationFlag.value = null
            }
        })

        mPrivateAnimationViewModel.animationEndFlag.observe(this, Observer {
            if (it == true) {
                mPrivateAnimationViewModel.animationEndFlag.value = null
                dismiss()
            }
        })
    }

    override fun onStart() {
        super.onStart()
        setDialogSize(Gravity.CENTER, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    /**
     * 播放动画
     */
    private fun playAnimaiton(gift: ChatGift) {
        val specialParams = gift.specialParams
        when (gift.specialType) {
            ChatGift.Sound -> {
                //音效类型
            }
            ChatGift.Screen -> {
                //飘屏类型
                val picArray = specialParams.pics.split(",")
                if (picArray.isEmpty()) {
                    return
                }
                if (specialParams.screenType == ChatGift.BothSide) {
                    //两侧飘屏
                    val picUrl = getRandomPicUrl(picArray)
                    ImageUtils.requestImageForBitmap(picUrl, {
                        val bitmapDrawable = BitmapDrawable(resources, it)
                        activity?.runOnUiThread { startSlideAnimation(bitmapDrawable) }
                    })

                } else if (specialParams.screenType == ChatGift.TopDown) {
                    //从上往下飘屏
                    val totalCount = 30
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
            }
            else -> {
            }
        }
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
        val iv = SimpleDraweeView(context).apply {
            setImageURI(StringHelper.getOssImgUrl(url))
        }
        val tempX = ScreenUtils.getScreenWidth() * (0.8 * Math.random() + 0.1)
        val params = FrameLayout.LayoutParams(dp2px(40), dp2px(40))
        con_root.addView(iv, params)
        iv.x = tempX.toFloat()

        //开始动画
        val targetY = ScreenUtils.getScreenHeight() - iv.bottom
        val yTranslateAnimator = ObjectAnimator.ofFloat(iv, "translationY", -dp2pxf(40), targetY.toFloat())

        yTranslateAnimator.apply {
            duration = 2500 + (1000 * Math.random()).toLong()
            interpolator = BounceInterpolator()
        }

        yTranslateAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                con_root.removeView(iv)
                removeCount++
                logger.info("Message removeCount = $removeCount")
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

        yTranslateAnimator.start()
    }

    /**
     * 播放两侧抛出动画
     */
    private fun startSlideAnimation(drawable: Drawable) {
        ParticleSystem(con_root, 100, drawable, 1500)
            .setSpeedModuleAndAngleRange(0.4f, 0.6f, 0, 30)
            .setRotationSpeed(600f)
            .setAcceleration(0.003f, 90)
            .emit(-100, 400, 3, 5000)

        ParticleSystem(con_root, 100, drawable, 1500)
            .setSpeedModuleAndAngleRange(0.4f, 0.6f, 150, 180)
            .setRotationSpeed(600f)
            .setAcceleration(0.003f, 90)
            .emit(1180, 400, 3, 5000)
        val intervalTimer = (5000 + 1500).toLong()
        Observable.timer(intervalTimer, TimeUnit.MILLISECONDS)
            .bindUntilEvent(this, FragmentEvent.DESTROY_VIEW)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ dismiss() }, { it.printStackTrace() })
    }


}