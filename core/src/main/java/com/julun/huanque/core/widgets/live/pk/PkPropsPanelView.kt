package com.julun.huanque.core.widgets.live.pk

import android.animation.ValueAnimator
import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.julun.huanque.common.bean.beans.PkGiftTaskInfo
import com.julun.huanque.common.bean.beans.PkPropInfo
import com.julun.huanque.common.bean.beans.PkScoreTaskInfo
import com.julun.huanque.common.constant.PKPropStatus
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.loadImage
import com.julun.huanque.common.suger.setTFDinCdc2
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.ULog
import com.julun.huanque.core.R
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.android.synthetic.main.view_pk_props_layout.view.*
import java.util.concurrent.TimeUnit


/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2019/8/21 16:29
 *
 *@Description : pk显示道具信息面板 关联[PkMicView]
 *
 */
class PkPropsPanelView : RelativeLayout {

    private val logger = ULog.getLogger("PkPropsPanelView")

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    init {
        LayoutInflater.from(context).inflate(R.layout.view_pk_props_layout, this)
        pk_props_process_text01.setTFDinCdc2()
        pk_props_process_text02.setTFDinCdc2()
    }
    private var giftHideDispose: Disposable? = null

    private var giftCdDispose: Disposable? = null
    //显示礼物任务进行中
    fun showGiftTasking(data: PkGiftTaskInfo) {
        showContainer()
//        pk_props_ing.show()
        changePropIng(false)
        tv_pk_props_award.hide()
        process_pk_props.hide()
        tv_pk_task_bottom.show()
        val time = data.ttl
        val totalTime=data.maxTtl.toInt()
        pb_pk_task_time.max = totalTime

        if (propCdDispose?.isDisposed == false) {
            propCdDispose?.dispose()
        }

        if (giftCdDispose?.isDisposed == false) {
            giftCdDispose?.dispose()
        }
        tv_pk_task_title.text = "礼物任务(${time}s)"
        giftCdDispose = Observable.intervalRange(1, time, 0, 1L, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe {
            tv_pk_task_title.text = "礼物任务(${time - it}s)"
            pb_pk_task_time.progress = (time - it).toInt()
        }
        sdv_pk_props_gift.loadImage(data.giftPic, 35f, 30f)
        tv_pk_props_gift.text = "${data.giftName}\n(${data.receiveCnt}/${data.needCnt})"


        tv_pk_task_bottom.text = "额外获得${data.multiple}倍PK积分"
    }


    //显示礼物任务结果
    fun showGiftTaskResult(success: Boolean, data: PkGiftTaskInfo) {
        showContainer()
//        pk_props_ing.hide()
        changePropIng(true)
        tv_pk_props_award.show()
        process_pk_props.hide()
        tv_pk_task_bottom.show()
        if(giftCdDispose?.isDisposed==false){
            giftCdDispose?.dispose()
        }
        if (propCdDispose?.isDisposed == false) {
            propCdDispose?.dispose()
        }
        //重置进度条
        pb_pk_task_time.progress=0
        if (success) {
            tv_pk_task_title.text = context.getString(R.string.pk_gift_task_success)
            val score = "${data.awardScore}积分"
            val sp = SpannableStringBuilder("任务额外奖励$score")
            val colorY = ContextCompat.getColor(context, R.color.colorAccent_lib)
            val foregroundColorSpan = ForegroundColorSpan(colorY)
            sp.setSpan(foregroundColorSpan, 6, sp.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv_pk_props_award.text = sp
        } else {
            tv_pk_task_title.text = context.getString(R.string.pk_gift_task__fail)
            tv_pk_props_award.text = context.getString(R.string.pk_props_award_fail)
        }
        tv_pk_task_bottom.text = "额外获得${data.multiple}倍PK积分"
        delayToHide()
    }


    //显示积分任务进行中
    fun showScoreTasking(data: PkScoreTaskInfo) {
        showContainer()
//        pk_props_ing.hide()
        changePropIng(true)
        tv_pk_props_award.show()
        process_pk_props.hide()
        tv_pk_task_bottom.show()
        val time = data.ttl
        val totalTime=data.maxTtl.toInt()
        pb_pk_task_time.max = totalTime

        if (propCdDispose?.isDisposed == false) {
            propCdDispose?.dispose()
        }

        if (giftCdDispose?.isDisposed == false) {
            giftCdDispose?.dispose()
        }
        tv_pk_task_title.text = "PK积分任务(${time}s)"
        giftCdDispose = Observable.intervalRange(1, time, 0, 1L, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe {
            tv_pk_task_title.text = "PK积分任务(${time - it}s)"
            pb_pk_task_time.progress = (time - it).toInt()
        }
        val score = "${data.nowScore}/${data.needScore}"
        val content="PK积分任务\n$score"
        val sp = SpannableStringBuilder(content)
        val colorY = ContextCompat.getColor(context, R.color.colorAccent_lib)
        val foregroundColorSpan = ForegroundColorSpan(colorY)
        sp.setSpan(foregroundColorSpan, 6, content.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv_pk_props_award.text = sp


        tv_pk_task_bottom.text = "额外获得${data.multiple}倍PK积分"
    }


    /**
     *  显示积分任务结果 [success]是否成功完成任务
     */

    fun showScoreTaskResult(success: Boolean, data: PkScoreTaskInfo) {
        showContainer()
//        pk_props_ing.hide()
        changePropIng(true)
        tv_pk_props_award.show()
        process_pk_props.hide()
        tv_pk_task_bottom.show()
        if(giftCdDispose?.isDisposed==false){
            giftCdDispose?.dispose()
        }
        if (propCdDispose?.isDisposed == false) {
            propCdDispose?.dispose()
        }
        //重置进度条
        pb_pk_task_time.progress=0
        if (success) {
            tv_pk_task_title.text = context.getString(R.string.pk_score_task_success)
            val score = "${data.awardScore}积分"
            val sp = SpannableStringBuilder("任务额外奖励\n$score")
            val colorY = ContextCompat.getColor(context, R.color.colorAccent_lib)
            val foregroundColorSpan = ForegroundColorSpan(colorY)
            sp.setSpan(foregroundColorSpan, 6, sp.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv_pk_props_award.text = sp
        } else {
            tv_pk_task_title.text = context.getString(R.string.pk_score_task__fail)
            tv_pk_props_award.text = context.getString(R.string.pk_props_award_fail)
        }
        tv_pk_task_bottom.text = "额外获得${data.multiple}倍PK积分"
        delayToHide()
    }
    private var propCdDispose: Disposable? = null
    //显示道具抢夺进行中
    fun showPropGrabIng(data: PkPropInfo) {
        showContainer()
        logger.info("道具争夺中：$data")
//        pk_props_ing.show()
        changePropIng(false)
        tv_pk_props_award.hide()
        process_pk_props.show()
        tv_pk_task_bottom.hide()
        val time = data.ttl
        val totalTime=data.maxTtl.toInt()
        pb_pk_task_time.max = totalTime
        if(giftCdDispose?.isDisposed==false){
            giftCdDispose?.dispose()
        }
        if (propCdDispose?.isDisposed == false) {
            propCdDispose?.dispose()
        }
        tv_pk_task_title.text = "道具争夺(${time}s)"
        propCdDispose = Observable.intervalRange(1, time, 0, 1L, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe {
            tv_pk_task_title.text = "道具争夺(${time - it}s)"
            pb_pk_task_time.progress = (time - it).toInt()
        }
        val propImg = data.propPic
        sdv_pk_props_gift.loadImage(propImg, 35f, 30f)
        tv_pk_props_gift.text = "特殊道具\n(${data.propName})"
    }


    //显示道具抢夺结果
    fun showPropGrabResult(success: Boolean, data: PkPropInfo) {
        showContainer()
//        pk_props_ing.hide()
        changePropIng(true)
        tv_pk_props_award.show()
        process_pk_props.show()
        tv_pk_task_bottom.hide()

        if(giftCdDispose?.isDisposed==false){
            giftCdDispose?.dispose()
        }
        if (propCdDispose?.isDisposed == false) {
            propCdDispose?.dispose()
        }
        pb_pk_task_time.progress=0
        var name = data.nickname
        if (name.length > 5) {
            name = "${name.substring(0, 4)}…"
        }
        if (data.status == PKPropStatus.UnUse) {
            if (success) {
                tv_pk_task_title.text = context.getString(R.string.pk_props_grab_success)
                val sp = SpannableStringBuilder("我方${name}\n抢到道具")
                val colorY = ContextCompat.getColor(context, R.color.colorAccent_lib)
                val foregroundColorSpan = ForegroundColorSpan(colorY)
                sp.setSpan(foregroundColorSpan, 2, 2 + name.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                tv_pk_props_award.text = sp
            } else {
                tv_pk_task_title.text = context.getString(R.string.pk_props_grab_fail)
                val sp = SpannableStringBuilder("对方${name}\n抢到道具")
                val colorY = ContextCompat.getColor(context, R.color.colorAccent_lib)
                val foregroundColorSpan = ForegroundColorSpan(colorY)
                sp.setSpan(foregroundColorSpan, 2, 2 + name.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                tv_pk_props_award.text = sp
            }
        } else if (data.status == PKPropStatus.Fail) {
            tv_pk_task_title.text = context.getString(R.string.pk_props_grab_fail)
            //无人抢夺
            tv_pk_props_award.text = context.getString(R.string.pk_props_grab_none)
        }
    }

    /**
     * 刷新道具抢夺分值
     */
    fun notifyPropValue(lastS1: Long, lastS2: Long, s1: Long, s2: Long) {
        process_pk_props.show()
        pk_props_process_text01.setNumberString("$lastS1", "$s1")
        pk_props_process_text02.setNumberString("$lastS2", "$s2")
        val start: Float = if (lastS1 == 0L || lastS2 == 0L) {
            (lastS1.toFloat() + 1) / (lastS2 + 1)
        } else {
            lastS1.toFloat() / lastS2
        }
        val end: Float = if (s1 == 0L || s2 == 0L) {
            (s1.toFloat() + 1) / (s2 + 1)
        } else {
            s1.toFloat() / s2
        }
        val lp1 = pk_props_process_01.layoutParams as ConstraintLayout.LayoutParams
        //score1/score2得出是score2的多少倍
        //有变化时才做动画
        if (end != start) {
            val animator21 = ValueAnimator.ofFloat(start, end)
            //时间比PK条变化短一点 防止混乱
            animator21.duration = 200L
            animator21.addUpdateListener { value ->
                logger.info("道具进度的权重刷新${value.animatedValue}")
                lp1.horizontalWeight = value.animatedValue as Float
                pk_props_process_01.requestLayout()
            }
            animator21.start()
        }
    }

    private fun showContainer(){
        if (giftHideDispose?.isDisposed == false) {
            giftHideDispose?.dispose()
        }
        if (this.visibility == View.GONE) {
            this.show()
        }
    }
    private fun changePropIng(hide:Boolean){
        if(hide){
            sdv_pk_props_gift.hide()
            tv_pk_props_gift.hide()
        }else{
            sdv_pk_props_gift.show()
            tv_pk_props_gift.show()
        }

    }
    fun delayToHide(callback: () -> Unit = {}) {
        if (giftCdDispose?.isDisposed == false) {
            giftHideDispose?.dispose()
        }
        giftHideDispose = Observable.timer(5, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe {
            this.hide()
            callback()
        }
    }
    fun reset(){
        pk_props_process_text01.text = "0"
        pk_props_process_text02.text = "0"
        val lp1 = pk_props_process_01.layoutParams as ConstraintLayout.LayoutParams
        lp1.horizontalWeight=1f

    }
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (giftCdDispose?.isDisposed == false) {
            giftCdDispose?.dispose()
        }
        if (giftHideDispose?.isDisposed == false) {
            giftHideDispose?.dispose()
        }

        if (propCdDispose?.isDisposed == false) {
            propCdDispose?.dispose()
        }

    }
}
