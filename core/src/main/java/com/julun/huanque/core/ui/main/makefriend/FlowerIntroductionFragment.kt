package com.julun.huanque.core.ui.main.makefriend

import android.view.Gravity
import android.view.ViewGroup
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.core.R
import kotlinx.android.synthetic.main.fragment_flower_introduction.*

/**
 *@创建者   dong
 *@创建时间 2020/8/25 13:52
 *@描述 花魁 说明弹窗
 */
class FlowerIntroductionFragment : BaseDialogFragment() {
    override fun getLayoutId() = R.layout.fragment_flower_introduction

    override fun initViews() {
        tv_know.onClickNew {
            dismiss()
        }
    }


    override fun configDialog() {
        setDialogSize(Gravity.CENTER, 53, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}