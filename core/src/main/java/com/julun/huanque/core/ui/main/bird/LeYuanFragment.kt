package com.julun.huanque.core.ui.main.bird

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.RectF
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnticipateOvershootInterpolator
import android.view.animation.BounceInterpolator
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.animation.addListener
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.base.BaseVMFragment
import com.julun.huanque.common.basic.NetState
import com.julun.huanque.common.bean.beans.BirdHomeInfo
import com.julun.huanque.common.bean.beans.CombineResult
import com.julun.huanque.common.bean.beans.UnlockUpgrade
import com.julun.huanque.common.bean.beans.UpgradeBirdBean
import com.julun.huanque.common.constant.IntentParamKey
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.manager.audio.AudioPlayerManager
import com.julun.huanque.common.manager.audio.SoundPoolManager
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.core.R
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_leyuan.*
import org.jetbrains.anko.sdk23.listeners.onTouch
import java.util.concurrent.TimeUnit

/**
 *@创建者   dong
 *@创建时间 2020/6/29 18:06
 *@描述 乐园Fragment
 */
class LeYuanFragment : BaseVMFragment<LeYuanViewModel>() {
    companion object {
        fun newInstance() = LeYuanFragment()
        //一些播放的音频短
        const val BIRD_BUY = "bird/bird_buy.mp3"
        const val BIRD_COIN = "bird/bird_coin.mp3"
        const val BIRD_COMBINE = "bird/bird_combine.mp3"
    }


    var programId: Long? = null
    override fun getLayoutId() = R.layout.fragment_leyuan
    private val birdAdapter: BirdAdapter by lazy { BirdAdapter() }
    private var currentCombineItem: UpgradeBirdBean? = null
    private var currentTargetItem: UpgradeBirdBean? = null

    //记录目标item的holder 执行合并动画时用到
    private var currentTargetViewHolder: BaseViewHolder? = null

    //用来播放背景音乐
    private val audioPlayerManager: AudioPlayerManager by lazy { AudioPlayerManager(requireContext()) }
//    private lateinit var soundPool: SoundPool
//    private var soundIdMap = hashMapOf<String, Int>()

    //记录操作的升级鹊原始的位置坐标
    private var originXY: IntArray? = null

    //当前移动的位置
    private var currentXY: IntArray? = null
    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        programId = arguments?.getLong(IntentParamKey.PROGRAM_ID.name)
        initViewModel()
        rv_bird_packet.adapter = birdAdapter
        rv_bird_packet.layoutManager = GridLayoutManager(requireContext(), 4)

