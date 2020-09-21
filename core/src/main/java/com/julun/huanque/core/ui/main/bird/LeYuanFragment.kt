package com.julun.huanque.core.ui.main.bird

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.RectF
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.BounceInterpolator
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.animation.addListener
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.binioter.guideview.Guide
import com.binioter.guideview.GuideBuilder
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.base.BaseVMFragment
import com.julun.huanque.common.base.dialog.MyAlertDialog
import com.julun.huanque.common.basic.NetState
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.bean.events.HideBirdEvent
import com.julun.huanque.common.constant.IntentParamKey
import com.julun.huanque.common.helper.StorageHelper
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.manager.audio.AudioPlayerManager
import com.julun.huanque.common.manager.audio.SoundPoolManager
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.common.utils.ScreenUtils
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.common.widgets.live.WebpGifView
import com.julun.huanque.core.R
import com.julun.huanque.core.ui.live.PlayerActivity
import com.julun.huanque.core.ui.main.bird.guide.LottieComponent
import com.julun.huanque.core.ui.main.bird.guide.LottieComponent2
import com.julun.huanque.core.ui.withdraw.WithdrawActivity
import com.julun.huanque.core.widgets.DispatchRecyclerView
import com.julun.rnlib.RNPageActivity
import com.julun.rnlib.RnConstant
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_leyuan.*
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.sdk23.listeners.onTouch
import org.jetbrains.anko.startActivity
import java.math.BigInteger
import java.util.concurrent.TimeUnit

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/8/26 19:51
 *
 *@Description: LeYuanFragment
 *
 */
class LeYuanFragment : BaseVMFragment<LeYuanViewModel>() {
    companion object {
        fun newInstance(programId: Long? = null): LeYuanFragment {
            val fragment = LeYuanFragment()
            fragment.arguments = Bundle().apply {
                if (programId != null) {
                    putLong(IntentParamKey.PROGRAM_ID.name, programId)
                }

            }
            return fragment
        }

        //一些播放的音频短
        const val BIRD_BUY = "bird/bird_buy.mp3"
        const val BIRD_COIN_SHORT = "bird/bird_coin_short.mp3"
        const val BIRD_COIN = "bird/bird_coin.mp3"
        const val BIRD_COMBINE = "bird/bird_combine.mp3"
    }


    private val mFunctionViewModel: BirdFunctionViewModel by activityViewModels()

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

    private var shopDialogFragment: BirdShopDialogFragment? = null

    private var birdTaskDialogFragment: BirdTaskDialogFragment? = null

    private var birdDescDialogFragment: BirdDescriptionDialogFragment? = null


    private var birdFunctionDialogFragment: BirdFunctionDialogFragment? = null

    private var mBirdGotFunctionDialogFragment: BirdGotFunctionDialogFragment? = null

    private var mBirdTaskGuideFragment: BirdTaskGuideFragment? = null
    private var isInLivePage = false

    //当前是否正在操作请求中 如果没有完成 就不再执行下次拖动
    private var isActionDoing: Boolean = false

