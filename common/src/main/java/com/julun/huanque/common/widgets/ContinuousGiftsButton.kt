package com.julun.huanque.common.widgets

import android.content.Context
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import android.util.AttributeSet
import android.view.LayoutInflater
import com.julun.huanque.common.R
import com.julun.huanque.common.suger.show
import kotlinx.android.synthetic.main.view_continuous_gifts.view.*

/**
 * 连击礼物按钮
 * @author WanZhiYuan
 * @version 1.0
 * @createDate 2019/03/08
 */
class ContinuousGiftsButton(context: Context?, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {

//    private lateinit var mViewmodel: ContinuousGiftViewModel
//    private var mFrom: NewSendGiftForm? = null

    //默认按钮样式
//    private var mBtnStyle: String = BusiConstant.GiftButtonStyle.Yellow

    init {
        LayoutInflater.from(context).inflate(R.layout.view_continuous_gifts, this)
    }

    fun showButton(level: Int/*, icon: String*/) {
        this.show()
//        when (level) {
//            2 -> setButtonStyle(BusiConstant.GiftButtonStyle.Green)
//            3 -> setButtonStyle(BusiConstant.GiftButtonStyle.Blue)
//            4 -> setButtonStyle(BusiConstant.GiftButtonStyle.Orange)
//            5 -> setButtonStyle(BusiConstant.GiftButtonStyle.Violet)
//            else -> setButtonStyle(BusiConstant.GiftButtonStyle.Yellow)
//        }
        setButtonStyle(level)
        cbvGiftCircleView.setReverse(true)
//        cbvGiftCircleView.setAnimationTime(time)
//        ImageUtils.loadImage(sdvGiftIcon, icon, 41f, 41f)
//        initViewModel()
    }

    fun setListener(listener: CircleBarView.OnAnimationListener) {
        cbvGiftCircleView.setOnAnimationListener(listener)
    }

    private fun initViewModel() {
//        val activity = context as? PlayerActivity
//        activity?.let {
//            mViewmodel = ViewModelProviders.of(it).get(ContinuousGiftViewModel::class.java)
//            mViewmodel.levelStatue.observe(it, Observer {
//                it ?: return@Observer
//                when (it) {
//                    2 -> setButtonStyle(BusiConstant.GiftButtonStyle.Green)
//                    3 -> setButtonStyle(BusiConstant.GiftButtonStyle.Blue)
//                    4 -> setButtonStyle(BusiConstant.GiftButtonStyle.Orange)
//                    5 -> setButtonStyle(BusiConstant.GiftButtonStyle.Violet)
//                    else -> setButtonStyle(BusiConstant.GiftButtonStyle.Yellow)
//                }
//                cbvGiftCircleView.restartAnimation()
//            })
//        }
    }


    private fun setButtonStyle(level:Int) {
        when (level) {
            1->{
                cbvGiftCircleView.setColor(ContextCompat.getColor(context,R.color.gift_1_bg),
                        ContextCompat.getColor(context,R.color.gift_progress),
                        ContextCompat.getColor(context,R.color.gift_1_outside)
                )
                ivBtnSendGifts.setImageResource(R.drawable.btn_image_continuous_yellow)
            }
            2 -> {
                cbvGiftCircleView.setColor(ContextCompat.getColor(context,R.color.gift_2_bg),
                        ContextCompat.getColor(context,R.color.gift_progress),
                        ContextCompat.getColor(context,R.color.gift_2_outside)
                )
                ivBtnSendGifts.setImageResource(R.drawable.btn_image_continuous_green)
            }
            3 -> {
                cbvGiftCircleView.setColor(ContextCompat.getColor(context,R.color.gift_3_bg),
                        ContextCompat.getColor(context,R.color.gift_progress),
                        ContextCompat.getColor(context,R.color.gift_3_outside)
                )
                ivBtnSendGifts.setImageResource(R.drawable.btn_image_continuous_blue)
            }
            4 -> {
                cbvGiftCircleView.setColor(ContextCompat.getColor(context,R.color.gift_4_bg),
                        ContextCompat.getColor(context,R.color.gift_progress),
                        ContextCompat.getColor(context,R.color.gift_4_outside)
                )
                ivBtnSendGifts.setImageResource(R.drawable.btn_image_continuous_orange)
            }
            5 -> {
                cbvGiftCircleView.setColor(ContextCompat.getColor(context,R.color.gift_5_bg),
                        ContextCompat.getColor(context,R.color.gift_progress),
                        ContextCompat.getColor(context,R.color.gift_5_outside)
                )
                ivBtnSendGifts.setImageResource(R.drawable.btn_image_continuous_violet)
            }
            else -> {
                cbvGiftCircleView.setColor(ContextCompat.getColor(context,R.color.gift_1_bg),
                        ContextCompat.getColor(context,R.color.gift_progress),
                        ContextCompat.getColor(context,R.color.gift_1_outside)
                )
                ivBtnSendGifts.setImageResource(R.drawable.btn_image_continuous_yellow)
            }
        }
    }
}