package com.julun.huanque.core.ui.homepage

import android.view.Gravity
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.core.R
import com.julun.huanque.core.viewmodel.HomePageViewModel
import kotlinx.android.synthetic.main.fragment_home_page.*

/**
 *@创建者   dong
 *@创建时间 2020/10/15 14:12
 *@描述 主页操作Fragment
 */
class HomePageActionFragment : BaseDialogFragment() {

    private val mHomePageActionViewModel: HomePageViewModel by activityViewModels()
    override fun getLayoutId() = R.layout.fragment_home_page

    override fun initViews() {
        tv_report.onClickNew {
            mHomePageActionViewModel.actionData.value = HomePageViewModel.ACTION_REPORT
            dismiss()
        }

        tv_black.onClickNew {
            mHomePageActionViewModel.actionData.value = HomePageViewModel.ACTION_BLACK
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        initViewModel()
        setDialogSize(Gravity.BOTTOM, 0, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {
        mHomePageActionViewModel.blackStatus.observe(this, Observer {
            if (it == BusiConstant.True) {
                tv_black.text = "取消拉黑"
            } else {
                tv_black.text = "拉黑"
            }
        })

    }
}