    private val myAlertDialog: MyAlertDialog by lazy { MyAlertDialog(requireActivity(), false) }
    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        val pid = arguments?.getLong(IntentParamKey.PROGRAM_ID.name)
        if (pid != 0L && pid != null) {
            programId = pid
        }
        val activity = requireActivity()
        if (activity is PlayerActivity) {
            isInLivePage = true
            csl_top.inVisible()
            view_top_holder.show()
            view_top_holder.onTouch { _, _ ->
                logger("触摸头部关闭弹窗")
                EventBus.getDefault().post(HideBirdEvent())
                false
            }
        } else if (activity is LeYuanBirdActivity) {
            isInLivePage = false
            csl_top.show()
            view_top_holder.hide()
        }
        tv_recycler_coin.isColorsVertical = true
        tv_recycler_coin.colors = intArrayOf(Color.parseColor("#FFFBEF"), Color.parseColor("#E5A441"))
        tv_recycler_coin.setStrokeColorAndWidth(Color.parseColor("#B96E23"), 2f)
        initViewModel()
        rv_bird_packet.adapter = birdAdapter
        (rv_bird_packet.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        rv_bird_packet.layoutManager = GridLayoutManager(requireContext(), 4)
        sdv_cai_shen.onClickNew {
            val bird = mViewModel.gotFunctionBirdInfo("wealth")
            if (bird != null) {
                if (birdFunctionDialogFragment == null) {
                    birdFunctionDialogFragment = BirdFunctionDialogFragment.newInstance(bird)
                } else {
                    birdFunctionDialogFragment?.setBird(bird)
                }
                birdFunctionDialogFragment?.show(requireActivity(), "birdFunctionDialogFragment")
            }
        }
        sdv_bird_niu_lang.onClickNew {
            val bird = mViewModel.gotFunctionBirdInfo("cowherd")
            if (bird != null) {
                if (birdFunctionDialogFragment == null) {
                    birdFunctionDialogFragment = BirdFunctionDialogFragment.newInstance(bird)
                } else {
                    birdFunctionDialogFragment?.setBird(bird)
                }
                birdFunctionDialogFragment?.show(requireActivity(), "birdFunctionDialogFragment")
            }
        }
        sdv_bird_zhi_nv.onClickNew {
            val bird = mViewModel.gotFunctionBirdInfo("weaver")
            if (bird != null) {
                if (birdFunctionDialogFragment == null) {
                    birdFunctionDialogFragment = BirdFunctionDialogFragment.newInstance(bird)
                } else {
                    birdFunctionDialogFragment?.setBird(bird)
                }
                birdFunctionDialogFragment?.show(requireActivity(), "birdFunctionDialogFragment")
            }
        }
        sdv_bird_hong_bao.onClickNew {
            val bird = mViewModel.gotFunctionBirdInfo("redpacket")
            if (bird != null) {
                if (birdFunctionDialogFragment == null) {
                    birdFunctionDialogFragment = BirdFunctionDialogFragment.newInstance(bird)
                } else {
                    birdFunctionDialogFragment?.setBird(bird)
                }
                birdFunctionDialogFragment?.show(requireActivity(), "birdFunctionDialogFragment")
            }
        }
        sdv_bird_shen_mi.onClickNew {
            val bird = mViewModel.gotFunctionBirdInfo("mystical")
            if (bird != null) {
                if (birdFunctionDialogFragment == null) {
                    birdFunctionDialogFragment = BirdFunctionDialogFragment.newInstance(bird)
                } else {
                    birdFunctionDialogFragment?.setBird(bird)
                }
                birdFunctionDialogFragment?.show(requireActivity(), "birdFunctionDialogFragment")
            }
        }
        iv_bottom_03.onClick {
            logger.info("购买操作-----$isActionDoing")
            if (!isActionDoing) {
//                iv_bottom_03.isEnabled = false
                mViewModel.buyBird()
            }

        }

        ll_redPacket.onClickNew {
            logger.info("点击了红包")
            gotoWithdraw()
        }
        iv_redPacket.onClickNew {
            logger.info("点击了红包2")
            gotoWithdraw()
        }
        iv_bird_guide.onClickNew {
            logger.info("点击了规则")
            //todo 跳转h5


        }
        iv_bottom_02.onClickNew {
            logger.info("点击了邀友")
            RNPageActivity.start(requireActivity(), RnConstant.INVITE_FRIENDS_PAGE)
        }
        iv_shop.onClickNew {
            logger.info("点击了商店")
            shopDialogFragment = shopDialogFragment ?: BirdShopDialogFragment(mViewModel)
            shopDialogFragment?.show(requireActivity(), "shopDialogFragment")
        }
        iv_task.onClickNew {
            birdTaskDialogFragment = birdTaskDialogFragment ?: BirdTaskDialogFragment(mViewModel)
            birdTaskDialogFragment?.show(requireActivity(), "birdTaskDialogFragment")
        }
        iv_bottom_01.onClickNew {
            birdDescDialogFragment = birdDescDialogFragment ?: BirdDescriptionDialogFragment((mViewModel))
            birdDescDialogFragment?.show(requireActivity(), "birdDescDialogFragment")
        }
        ivClose.onClickNew {
            EventBus.getDefault().post(HideBirdEvent())
        }
        rv_bird_packet.mDispatchListener = object : DispatchRecyclerView.DispatchListener {
            override fun dispatch(event: MotionEvent?): Boolean {
                event ?: return false
//            logger.info("rv_bird_packet event=${event.action} rawX=${event.rawX} rawY=${event.rawY}")
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        if (isActionDoing) {
                            logger.info("当前的操作还没完成 停止分发")
                            return false
                        }
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
                                intArrayOf(
                                    (event.rawX - bird_mask.width / 2).toInt(),
                                    (event.rawY - bird_mask.height / 2).toInt()
                                )
                            bird_mask.x = event.rawX - bird_mask.width / 2
                            bird_mask.y = event.rawY - bird_mask.height / 2
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        if (currentCombineItem?.upgradeId != null) {
                            isActionDoing = true
                            val itemView = rv_bird_packet.findChildViewUnder(event.x, event.y)
                            if (itemView != null) {
                                switchRecycleState(false)
                                val viewHolder = rv_bird_packet.getChildViewHolder(itemView) as? BaseViewHolder
                                if (viewHolder != null) {
                                    currentTargetItem = birdAdapter.getItemOrNull(viewHolder.adapterPosition)
                                    currentTargetViewHolder = viewHolder
                                    if (currentCombineItem != null && currentTargetItem != null) {
                                        logger.info("获取到指定目标item 开始执行操作 ${viewHolder.layoutPosition}")
                                        if (currentCombineItem == currentTargetItem) {
                                            logger.info("是同一个item 不处理")
                                            recoveryItemBird()
                                            isActionDoing = false
                                        } else {
                                            if (currentCombineItem!!.upgradeLevel == 37 && currentTargetItem!!.upgradeLevel == 37) {
                                                myAlertDialog.showAlertWithOKAndCancel(
                                                    message = "合并后您将随机获得一只功能鹊，并且您的棋盘将被重置，包括已合成的升级鹊和金币余额。",
                                                    noText = "再想想",
                                                    okText = "确定合并",
                                                    callback = MyAlertDialog.MyDialogCallback(onRight = {
                                                        startCombineBird()
                                                    }, onCancel = {
                                                        recoveryItemBird()
                                                        isActionDoing = false
                                                    })
                                                )
                                            } else {
                                                startCombineBird()
                                            }
                                        }

                                    } else {
                                        isActionDoing = false
                                    }
                                }

                            } else {
                                logger.info("当前的触控点不在任何itemView上")
                                if (calculateDragToSell(event.rawX, event.rawY)) {
                                    logger.info("符合拖拽卖出 开始调用出售")
                                    val upgradeId = currentCombineItem?.upgradeId
                                    if (upgradeId != null) {
                                        mViewModel.recycleBird(upgradeId)
                                    } else {
                                        isActionDoing = false
                                    }
                                    bird_mask.hide()
                                } else {
                                    isActionDoing = false
                                    switchRecycleState(false)
                                    playBackAnim(currentXY, originXY)
                                }

                            }

                        }
                    }
                }

                return true
            }
        }
        bird_combine_ani.setCallBack(
            object : WebpGifView.GiftViewPlayCallBack {
                //不管成功加载或者失败加载  都开始本地动画
                override fun onRelease() {
                }

                override fun onStart() {
                }

                override fun onError() {
                    bird_combine_ani.hide()
                }

                override fun onEnd() {
                    bird_combine_ani.hide()

                }
            })
        if (!isInLivePage) {
            initMusicSet()
            initMusic()
            initCloudAni()
        }
    }

    //云朵动画
    private fun initCloudAni() {
        view_cloud_01.post {
            val anim0101 =
                ObjectAnimator.ofFloat(
                    view_cloud_01,
                    View.TRANSLATION_X,
                    -view_cloud_01.right.toFloat() - dp2px(50),
                    ScreenUtils.screenWidthFloat - view_cloud_01.left.toFloat()
                )
            val anim0102 =
                ObjectAnimator.ofFloat(
                    view_cloud_02,
                    View.TRANSLATION_X,
                    -view_cloud_02.right.toFloat(), ScreenUtils.screenWidthFloat - view_cloud_02.left.toFloat()
                )
            anim0101.repeatMode = ValueAnimator.RESTART
            anim0101.repeatCount = ValueAnimator.INFINITE
            anim0101.duration = 1000L * 40
//            anim0101.startDelay = 1000L
            anim0101.start()
            anim0102.repeatMode = ValueAnimator.RESTART
            anim0102.repeatCount = ValueAnimator.INFINITE

            anim0102.duration = 1000L * 30
            anim0102.start()
        }

    }

    private fun startCombineBird() {
        if (currentCombineItem != null && currentTargetItem != null) {
            mViewModel.combineBird(
                currentCombineItem!!.upgradeId,
                currentTargetItem!!.upgradeId,
                currentCombineItem!!.upgradePos,
                currentTargetItem!!.upgradePos
            )

        } else {
            recoveryItemBird()
            isActionDoing = false
        }

    }

    private var guide1: Guide? = null
    private var isGuide1: Boolean = false

    /**
     * [hasPlay]如果当前的返回的棋盘不是一级或者棋盘有鹊时代表以经玩过
     */
    private fun initGuideView1(hasPlay:Boolean) {
        if(hasPlay){
            logger.info("已经玩过 不再需要引导")
            return
        }
        val needBirdGuide = StorageHelper.getNeedBirdGuide()
        if (!needBirdGuide) {
            logger.info("不再需要引导")
            return
        }
        val builder = GuideBuilder()
        builder.setTargetView(iv_bottom_03)
            .setAlpha(204)
//            .setHighTargetCorner(20)
            .setHighTargetPaddingTop(dp2px(18))
            .setHighTargetPaddingBottom(dp2px(5))
            .setHighTargetPaddingLeft(dp2px(15))
            .setHighTargetPaddingRight(dp2px(15))
            .setAutoDismiss(false)
//            .setOutsideTouchable(true)
        builder.setOnVisibilityChangedListener(object : GuideBuilder.OnVisibilityChangedListener {
            override fun onShown() {
                isGuide1 = true
            }

            override fun onDismiss() {
                logger("引导1关闭")
                isGuide1 = false
            }
        })

        builder.addComponent(LottieComponent(0).apply {
            this.listener = object : LottieComponent.OnListener {
                override fun onclickView(viewId: Int) {
                    logger.info("点击了引导view=$viewId")
                    if (viewId == R.id.close) {
                        logger.info("用户主动关闭了引导 不再引导")
                        guide1?.dismiss()
                        StorageHelper.setNeedBirdGuide(false)
                    } else if (viewId == R.id.click_holder) {
                        mViewModel.buyBird()
                    }

                }

            }
        })
        guide1 = builder.createGuide()
        guide1?.show(requireActivity())
    }

    private var guide2: Guide? = null
    private var isGuide2: Boolean = false
    private fun initGuideView2() {
        val builder = GuideBuilder()
        builder.setTargetView(iv_bottom_03)
            .setAlpha(204)
            .setHighTargetPaddingTop(dp2px(18))
            .setHighTargetPaddingBottom(dp2px(5))
            .setHighTargetPaddingLeft(dp2px(15))
            .setHighTargetPaddingRight(dp2px(15))
            .setAutoDismiss(false)
//            .setOutsideTouchable(true)
        builder.setOnVisibilityChangedListener(object : GuideBuilder.OnVisibilityChangedListener {
            override fun onShown() {
                isGuide2 = true
            }

            override fun onDismiss() {
                logger("引导2关闭")
                isGuide2 = false
            }
        })

        builder.addComponent(LottieComponent(1).apply {
            this.listener = object : LottieComponent.OnListener {
                override fun onclickView(viewId: Int) {
                    logger.info("点击了引导view=$viewId")
                    if (viewId == R.id.close) {
                        guide2?.dismiss()
                        StorageHelper.setNeedBirdGuide(false)
                        logger.info("用户主动关闭了引导 不再引导")
                    } else if (viewId == R.id.click_holder) {
                        mViewModel.buyBird()
                        //
                    }

                }

            }
        })
        guide2 = builder.createGuide()
        guide2?.show(requireActivity())
    }

    private var guide3: Guide? = null
    private var isGuide3: Boolean = false
    private fun initGuideView3() {
        val viewFirst = birdAdapter.getViewByPosition(0, R.id.constraintLayout) ?: return
        val builder = GuideBuilder()
        builder.setTargetView(viewFirst)
            .setAlpha(204)
            .setHighTargetPaddingTop(dp2px(3))
            .setHighTargetPaddingBottom(dp2px(3))
            .setHighTargetPaddingLeft(dp2px(10))
            .setHighTargetPaddingRight(viewFirst.width)
            .setAutoDismiss(false)
            .setOutsideTouchable(true)
        builder.setOnVisibilityChangedListener(object : GuideBuilder.OnVisibilityChangedListener {
            override fun onShown() {
                isGuide3 = true
            }

            override fun onDismiss() {
                logger("引导3关闭")
                isGuide3 = false
                StorageHelper.setNeedBirdGuide(false)
            }
        })
        builder.addComponent(LottieComponent2(px2dp(viewFirst.width * 2f)).apply {
            this.listener = object : LottieComponent2.OnListener {
                override fun onclickView(viewId: Int) {
                    logger.info("点击了引导view=$viewId")
                    if (viewId == R.id.close) {
                        guide3?.dismiss()
                        StorageHelper.setNeedBirdGuide(false)
                        logger.info("用户主动关闭了引导 不再引导")
                    }

                }

            }
        })
        guide3 = builder.createGuide()
        guide3?.show(requireActivity())
    }

    private fun gotoWithdraw() {
        requireActivity().startActivity<WithdrawActivity>()
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
        SoundPoolManager.instance.loadRF(BIRD_COIN_SHORT, manager.openFd(BIRD_COIN_SHORT))
        SoundPoolManager.instance.loadRF(BIRD_COIN, manager.openFd(BIRD_COIN))
        SoundPoolManager.instance.loadRF(BIRD_COMBINE, manager.openFd(BIRD_COMBINE))

    }

    private fun playSound(soundPath: String) {
        if (!isInLivePage) {
            SoundPoolManager.instance.play(soundPath)
        }
    }

    private fun initViewModel() {
        mViewModel.programId = programId
        mViewModel.homeInfo.observe(this, Observer {
            if (it.isSuccess()) {
                renderData(it.requireT())
            }
        })

        mViewModel.buyResult.observe(this, Observer {
//            iv_bottom_03.isEnabled = true
            if (it.isSuccess()) {
                val bird = it.requireT()
                if (bird.hasEnough != null && bird.hasEnough == false) {
                    if (mBirdTaskGuideFragment == null) {
                        mBirdTaskGuideFragment = BirdTaskGuideFragment.newInstance(task = bird.taskGuideInfo)
                    } else {
                        mBirdTaskGuideFragment?.setTask(bird.taskGuideInfo)
                    }
                    mBirdTaskGuideFragment?.show(requireActivity(), "BirdTaskGuideFragment")
                } else {
                    if (bird.currentUpgrade.upgradePos < birdAdapter.data.size) {
                        birdAdapter.data[bird.currentUpgrade.upgradePos] = bird.currentUpgrade
                        birdAdapter.notifyItemChanged(bird.currentUpgrade.upgradePos)
//                    SoundPoolManager.instance.play(BIRD_BUY)
                        playSound(BIRD_BUY)
                    }
                    if (isGuide1) {
                        guide1?.dismiss()
                        initGuideView2()
                    } else if (isGuide2) {
                        guide2?.dismiss()
                        initGuideView3()
                    }
                }

            } else {
                ToastUtils.show2(it.error?.busiMessage)
            }
        })
        mViewModel.combineResult.observe(this, Observer {
            if (it.isSuccess()) {
                logger.info(it.requireT().resultType)
                processCombineResult(it.requireT())
            } else {
                recoveryItemBird()
                isActionDoing = false
            }

        })
        mViewModel.recycleResult.observe(this, Observer {
            if (it.isSuccess()) {
//                SoundPoolManager.instance.play(BIRD_COIN)
                playSound(BIRD_COIN)
                playRecycleBirdCoin(currentCombineItem?.upgradeSaleCoins)
                val currentIndex = birdAdapter.data.indexOf(currentCombineItem)
                if (currentIndex != -1) {
                    birdAdapter.setData(currentIndex, UpgradeBirdBean(upgradePos = currentIndex))
                }
            } else {
//                ToastUtils.show(it.error?.busiMessage)
            }
            isActionDoing = false
        })
        mViewModel.totalCoin.observe(this, Observer {
            if (it != null) {
//                tv_balance.text = "${StringHelper.formatBigNum(it)}金币"
                tv_balance.text = StringHelper.formatBigNum(it)
                playCoinTextAni()
            }
        })
        mViewModel.coinsPerSec.observe(this, Observer {
            if (it != null) {
                tv_balance_produce.text = "${StringHelper.formatBigNum(it)}金币/秒"
            }
        })
        mViewModel.hasNotReceive.observe(this, Observer {
            if (it != null) {
                if (it) {
                    tv_task_tag.show()
                } else {
                    tv_task_tag.hide()
                }

            }
        })
        mViewModel.unlockUpgrade.observe(this, Observer {
            if (it != null) {
                renderBottom(it)
            }
        })
        mViewModel.functionInfo.observe(this, Observer {
            if (it != null) {
                //弹出获取功能鹊
                if (mBirdGotFunctionDialogFragment == null) {
                    mBirdGotFunctionDialogFragment = BirdGotFunctionDialogFragment.newInstance(it)
                } else {
                    mBirdGotFunctionDialogFragment?.setBird(it)
                }
                mBirdGotFunctionDialogFragment?.show(requireActivity(), "BirdGotFunctionDialogFragment")
            }
        })


        mFunctionViewModel.flyResult.observe(this, Observer {
            it ?: return@Observer
            if (it.isSuccess()) {
                val data = it.requireT()
                //直接刷新
                when (data.resultType) {
                    "Function" -> {
                        view_top_holder?.postDelayed({
                            mViewModel.queryHome()
                        }, 500)
                    }
                    "Cash" -> {
                        //手动更改余额 减去功能鹊数目
                        val info = mViewModel.homeInfo.value?.getT()
                        if (info != null) {
                            tv_cash.text = "${info.cash.add(data.cash)}元"
                            val fInfo = data.functionNumInfo ?: return@Observer
                            if (fInfo.wealth.isNotEmpty() && fInfo.wealth != "0") {
                                tv_cai_shen.text = fInfo.wealth
                                tv_cai_shen.show()
                            } else {
                                tv_cai_shen.hide()
                            }
                            if (fInfo.cowherd.isNotEmpty() && fInfo.cowherd != "0") {
                                tv_niu_lang.text = fInfo.cowherd
                                tv_niu_lang.show()
                            } else {
                                tv_niu_lang.hide()
                            }
                            sdv_bird_zhi_nv.loadImage(fInfo.weaver, 80f, 80f)
                            if (fInfo.weaver.isNotEmpty() && fInfo.weaver != "0") {
                                tv_zhi_nv.text = fInfo.weaver
                                tv_zhi_nv.show()
                            } else {
                                tv_zhi_nv.hide()
                            }

                            if (fInfo.redpacket.isNotEmpty() && fInfo.redpacket != "0") {
                                tv_hong_bao.text = fInfo.redpacket
                                tv_hong_bao.show()
                            } else {
                                tv_hong_bao.hide()
                            }
                            if (fInfo.mystical.isNotEmpty() && fInfo.mystical != "0") {
                                tv_shen_mi.text = fInfo.mystical
                                tv_shen_mi.show()
                            } else {
                                tv_shen_mi.hide()
                            }
                        }

                    }
                }


            }

        })


    }

    /**
     * 播放出售金币飘出动画
     */
    private fun playRecycleBirdCoin(coin: BigInteger?) {
        if (coin == null) {
            return
        }
        tv_recycler_coin.text = "+${StringHelper.formatBigNum(coin)}"
        val aniText1 = ObjectAnimator.ofFloat(tv_recycler_coin, View.TRANSLATION_Y, 0f, -dp2pxf(30))
        aniText1.interpolator = AccelerateDecelerateInterpolator()
        aniText1.duration = 200
        //目的是继续显示
        val aniText2 = ObjectAnimator.ofFloat(tv_recycler_coin, View.TRANSLATION_Y, -dp2pxf(40), -dp2pxf(45))
        aniText2.startDelay = 100
        aniText2.duration = 300
        val set = AnimatorSet()
        set.playSequentially(aniText1, aniText2)
        set.addListener(onEnd = {
//                    logger("数字执行完成  开始隐藏")
            tv_recycler_coin?.hide()
            switchRecycleState(false)
        })
        tv_recycler_coin.show()
        set.start()
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
                    isActionDoing = false

                }
                //合并升级
                CombineResult.Upgrade, CombineResult.Function -> {

                    playCombineAnim(result)

                }
                CombineResult.SwapPos -> {
//                    currentTargetItem?.upgradePos = currentIndex
//                    birdAdapter.setData(currentIndex, currentTargetItem!!)
//
//                    currentCombineItem?.upgradePos = targetIndex
//                    birdAdapter.setData(targetIndex, currentCombineItem!!)
//                    bird_mask.hide()
//                    isActionDoing = false

                    playSwapPosAni(result)
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
        bird_mask.hide()
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
            tv_bird_price.backgroundResource = R.mipmap.bg_bird_price_recycler
            tv_recycler.show()
            tv_price_level.text = "可回收"
            iv_bottom_03.imageResource = R.mipmap.bg_bird_bottom_03_recycler
        } else {
            sdv_bottom_bird.show()
            tv_recycler.hide()
            tv_bird_price.text = StringHelper.formatBigNum(currentUnlockUpgrade!!.upgradeCoins)
            tv_bird_price.backgroundResource = R.mipmap.bg_bird_price
            tv_price_level.text = "Lv.${currentUnlockUpgrade!!.upgradeLevel}"
            iv_bottom_03.imageResource = R.mipmap.bg_bird_bottom_03
        }
    }

    private var currentUnlockUpgrade: UnlockUpgrade? = null
    private fun renderData(info: BirdHomeInfo) {

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
            tv_cash.text = "${info.cash}元"
        }


        birdAdapter.setList(info.upgradeList)
        startAniInterval()
        if (requireActivity() is LeYuanBirdActivity) {
            rv_bird_packet.postDelayed({
                val hasPlay=info.unlockUpgrade.upgradeLevel!=0||info.upgradeList.size>0
                initGuideView1(hasPlay)
            }, 100)
        }
    }

    /**
     * 刷新底部购买栏数据
     */
    private fun renderBottom(unlockUpgrade: UnlockUpgrade) {
        currentUnlockUpgrade = unlockUpgrade
        tv_bird_price.text = StringHelper.formatBigNum(unlockUpgrade.upgradeCoins)
        tv_price_level.text = "Lv.${unlockUpgrade.upgradeLevel}"
        sdv_bottom_bird.loadImage(unlockUpgrade.upgradeIcon, 86f, 86f)
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
//                                SoundPoolManager.instance.play(BIRD_COIN_SHORT)
                                playSound(BIRD_COIN_SHORT)
                                if (programId == null) {
                                    mViewModel.startProcessCoins(
                                        upgradeBirdBean.onlineCoinsPerSec.multiply(
                                            BigInteger.valueOf(
                                                BirdAdapter.CoinsPerSec
                                            )
                                        )
                                    )
                                } else {
                                    mViewModel.startProcessCoins(
                                        upgradeBirdBean.programCoinsPerSec.multiply(
                                            BigInteger.valueOf(
                                                BirdAdapter.CoinsPerSec
                                            )
                                        )
                                    )
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
            })
            set.start()
        }

    }

    /**
     * 播放交换动画
     */
    private fun playSwapPosAni(result: CombineResult) {
        if (currentTargetViewHolder == null || currentCombineItem == null || currentTargetItem == null || originXY == null || originXY!!.isEmpty()) {
            isActionDoing = false
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
        //目标位置
        bird_mask.x = location[0].toFloat()
        bird_mask.y = location[1].toFloat()
        bird_mask.show()
        bird_mask.loadImage(currentTargetItem!!.upgradeIcon, 93f, 93f)

        val bm2Lp = bird_mask2.layoutParams as FrameLayout.LayoutParams
        bm2Lp.width = image.width
        bm2Lp.height = image.height
        //起始位置
        bird_mask2.x = originXY?.getOrNull(0)?.toFloat() ?: return
        bird_mask2.y = originXY?.getOrNull(1)?.toFloat() ?: return
        bird_mask2.show()
        bird_mask2.loadImage(currentCombineItem!!.upgradeIcon, 93f, 93f)

        mask_container.requestLayout()


        //2.做交换动画
        val anim0101 =
            ObjectAnimator.ofFloat(bird_mask, View.TRANSLATION_X, bird_mask.translationX, bird_mask2.translationX)
        val anim0102 =
            ObjectAnimator.ofFloat(
                bird_mask,
                View.TRANSLATION_Y,
                bird_mask.translationY,
                bird_mask2.translationY
            )
        val anim01 = AnimatorSet()
        anim01.duration = 150
        anim01.playTogether(anim0101, anim0102)
        anim01.interpolator = AccelerateDecelerateInterpolator()
        //(2)
        val anim0201 =
            ObjectAnimator.ofFloat(bird_mask2, View.TRANSLATION_X, bird_mask2.translationX, bird_mask.translationX)
        val anim0202 =
            ObjectAnimator.ofFloat(
                bird_mask2,
                View.TRANSLATION_Y,
                bird_mask2.translationY,
                bird_mask.translationY
            )
        val anim02 = AnimatorSet()
        anim02.duration = 150
        anim02.playTogether(anim0201, anim0202)
        anim02.interpolator = AccelerateDecelerateInterpolator()
        //(3)
        anim02.addListener(onEnd = {
            currentTargetItem?.upgradePos = currentIndex
            birdAdapter.setData(currentIndex, currentTargetItem!!)

            currentCombineItem?.upgradePos = targetIndex
            birdAdapter.setData(targetIndex, currentCombineItem!!)
            bird_mask.hide()
            bird_mask2.hide()
            isActionDoing = false
        })
        val sAnim = AnimatorSet()
        sAnim.playTogether(anim01, anim02)
        sAnim.start()

    }

    /**
     * 播放合并动画
     */
    private fun playCombineAnim(result: CombineResult) {
        if (currentTargetViewHolder == null || currentCombineItem == null || currentTargetItem == null) {
            isActionDoing = false
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
        bird_mask.show()
        bird_mask.loadImage(currentCombineItem!!.upgradeIcon, 93f, 93f)

        val bm2Lp = bird_mask2.layoutParams as FrameLayout.LayoutParams
        bm2Lp.width = image.width
        bm2Lp.height = image.height

        bird_mask2.x = location[0].toFloat()
        bird_mask2.y = location[1].toFloat()
//        bird_mask2.requestLayout()
        bird_mask2.show()
        bird_mask2.loadImage(currentCombineItem!!.upgradeIcon, 93f, 93f)

        bird_combine_ani.x = location[0].toFloat()
        bird_combine_ani.y = location[1].toFloat()
        mask_container.requestLayout()
        bird_combine_ani.hide()


        //设定向两边
        val distance = dp2px(35)
        //2.做合并动画
        //(1)mask1 mask2向两边展开动画
        val anim0101 =
            ObjectAnimator.ofFloat(bird_mask, View.TRANSLATION_X, bird_mask.translationX, bird_mask.translationX - distance)
        val anim0102 =
            ObjectAnimator.ofFloat(
                bird_mask2,
                View.TRANSLATION_X,
                bird_mask2.translationX,
                bird_mask2.translationX + distance
            )
        val anim01 = AnimatorSet()
        anim01.duration = 100
//        anim01.interpolator = AnticipateOvershootInterpolator()
        anim01.playTogether(anim0101, anim0102)

        //(2)停留一段时间后 相互靠拢
        val anim0201 =
            ObjectAnimator.ofFloat(bird_mask, View.TRANSLATION_X, bird_mask.translationX - distance, bird_mask.translationX)
        val anim0202 =
            ObjectAnimator.ofFloat(
                bird_mask2,
                View.TRANSLATION_X,
                bird_mask2.translationX + distance,
                bird_mask2.translationX
            )
        val anim02 = AnimatorSet()
        anim02.interpolator = BounceInterpolator()
        anim02.duration = 100
        anim02.startDelay = 10
        anim02.playTogether(anim0201, anim0202)
        //(3)靠拢后 播放合体特效
        anim02.addListener(onEnd = {
            //播放特效 播放声音
//            SoundPoolManager.instance.play(BIRD_COMBINE)
            playSound(BIRD_COMBINE)
            bird_combine_ani.show()
            ImageUtils.loadWebpImageLocal(bird_combine_ani, R.mipmap.anim_bird_combine)
            logger.info("动画播放完了 开始播放特效")
            bird_mask.hide()
            bird_mask2.hide()
            isActionDoing = false
            //如果生成了升级鹊 直接弹窗 这里不再往后处理
            if (result.functionInfo != null) {
                mViewModel.functionInfo.value = result.functionInfo
                //刷新整个页面
                mViewModel.queryHome()
                return@addListener
            }
            //3.将原有的两个鹊移除 将升级的鹊放入 目标格子
            val currentNew = result.currentUpgrade
            if (currentIndex != -1 && targetIndex != -1 && currentNew != null) {
                birdAdapter.setData(currentIndex, UpgradeBirdBean(upgradePos = currentIndex))
                birdAdapter.setData(targetIndex, currentNew)
            }
            if (isGuide3) {
                guide3?.dismiss()
            }
            if (result.currentUpgradeFirst) {
                currentNew ?: return@addListener
                val funBird =
                    FunctionBird(
                        functionName = currentNew.upgradeName,
                        functionIcon = currentNew.upgradeIcon,
                        level = currentNew.upgradeLevel
                    )
                if (mBirdGotFunctionDialogFragment == null) {
                    mBirdGotFunctionDialogFragment = BirdGotFunctionDialogFragment.newInstance(funBird)
                } else {
                    mBirdGotFunctionDialogFragment?.setBird(funBird)
                }
                mBirdGotFunctionDialogFragment?.show(requireActivity(), "BirdGotFunctionDialogFragment")
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