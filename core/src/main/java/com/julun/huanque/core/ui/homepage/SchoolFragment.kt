package com.julun.huanque.core.ui.homepage

import android.graphics.Color
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.julun.huanque.common.base.BaseBottomSheetFragment
import com.julun.huanque.common.bean.beans.SchoolInfo
import com.julun.huanque.common.suger.dp2px
import com.julun.huanque.core.R
import com.julun.huanque.core.viewmodel.HomePageViewModel
import kotlinx.android.synthetic.main.frag_school.*

/**
 *@创建者   dong
 *@创建时间 2020/12/29 14:26
 *@描述 职业弹窗
 */
class SchoolFragment : BaseBottomSheetFragment() {
    private val mHomePageViewModel: HomePageViewModel by activityViewModels()
    override fun getLayoutId() = R.layout.frag_school

    override fun initViews() {
    }

    override fun getHeight() = dp2px(480)
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
                showViewByData(it.schoolInfo)
            }
        })
    }

    /**
     * 显示数据
     */
    private fun showViewByData(info: SchoolInfo) {
        tv_school.text = info.school
        tv_education.text = info.education
    }


}