        iv_bottom_03.onClickNew {
            iv_bottom_03.isEnabled = false
            mViewModel.buyBird()
        }
        ll_redPacket.onClickNew {
            logger.info("点击了红包")
        }
        iv_redPacket.onClickNew {
            logger.info("点击了红包2")
        }
        iv_bird_guide.onClickNew {
            logger.info("点击了规则")

        }
        rv_bird_packet.onTouch { _, event ->
//            logger.info("rv_bird_packet event=${event.action} rawX=${event.rawX} rawY=${event.rawY}")
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    val itemView = rv_bird_packet.findChildViewUnder(event.x, event.y)
                    logger.info("获取到当前的view ${itemView?.id}")
                    if (itemView != null) {
                        val viewHolder = rv_bird_packet.getChildViewHolder(itemView) as? BaseViewHolder
                        if (viewHolder != null) {
                            currentCombineItem = birdAdapter.getItemOrNull(viewHolder.adapterPosition)
                            logger.info("获取到当前的item=${viewHolder.adapterPosition}")
                            if (currentCombineItem?.upgradeId != null) {

                                val image = viewHolder.getView<SimpleDraweeView>(R.id.sdv_bird)
                                val bmLp = bird_mask.layoutParams as FrameLayout.LayoutParams
                                bmLp.width = image.width
                                bmLp.height = image.height
                                val location = IntArray(2)
                                image.getLocationOnScreen(location)
                                bird_mask.x = location[0].toFloat()
                                bird_mask.y = location[1].toFloat()
                                originXY = location
                                bird_mask.requestLayout()
                                bird_mask.show()
                                bird_mask.loadImage(currentCombineItem!!.upgradeIcon, 93f, 93f)
                                //设置蒙层
                                currentCombineItem?.isActive = true
                                birdAdapter.notifyItemChanged(viewHolder.adapterPosition)
                                switchRecycleState(true)
                            } else {
                                logger.info("这里item没有鸟")
                            }

                        }

                    }

                }
                MotionEvent.ACTION_MOVE -> {
                    if (currentCombineItem?.upgradeId != null) {
                        currentXY =
                            intArrayOf((event.rawX - bird_mask.width / 2).toInt(), (event.rawY - bird_mask.height / 2).toInt())
                        bird_mask.x = event.rawX - bird_mask.width / 2
                        bird_mask.y = event.rawY - bird_mask.height / 2
                    }
                }
                MotionEvent.ACTION_UP -> {
                    if (currentCombineItem?.upgradeId != null) {

                        val itemView = rv_bird_packet.findChildViewUnder(event.x, event.y)
                        switchRecycleState(false)
                        if (itemView != null) {
                            val viewHolder = rv_bird_packet.getChildViewHolder(itemView) as? BaseViewHolder
                            if (viewHolder != null) {
                                currentTargetItem = birdAdapter.getItemOrNull(viewHolder.adapterPosition)
                                currentTargetViewHolder = viewHolder
                                if (currentCombineItem != null && currentTargetItem != null) {
                                    logger.info("获取到指定目标item 开始执行操作 ${viewHolder.layoutPosition}")
                                    if (currentCombineItem == currentTargetItem) {
                                        logger.info("是同一个item 不处理")
                                        recoveryItemBird()
                                        bird_mask.hide()
                                    } else {
                                        mViewModel.combineBird(
                                            currentCombineItem!!.upgradeId,
                                            currentTargetItem!!.upgradeId,
                                            currentCombineItem!!.upgradePos,
                                            currentTargetItem!!.upgradePos
                                        )

                                    }

                                }
                            }

                        } else {
                            logger.info("当前的触控点不在任何itemView上")
                            if (calculateDragToSell(event.rawX, event.rawY)) {
                                logger.info("符合拖拽卖出 开始调用出售")
                                val upgradeId = currentCombineItem?.upgradeId
                                if (upgradeId != null) {
                                    mViewModel.recycleBird(upgradeId)
                                }
                                bird_mask.hide()
                            } else {
                                playBackAnim(currentXY, originXY)
                            }

                        }

                    }
                }
            }

            true
        }
        initMusicSet()
        initMusic()
    }

    private fun initMusic() {

        if (programId == null) {
            audioPlayerManager.setAssetsName("bird/bird_bg.mp3")
            audioPlayerManager.start(true)
        }
    }

    /**
     * 初始化声音池 以及一系列短暂声音集合
     */
    private fun initMusicSet() {

        val manager = requireContext().assets
        SoundPoolManager.instance.loadRF(BIRD_BUY, manager.openFd(BIRD_BUY))
        SoundPoolManager.instance.loadRF(BIRD_COIN, manager.openFd(BIRD_COIN))
        SoundPoolManager.instance.loadRF(BIRD_COMBINE, manager.openFd(BIRD_COMBINE))

    }

    private fun initViewModel() {
        mViewModel.programId = programId
        mViewModel.homeInfo.observe(this, Observer {
            if (it.isSuccess()) {
                renderData(it.getT())
            }
        })

        mViewModel.buyResult.observe(this, Observer {
            iv_bottom_03.isEnabled = true
            if (it.isSuccess()) {
                val bird = it.getT()
                if (bird.currentUpgrade.upgradePos < birdAdapter.data.size) {
                    birdAdapter.data[bird.currentUpgrade.upgradePos] = bird.currentUpgrade
                    birdAdapter.notifyItemChanged(bird.currentUpgrade.upgradePos)
                    SoundPoolManager.instance.play(BIRD_BUY)
                }
            } else {
                ToastUtils.show(it.error?.busiMessage)
            }
        })
        mViewModel.combineResult.observe(this, Observer {
            if (it.isSuccess()) {
                logger.info(it.getT().resultType)
                processCombineResult(it.getT())

            }
        })
        mViewModel.recycleResult.observe(this, Observer {
            if (it.isSuccess()) {
                SoundPoolManager.instance.play(BIRD_COIN)
                val currentIndex = birdAdapter.data.indexOf(currentCombineItem)
                if (currentIndex != -1) {
                    birdAdapter.setData(currentIndex, UpgradeBirdBean(upgradePos = currentIndex))
                }
            }else{
                ToastUtils.show(it.error?.busiMessage)
            }
        })
        mViewModel.totalCoin.observe(this, Observer {
            if (it != null) {
//                tv_balance.text = "余额${StringHelper.formatBigNum(it)}金币"
                tv_balance.text = StringHelper.formatBigNum(it)
                playCoinTextAni()
            }
        })
        mViewModel.coinsPerSec.observe(this, Observer {
            if (it != null) {
                tv_balance_produce.text = "${StringHelper.formatBigNum(it)}金币/秒"
            }
        })


    }

    /**
     * 播放金币变化动画
     */
    private var mCoinTextAni: AnimatorSet? = null
    private fun playCoinTextAni() {
        if (mCoinTextAni == null) {
            val aniX = ObjectAnimator.ofFloat(tv_balance, View.SCALE_X, 1.0f, 1.5f, 1.0f)
            val aniY = ObjectAnimator.ofFloat(tv_balance, View.SCALE_Y, 1.0f, 1.5f, 1.0f)
            mCoinTextAni = AnimatorSet()
            mCoinTextAni!!.interpolator = AccelerateDecelerateInterpolator()
            mCoinTextAni!!.duration = 200
            mCoinTextAni!!.playTogether(aniX, aniY)
        }
        mCoinTextAni?.start()
    }

    /**
     * 处理 移动 互换  合并 以及 产生功能鹊的结果
     */
    private fun processCombineResult(result: CombineResult) {
        val currentIndex = birdAdapter.data.indexOf(currentCombineItem)
        val targetIndex = birdAdapter.data.indexOf(currentTargetItem)
        if (currentIndex != -1 && targetIndex != -1) {
            currentCombineItem?.isActive = false
            when (result.resultType) {
                //移动
                CombineResult.MovePos -> {

                    currentTargetItem?.upgradePos = currentIndex
                    birdAdapter.setData(currentIndex, currentTargetItem!!)

                    currentCombineItem?.upgradePos = targetIndex
                    birdAdapter.setData(targetIndex, currentCombineItem!!)
                    bird_mask.hide()


                }
                //合并升级
                CombineResult.Upgrade -> {

                    playCombineAnim(result)

                }
                CombineResult.SwapPos -> {
                    currentTargetItem?.upgradePos = currentIndex
                    birdAdapter.setData(currentIndex, currentTargetItem!!)

                    currentCombineItem?.upgradePos = targetIndex
                    birdAdapter.setData(targetIndex, currentCombineItem!!)

                    bird_mask.hide()
                }
            }
        }
    }

    /**
     * 复原拖动效果到初始状态
     */
    private fun recoveryItemBird() {
        val currentIndex = birdAdapter.data.indexOf(currentCombineItem)
        currentCombineItem?.isActive = false
        if (currentIndex != -1) {
            birdAdapter.notifyItemChanged(currentIndex)
        }
    }

    /**
     * 切换回收状态[state]回收状态
     */
    private fun switchRecycleState(state: Boolean) {
        if (currentCombineItem == null || currentUnlockUpgrade == null) {
            return
        }
        if (state) {
            sdv_bottom_bird.hide()
            tv_bird_price.text = StringHelper.formatBigNum(currentCombineItem!!.upgradeSaleCoins)
            tv_recycler.show()
            tv_price_level.text = "可回收"
        } else {
            sdv_bottom_bird.show()
            tv_recycler.hide()
            tv_bird_price.text = StringHelper.formatBigNum(currentUnlockUpgrade!!.upgradeCoins)
            tv_price_level.text = "Lv.${currentUnlockUpgrade!!.upgradeLevel}"
        }
    }

    private var currentUnlockUpgrade: UnlockUpgrade? = null
    private fun renderData(info: BirdHomeInfo) {
        currentUnlockUpgrade = info.unlockUpgrade
        info.functionInfo.let { fInfo ->
            sdv_cai_shen.loadImage(fInfo.wealth.functionIcon, 80f, 80f)
            if (fInfo.wealth.functionNum.isNotEmpty() && fInfo.wealth.functionNum != "0") {
                tv_cai_shen.text = fInfo.wealth.functionNum
                tv_cai_shen.show()
            } else {
                tv_cai_shen.hide()
            }


            sdv_bird_niu_lang.loadImage(fInfo.cowherd.functionIcon, 80f, 80f)
            if (fInfo.cowherd.functionNum.isNotEmpty() && fInfo.cowherd.functionNum != "0") {
                tv_niu_lang.text = fInfo.cowherd.functionNum
                tv_niu_lang.show()
            } else {
                tv_niu_lang.hide()
            }
            sdv_bird_zhi_nv.loadImage(fInfo.weaver.functionIcon, 80f, 80f)
            if (fInfo.weaver.functionNum.isNotEmpty() && fInfo.weaver.functionNum != "0") {
                tv_zhi_nv.text = fInfo.weaver.functionNum
                tv_zhi_nv.show()
            } else {
                tv_zhi_nv.hide()
            }


            sdv_bird_hong_bao.loadImage(fInfo.redpacket.functionIcon, 80f, 80f)
            if (fInfo.redpacket.functionNum.isNotEmpty() && fInfo.redpacket.functionNum != "0") {
                tv_hong_bao.text = fInfo.redpacket.functionNum
                tv_hong_bao.show()
            } else {
                tv_hong_bao.hide()
            }

            sdv_bird_shen_mi.loadImage(fInfo.mystical.functionIcon, 80f, 80f)
            if (fInfo.mystical.functionNum.isNotEmpty() && fInfo.mystical.functionNum != "0") {
                tv_shen_mi.text = fInfo.mystical.functionNum
                tv_shen_mi.show()
            } else {
                tv_shen_mi.hide()
            }
            tv_cash.text="${info.cash}"
        }


        birdAdapter.setList(info.upgradeList)
        startAniInterval()
        tv_bird_price.text = StringHelper.formatBigNum(info.unlockUpgrade.upgradeCoins)
        tv_price_level.text = "Lv.${info.unlockUpgrade.upgradeLevel}"
        sdv_bottom_bird.loadImage(info.unlockUpgrade.upgradeIcon, 86f, 86f)
    }

    private var aniIntervalDispose: Disposable? = null
    private fun startAniInterval() {
        val list = birdAdapter.data
        aniIntervalDispose?.dispose()
        aniIntervalDispose =
            Observable.interval(0, 100, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe {
                list.forEachIndexed { index, upgradeBirdBean ->
                    if (upgradeBirdBean.upgradeId != null /*&& !upgradeBirdBean.isActive*/) {
                        val diffTime = System.currentTimeMillis() / 100 - upgradeBirdBean.createTime
//                    logger("pos=${upgradeBirdBean.upgradePos} diffTime=$diffTime 取余=${diffTime % (CoinsPerSec*10)}")
                        if (diffTime % (BirdAdapter.CoinsPerSec * 10) == 0L) {
                            val imageView = birdAdapter.getViewByPosition(index, R.id.sdv_bird)
                            val textView = birdAdapter.getViewByPosition(index, R.id.tv_produce_sec) as? TextView
                            if (imageView != null && textView != null) {
                                birdAdapter.playAnim2(imageView, textView)
                                SoundPoolManager.instance.play(BIRD_COIN)
                                if (programId == null) {
                                    mViewModel.startProcessCoins(upgradeBirdBean.onlineCoinsPerSec)
                                } else {
                                    mViewModel.startProcessCoins(upgradeBirdBean.programCoinsPerSec)
                                }

                            }

                        }
                    }

                }
            }
    }

    private fun stopAniInterval() {
        aniIntervalDispose?.dispose()
    }

    override fun showLoadState(state: NetState) {
    }

    override fun lazyLoadData() {
        mViewModel.queryHome()
    }

    /**
     * 播放回归原位动画
     */
    private fun playBackAnim(currentPosition: IntArray?, originPosition: IntArray?) {
        if (currentPosition != null && currentPosition.isNotEmpty() && originPosition != null && originPosition.isNotEmpty()) {
            val anim1 = ValueAnimator.ofFloat(currentPosition[0].toFloat(), originPosition[0].toFloat())
            anim1.addUpdateListener { valueAnimate ->
                bird_mask.x = valueAnimate.animatedValue as Float
            }
            val anim2 = ValueAnimator.ofFloat(currentPosition[1].toFloat(), originPosition[1].toFloat())
            anim2.addUpdateListener { valueAnimate ->
                bird_mask.y = valueAnimate.animatedValue as Float
            }
            val set = AnimatorSet()
            set.duration = 300
            set.playTogether(anim1, anim2)
            set.addListener(onEnd = {
                recoveryItemBird()
                bird_mask.hide()
            })
            set.start()
        }

    }

    /**
     * 播放合并动画
     */
    private fun playCombineAnim(result: CombineResult) {
        if (currentTargetViewHolder == null || currentCombineItem == null || currentTargetItem == null) {
            return
        }
        val currentIndex = birdAdapter.data.indexOf(currentCombineItem!!)
        val targetIndex = birdAdapter.data.indexOf(currentTargetItem!!)

        val image = currentTargetViewHolder!!.getView<SimpleDraweeView>(R.id.sdv_bird)
        //1.播放合体动画前准备
        val location = IntArray(2)
        image.getLocationOnScreen(location)

        val bmLp = bird_mask2.layoutParams as FrameLayout.LayoutParams
        bmLp.width = image.width
        bmLp.height = image.height
        image.getLocationOnScreen(location)
        bird_mask.x = location[0].toFloat()
        bird_mask.y = location[1].toFloat()
        bird_mask.requestLayout()
        bird_mask.show()
        bird_mask.loadImage(currentCombineItem!!.upgradeIcon, 93f, 93f)

        val bm2Lp = bird_mask2.layoutParams as FrameLayout.LayoutParams
        bm2Lp.width = image.width
        bm2Lp.height = image.height

        bird_mask2.x = location[0].toFloat()
        bird_mask2.y = location[1].toFloat()
        bird_mask2.requestLayout()
        bird_mask2.show()
        bird_mask2.loadImage(currentCombineItem!!.upgradeIcon, 93f, 93f)
        //设定向两边
        val distance = dp2px(65)
        //2.做合并动画
        //(1)mask1 mask2向两边展开动画
        val anim0101 =
            ObjectAnimator.ofFloat(bird_mask, View.TRANSLATION_X, bird_mask.translationX, bird_mask.translationX - distance)
        val anim0102 =
            ObjectAnimator.ofFloat(bird_mask2, View.TRANSLATION_X, bird_mask2.translationX, bird_mask2.translationX + distance)
        val anim01 = AnimatorSet()
        anim01.duration = 200
        anim01.interpolator = BounceInterpolator()
        anim01.playTogether(anim0101, anim0102)

        //(2)停留一段时间后 相互靠拢
        val anim0201 =
            ObjectAnimator.ofFloat(bird_mask, View.TRANSLATION_X, bird_mask.translationX - distance, bird_mask.translationX)
        val anim0202 =
            ObjectAnimator.ofFloat(bird_mask2, View.TRANSLATION_X, bird_mask2.translationX + distance, bird_mask2.translationX)
        val anim02 = AnimatorSet()
        anim02.interpolator = AnticipateOvershootInterpolator()
        anim02.duration = 200
        anim02.startDelay = 200
        anim02.playTogether(anim0201, anim0202)
        //(3)靠拢后 播放合体特效
        anim02.addListener(onEnd = {
            //播放特效 播放声音
            SoundPoolManager.instance.play(BIRD_COMBINE)
            logger.info("动画播放完了 开始播放特效")
            //3.将原有的两个鹊移除 将升级的鹊放入 目标格子
            val currentNew = result.currentUpgrade
            if (currentIndex != -1 && targetIndex != -1 && currentNew != null) {
                birdAdapter.setData(currentIndex, UpgradeBirdBean(upgradePos = currentIndex))
                birdAdapter.setData(targetIndex, currentNew)
                bird_mask.hide()
                bird_mask2.hide()
            }

        })
        val combineAnim = AnimatorSet()
        combineAnim.playSequentially(anim01, anim02)
        combineAnim.start()
    }

    /**
     * 计算是否拖拽卖出
     */
    private fun calculateDragToSell(rawX: Float, rawY: Float): Boolean {
        if (rawX == 0f || rawY == 0f) {
            return false
        }

        // 获取控件在屏幕中的位置，返回的数组分别为控件左顶点的 x、y 的值
        val location = IntArray(2)
        iv_bottom_03.getLocationOnScreen(location)
        val rectF = RectF(
            location[0].toFloat(), location[1].toFloat(), (location[0] + iv_bottom_03.width).toFloat(),
            (location[1] + iv_bottom_03.height).toFloat()
        )
        if (rectF.contains(rawX, rawY)) {
            return true
        }
        return false
    }

    override fun onResume() {
        super.onResume()
        if (programId == null) {
            audioPlayerManager.resume()
        }

    }

    override fun onHiddenChanged(hidden: Boolean) {
        logger.info("onHiddenChanged=$hidden")
        super.onHiddenChanged(hidden)
        if (programId == null) {
            if (hidden) {
                audioPlayerManager.pause()
            } else {
                audioPlayerManager.resume()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (programId == null) {
            audioPlayerManager.pause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (programId == null) {
            audioPlayerManager.destroy()
        }
        stopAniInterval()
    }
}