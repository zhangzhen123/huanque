package com.julun.huanque.core.ui.live.fragment

import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.bean.beans.EggHitSumResult
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.ScreenUtils
import com.julun.huanque.core.R
import com.julun.huanque.core.adapter.EggResultItemAdapter
import com.julun.huanque.core.ui.live.PlayerViewModel
import kotlinx.android.synthetic.main.fragment_eggresult.*
import java.text.DecimalFormat


/**
 * Created by dong on 2018/7/5.
 */
class EggResultFragment : BaseDialogFragment() {


    companion object {
        fun newInstance() = EggResultFragment()
    }

    private val mAdapter = EggResultItemAdapter()
    private val playerViewModel: PlayerViewModel by activityViewModels()
    override fun getLayoutId() = R.layout.fragment_eggresult

    override fun initViews() {
        dialog?.setCanceledOnTouchOutside(false)
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
        playerViewModel.eggResultData.observe(this, Observer {
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

    private fun showView(result: EggHitSumResult) {
        tv_mengdou.show()
        val count = result.prizeBeans
//        val unit = if (count >= 10000) {
//            val df = DecimalFormat("#.00")
//            "${StringHelper.formatMengDou(df.format((count / 10000.toDouble())).toDouble())}万"
//        } else {
//            "${StringHelper.formatNumber(count)}"
//        }
//        tv_mengdou.text = "总计${StringHelper.formatCoinsCount(count)}鹊币"
        tv_mengdou.text = "总计${count}鹊币"
        mAdapter.setList(result.prizeList)
    }


    private fun initListeners() {
        tv_certain.onClickNew { dismiss() }
    }

    /**
     * 初始化RecyclerView
     */
    private fun initRecyclerView() {
        recyclerView_prize.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        recyclerView_prize.adapter = mAdapter
    }

//    override fun onStart() {
//        super.onStart()
//
//    }

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