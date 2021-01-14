package com.julun.huanque.core.ui.homepage

import android.graphics.Color
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.julun.huanque.common.base.BaseBottomSheetFragment
import com.julun.huanque.common.bean.beans.ProfessionInfo
import com.julun.huanque.common.helper.MixedHelper
import com.julun.huanque.common.suger.dp2px
import com.julun.huanque.core.R
import com.julun.huanque.core.adapter.ProfessionPeculiarityAdapter
import com.julun.huanque.core.viewmodel.HomePageViewModel
import kotlinx.android.synthetic.main.frag_job.*

/**
 *@创建者   dong
 *@创建时间 2020/12/29 14:26
 *@描述 职业弹窗
 */
class JobFragment : BaseBottomSheetFragment() {
    private val mHomePageViewModel: HomePageViewModel by activityViewModels()
    private val mAdapter = ProfessionPeculiarityAdapter()
    override fun getLayoutId() = R.layout.frag_job

    override fun initViews() {
        initRecyclerView()
    }

    override fun getHeight() = dp2px(423)
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

    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {
        mHomePageViewModel.homeInfoBean.observe(this, Observer {
            if (it != null) {
                showViewByData(it.profession)
            }
        })
    }

    /**
     * 初始化ViewModel
     */
    private fun initRecyclerView() {
        recycler_view_peculiarity.layoutManager = GridLayoutManager(requireContext(), 3)
        recycler_view_peculiarity.adapter = mAdapter
        mAdapter.setEmptyView(MixedHelper.getEmptyView(requireContext(),"TA还没有填写职业特点哦",true))
    }

    /**
     * 显示数据
     */
    private fun showViewByData(info: ProfessionInfo) {
        tv_industry.text = info.professionTypeText
        tv_profession.text = info.professionName
        val incomeText = info.incomeText
        if (incomeText.isNotEmpty()) {
            tv_income.text = incomeText
        } else {
            tv_income.text = "-"
        }
        mAdapter.setList(info.myFeatureList)
    }


}