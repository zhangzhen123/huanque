package com.julun.huanque.core.widgets.live

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.bean.beans.BirdLiveAward
import com.julun.huanque.common.bean.beans.BottomActionBean
import com.julun.huanque.common.constant.BirdTaskAwardType
import com.julun.huanque.common.constant.ClickType
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.loadImageLocal
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.common.utils.ULog
import com.julun.huanque.core.R
import com.julun.huanque.core.ui.live.PlayerActivity
import com.julun.huanque.core.ui.live.PlayerViewModel
import com.julun.huanque.core.ui.main.bird.BirdGotMoneyDialogFragment
import com.julun.huanque.core.ui.main.bird.BirdTaskViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.android.synthetic.main.view_live_bird_time_counter.view.*
import java.util.concurrent.TimeUnit


/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/9/14 14:04
 *
 *@Description: 直播间倒计时领取养鹊奖励控件
 *
 */
class BirdAwardCounterView : ConstraintLayout {

    private val logger = ULog.getLogger("BirdAwardCounterView")

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private var mPlayerViewModel: PlayerViewModel? = null
    private var mBirdTaskViewModel: BirdTaskViewModel? = null

    private var hasInflate:Boolean=false
    private fun initView(){
        if(hasInflate){
            logger.info("已经初始化过了")
            return
        }
        LayoutInflater.from(context).inflate(R.layout.view_live_bird_time_counter, this)
        val act = context as? PlayerActivity
        if (act != null) {
            mPlayerViewModel = ViewModelProvider(act).get(PlayerViewModel::class.java)
            mBirdTaskViewModel = ViewModelProvider(act).get(BirdTaskViewModel::class.java)

            mBirdTaskViewModel?.receiveTaskResult?.observe(act, Observer {
                if (it.isSuccess()) {
                    mPlayerViewModel?.actionBeanData?.value = BottomActionBean().apply {
                        this.type = ClickType.BIRD
                    }
                    postDelayed({
                        val content = "恭喜您成功领取了\n ${StringHelper.formatBigNum(it.requireT().awardCoins)}金币"
                        val dialog = BirdGotMoneyDialogFragment.newInstance(content)
                        dialog.show(act, "BirdGotMoneyDialogFragment")
                    }, 500L)
                    this@BirdAwardCounterView?.hide()
                } else if (it.state == NetStateType.ERROR) {
                    ToastUtils.show("${it.error?.busiMessage}")
                }
                isEnabled = true
                canReceive=false
            })
        }
        onClickNew {
            if (canReceive) {
                isEnabled = false
                mBirdTaskViewModel?.receiveTask(currentBirdLiveAward?.taskCode ?: return@onClickNew)
            }
        }
        hasInflate=true
    }

    private var propCdDispose: Disposable? = null

    //
    private var time: Long = 0
    private var canReceive=false
    private var currentBirdLiveAward: BirdLiveAward? = null
    fun showCounting(info: BirdLiveAward) {
        this.show()
        initView()
        currentBirdLiveAward = info
        val totalTime = info.time
        if (propCdDispose?.isDisposed == false) {
            propCdDispose?.dispose()
        }
        tv_tips.text = "$totalTime"
        propCdDispose =
            Observable.intervalRange(1, totalTime, 2, 1L, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe {
                time = totalTime - it
                if (time <= 0L) {
                    mPlayerViewModel?.watchLiveEnd()
                    canReceive=true
                    tv_tips.text = "领取"
                } else {
                    tv_tips.text = "$time"
                }
            }
        when (info.awardType) {
            BirdTaskAwardType.Small -> {
                sdv_coins.loadImageLocal(R.mipmap.icon_bird_coin_little)
                tv_title.text = "少量金币"
            }
            BirdTaskAwardType.Middle -> {
                sdv_coins.loadImageLocal(R.mipmap.icon_bird_coin_middle)
                tv_title.text = "中量金币"
            }

            BirdTaskAwardType.Big -> {
                sdv_coins.loadImageLocal(R.mipmap.icon_bird_coin_big)
                tv_title.text = "大量金币"
            }
        }

    }

    fun resetView() {
        if (propCdDispose?.isDisposed == false) {
            propCdDispose?.dispose()
        }
        currentBirdLiveAward = null
        time = 0
        hide()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (propCdDispose?.isDisposed == false) {
            propCdDispose?.dispose()
        }

    }
}
