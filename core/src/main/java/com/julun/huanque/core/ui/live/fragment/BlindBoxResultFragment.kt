package com.julun.huanque.core.ui.live.fragment

import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.bean.beans.EggHitSumResult
import com.julun.huanque.common.bean.beans.SendGiftResult
import com.julun.huanque.common.constant.SPParamKey
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.suger.dp2px
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.SPUtils
import com.julun.huanque.common.utils.ScreenUtils
import com.julun.huanque.core.R
import com.julun.huanque.core.adapter.BlindResultItemAdapter
import com.julun.huanque.core.adapter.EggResultItemAdapter
import com.julun.huanque.core.ui.live.PlayerViewModel
import kotlinx.android.synthetic.main.fragment_eggresult.*
import org.jetbrains.anko.backgroundResource
import java.text.DecimalFormat


/**
 * Created by dong on 2018/7/5.
 * 盲盒弹窗
 */
class BlindBoxResultFragment : BaseDialogFragment() {


    companion object {
        fun newInstance() = BlindBoxResultFragment()
    }

    private val mAdapter = BlindResultItemAdapter()
    private val playerViewModel: PlayerViewModel by activityViewModels()
    override fun getLayoutId() = R.layout.fragment_eggresult

    override fun initViews() {
        dialog?.setCanceledOnTouchOutside(false)
        tv_no_attention.show()
        tv_certain.backgroundResource = R.mipmap.icon_blind_know
        val param = tv_certain.layoutParams as? ConstraintLayout.LayoutParams
        param?.bottomMargin = dp2px(44f)
        tv_certain.layoutParams = param

        initRecyclerView()
        setDefaultView()
        initListeners()
        initViewModel()
    }

    override fun reCoverView() {
        super.reCoverView()
        initViewModel()
        setDefaultView()
    }


    private fun initViewModel() {
        playerViewModel.sendBlindBoxResultData.observe(this, Observer {
            if (it != null) {
                showView(it)
            }
        })
    }

    /**
     * 显示默认视图
     */
    private fun setDefaultView() {
        if (mAdapter.itemCount > 0) {
            recyclerView_prize.scrollToPosition(0)
        }
        tv_mengdou.hide()
        mAdapter.setList(null)
    }

    private fun showView(result: SendGiftResult) {
        tv_mengdou.show()
        val count = result.prizeBeans
//        val unit = if (count >= 10000) {
//            val df = DecimalFormat("#.00")
//            "${StringHelper.formatMengDou(df.format((count / 10000.toDouble())).toDouble())}万"
//        } else {
//            "${StringHelper.formatNumber(count)}"
//        }
//        tv_mengdou.text = "总计${StringHelper.formatCoinsCount(count)}鹊币"
        tv_mengdou.text = "礼物已打赏主播，总计${count}鹊币"
        mAdapter.setList(result.feedbackList)
    }


    private fun initListeners() {
        tv_certain.onClickNew { dismiss() }
        tv_no_attention.onClickNew {
            tv_no_attention.isSelected = !tv_no_attention.isSelected
            SPUtils.commitBoolean(SPParamKey.Blind_Box_Show, !tv_no_attention.isSelected)
        }
    }

    /**
     * 初始化RecyclerView
     */
    private fun initRecyclerView() {
        recyclerView_prize.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        recyclerView_prize.adapter = mAdapter
    }


    override fun configDialog() {
        setDialogSize()
    }
    override fun setWindowAnimations() {
        dialog?.window?.setWindowAnimations(R.style.dialog_game_result_style)
    }

    /**
     * 默认显示在底部，充满屏幕宽度
     */
    private fun setDialogSize() {
        val window = dialog?.window ?: return
        val params = window.attributes
        params.gravity = Gravity.CENTER_VERTICAL
        params.height = ViewGroup.LayoutParams.MATCH_PARENT
        params.width = ViewGroup.LayoutParams.MATCH_PARENT
//        params.verticalMargin = dip(10).toFloat()
        window.attributes = params
    }
}