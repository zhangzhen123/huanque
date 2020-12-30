package com.julun.huanque.core.ui.homepage

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.julun.huanque.common.base.BaseBottomSheetFragment
import com.julun.huanque.common.bean.beans.FigureBean
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.suger.dp2px
import com.julun.huanque.common.suger.loadImageNoResize
import com.julun.huanque.core.R
import com.julun.huanque.core.adapter.TagListAdapter
import com.julun.huanque.core.viewmodel.HomePageViewModel
import kotlinx.android.synthetic.main.frag_figure.*
import kotlinx.android.synthetic.main.frag_tag.*

/**
 *@创建者   dong
 *@创建时间 2020/12/28 9:30
 *@描述 身材弹窗
 */
class FigureFragment : BaseBottomSheetFragment() {

    private val mHomeViewModel: HomePageViewModel by activityViewModels()


    override fun getLayoutId() = R.layout.frag_figure


    override fun initViews() {
    }

    override fun onStart() {
        super.onStart()
//        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        val win = dialog?.window ?: return
        win.setWindowAnimations(R.style.dialog_bottom_bottom_style)
        initViewModel()
        val parent = view?.parent
        if (parent is View) {
            parent.setBackgroundColor(Color.TRANSPARENT)
        }
    }


    override fun getHeight() = dp2px(480)

    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {
        mHomeViewModel.homeInfoBean.observe(this, Observer {
            if (it != null) {
                showViewByData(it.figure)
            }
        })
    }

    /**
     * 显示身材数据
     */
    private fun showViewByData(data: FigureBean) {
//        sdv_figure.loadImageNoResize(data.)
        tv_height.text = "${data.height}cm"
        tv_weight.text = "${data.weight}kg"
        tv_suggest.text = data.suggest
    }


}