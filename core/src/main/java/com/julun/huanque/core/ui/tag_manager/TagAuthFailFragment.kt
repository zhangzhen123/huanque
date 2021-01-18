package com.julun.huanque.core.ui.tag_manager

import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.constant.ParamConstant
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.core.R
import com.julun.huanque.core.viewmodel.HomePageViewModel
import com.julun.huanque.core.viewmodel.InviteFillViewModel
import kotlinx.android.synthetic.main.frag_invite_fill.*

/**
 *@创建者   dong
 *@创建时间 2021/1/12 9:21
 *@描述 邀请填写资料弹窗
 */
class TagAuthFailFragment : BaseDialogFragment() {
    companion object {
        /**
         * @param reason 拒绝原因
         */
        fun newInstance(reason: String): TagAuthFailFragment {
            val fragment = TagAuthFailFragment()
            val bundle = Bundle().apply {
                putString(ParamConstant.REASON, reason)
            }
            fragment.arguments = bundle
            return fragment
        }

    }

    override fun getLayoutId() = R.layout.frag_tag_auth_fail

    override fun initViews() {
        tv_invite.onClickNew {
            dismiss()
        }

        tv_content.text = arguments?.getString(ParamConstant.REASON) ?: ""
    }

    override fun configDialog() {
        setDialogSize(Gravity.CENTER, 20, ViewGroup.LayoutParams.WRAP_CONTENT)
    }


}