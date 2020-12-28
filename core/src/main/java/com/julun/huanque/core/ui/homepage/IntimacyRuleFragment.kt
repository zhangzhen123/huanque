package com.julun.huanque.core.ui.homepage

import android.view.Gravity
import android.view.ViewGroup
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.core.R
import kotlinx.android.synthetic.main.fragment_intimacy_rule.*

/**
 *@创建者   dong
 *@创建时间 2020/11/9 11:16
 *@描述
 */
class IntimacyRuleFragment : BaseDialogFragment() {
    override fun getLayoutId() = R.layout.fragment_intimacy_rule

    override fun needEnterAnimation() = false

    override fun initViews() {
 
        tv_know.onClickNew {
            dismiss()
        }
    }

    override fun configDialog() {
        setDialogSize(Gravity.CENTER,  ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,52)
    }
}