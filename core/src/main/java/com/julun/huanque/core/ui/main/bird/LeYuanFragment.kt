package com.julun.huanque.core.ui.main.bird

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.RectF
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnticipateOvershootInterpolator
import android.view.animation.BounceInterpolator
import android.widget.FrameLayout
import androidx.core.animation.addListener
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.base.BaseVMFragment
import com.julun.huanque.common.basic.NetState
import com.julun.huanque.common.bean.beans.BirdHomeInfo
import com.julun.huanque.common.bean.beans.CombineResult
import com.julun.huanque.common.bean.beans.UpgradeBirdBean
import com.julun.huanque.common.constant.IntentParamKey
import com.julun.huanque.common.helper.StorageHelper
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.core.R
import kotlinx.android.synthetic.main.fragment_leyuan.*
import kotlinx.android.synthetic.main.view_pk_mic.view.*
import org.jetbrains.anko.sdk23.listeners.onTouch

/**
 *@创建者   dong
 *@创建时间 2020/6/29 18:06
 *@描述 乐园Fragment
 */
class LeYuanFragment : BaseVMFragment<LeYuanViewModel>() {
    companion object {
        fun newInstance() = LeYuanFragment()
    }


    var programId: Long? = null
    override fun getLayoutId() = R.layout.fragment_leyuan
    private val birdAdapter: BirdAdapter by lazy { BirdAdapter() }
    private var currentCombineItem: UpgradeBirdBean? = null
    private var currentTargetItem: UpgradeBirdBean? = null

    //记录目标item的holder 执行合并动画时用到
    private var currentTargetViewHolder: BaseViewHolder? = null

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
                        if (itemView != null) {
                            val viewHolder = rv_bird_packet.getChildViewHolder(itemView) as? BaseViewHolder
                            if (viewHolder != null) {
                                currentTargetItem = birdAdapter.getItemOrNull(viewHolder.adapterPosition)
                                currentTargetViewHolder = viewHolder
                                if (currentCombineItem != null && currentTargetItem != null) {
                                    logger.info("获取到指定目标item 开始执行操作 ${viewHolder.layoutPosition}")
                                    if (currentCombineItem == currentTargetItem) {
                                        logger.info("是同一个item 不处理")
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
                val currentIndex = birdAdapter.data.indexOf(currentCombineItem)
                if (currentIndex != -1) {
                    birdAdapter.setData(currentIndex, UpgradeBirdBean())
                }
            }
        })
        mViewModel.totalCoin.observe(this, Observer {
            if (it != null) {
                tv_balance.text = "余额${StringHelper.formatBigNum(it)}金币"
            }
        })
        mViewModel.coinsPerSec.observe(this, Observer {
            if (it != null) {
                tv_balance_produce.text = "${it}金币/秒"
            }
        })


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
        }


        birdAdapter.setList(info.upgradeList)

        tv_bird_price.text = "${info.unlockUpgrade.upgradeCoins}"
        tv_price_level.text = "Lv.${info.unlockUpgrade.upgradeLevel}"
        sdv_bottom_bird.loadImage(info.unlockUpgrade.upgradeIcon, 86f, 86f)
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
            //todo
            logger.info("动画播放完了 开始播放特效")
            //3.将原有的两个鹊移除 将升级的鹊放入 目标格子
            val currentNew = result.currentUpgrade
            if (currentIndex != -1 && targetIndex != -1 && currentNew != null) {
                birdAdapter.setData(currentIndex, UpgradeBirdBean())
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
}