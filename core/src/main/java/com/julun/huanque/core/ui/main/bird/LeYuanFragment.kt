package com.julun.huanque.core.ui.main.bird

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.base.BaseVMFragment
import com.julun.huanque.common.basic.NetState
import com.julun.huanque.common.bean.beans.BirdHomeInfo
import com.julun.huanque.common.constant.IntentParamKey
import com.julun.huanque.common.suger.loadImage
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.core.R
import kotlinx.android.synthetic.main.fragment_leyuan.*
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
        mask_container.onTouch { v, event ->
            logger.info("mask_container event=${event.action} rawX=${event.rawX} rawY=${event.rawY}")
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
//                    x = event.getRawX()
//                    y = event.getRawY()
                }
                MotionEvent.ACTION_MOVE -> {
                    val nowX = event.getRawX()
                    val nowY = event.getRawY()
//                    val movedX = nowX - x
//                    val movedY = nowY - y
//                    x = nowX
//                    y = nowY
//                    layoutParams?.x = (layoutParams?.x + movedX).toInt()
//                    layoutParams.y = layoutParams.y + movedY.toInt()
                    logger.info("nowX=$nowX nowY=$nowY")
                    bird_mask.x = event.rawX
                    bird_mask.y = event.rawY
                }
                MotionEvent.ACTION_UP -> {

                }
            }

            true
        }
        birdAdapter.onItemOnTouchListener = View.OnTouchListener { v, event ->

            if (event.action == MotionEvent.ACTION_DOWN) {
                logger.info("event=$event")
                val view = v as SimpleDraweeView
                val bmLp = bird_mask.layoutParams as FrameLayout.LayoutParams
                bmLp.width = view.width
                bmLp.height = view.height
                val location = IntArray(2)
                v.getLocationOnScreen(location)
//                bird_mask.x=event.rawX
                bird_mask.x = location[0].toFloat()
                bird_mask.y = location[1].toFloat()
                bird_mask.requestLayout()
                bird_mask.setImageDrawable(view.controller?.hierarchy?.topLevelDrawable)
            }
            false
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
        mViewModel.totalCoin.observe(this, Observer {
            if (it != null) {
                tv_balance.text = "余额${it}金币"
            }
        })
        mViewModel.coinsPerSec.observe(this, Observer {
            if (it != null) {
                tv_balance_produce.text = "${it}金币/秒"
            }
        })


    }

    private fun renderData(info: BirdHomeInfo) {
        info.functionInfo.let { fInfo ->
            sdv_cai_shen.loadImage(fInfo.wealth.functionIcon, 80f, 80f)

            sdv_bird_niu_lang.loadImage(fInfo.cowherd.functionIcon, 80f, 80f)

            sdv_bird_zhi_nv.loadImage(fInfo.weaver.functionIcon, 80f, 80f)

            sdv_bird_hong_bao.loadImage(fInfo.redpacket.functionIcon, 80f, 80f)

            sdv_bird_shen_mi.loadImage(fInfo.mystical.functionIcon, 80f, 80f)
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

}