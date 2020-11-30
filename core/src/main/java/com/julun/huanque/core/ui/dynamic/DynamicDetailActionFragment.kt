package com.julun.huanque.core.ui.dynamic

import android.view.Gravity
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.core.R
import kotlinx.android.synthetic.main.fragment_dynamic_detail_action.*

/**
 *@创建者   dong
 *@创建时间 2020/11/30 11:54
 *@描述 动态详情操作弹窗
 */
class DynamicDetailActionFragment : BaseDialogFragment() {
    companion object {
        fun newInstance() = DynamicDetailActionFragment()
    }

    private val mViewModel: DynamicDetailViewModel by viewModels()
    override fun getLayoutId() = R.layout.fragment_dynamic_detail_action

    override fun initViews() {
        tv_del.onClickNew {
            mViewModel.deleteFlag.value = true
            dismiss()
        }
        tv_cancel.onClickNew {
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        initViewModel()
        setDialogSize(Gravity.BOTTOM, 0, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun initViewModel() {

    }
}