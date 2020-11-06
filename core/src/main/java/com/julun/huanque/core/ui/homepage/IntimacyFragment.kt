package com.julun.huanque.core.ui.homepage

import android.view.Gravity
import android.view.ViewGroup
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.core.R

/**
 *@创建者   dong
 *@创建时间 2020/11/5 19:35
 *@描述 本周亲密度弹窗
 */
class IntimacyFragment : BaseDialogFragment() {
    override fun getLayoutId() = R.layout.fragment_intimacy

    override fun initViews() {
    }

    override fun onStart() {
        super.onStart()
        setDialogSize(Gravity.BOTTOM, 0, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}