package com.julun.huanque.core.ui.homepage

import android.view.Gravity
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.core.R
import com.julun.huanque.core.viewmodel.HomePageViewModel
import com.julun.huanque.core.viewmodel.InviteFillViewModel
import kotlinx.android.synthetic.main.frag_invite_fill.*

/**
 *@创建者   dong
 *@创建时间 2021/1/12 9:21
 *@描述 邀请真人认证弹窗
 */
class RealPeopleAttentionFragment : BaseDialogFragment() {
    private val mViewModel : HomePageViewModel by activityViewModels()
    override fun getLayoutId() = R.layout.frag_real_people_attention

    override fun initViews() {
        tv_cancel.onClickNew {
            dismiss()
        }
        tv_invite.onClickNew {
//            mViewModel.inviteFill()
            mViewModel.realPeopleState.value = true
            dismiss()
        }

    }

    override fun configDialog() {
        setDialogSize(Gravity.CENTER, 20, ViewGroup.LayoutParams.WRAP_CONTENT)
    }


}