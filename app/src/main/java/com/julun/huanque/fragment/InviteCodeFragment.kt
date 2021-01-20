package com.julun.huanque.fragment

import android.view.Gravity
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.R
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.viewmodel.InviteCodeViewModel
import kotlinx.android.synthetic.main.frag_invite_code.*

/**
 *@创建者   dong
 *@创建时间 2021/1/19 10:12
 *@描述 邀请码弹窗
 */
class InviteCodeFragment : BaseDialogFragment() {
    private val mViewModel: InviteCodeViewModel by activityViewModels()
    override fun getLayoutId() = R.layout.frag_invite_code

    override fun initViews() {
        iniViewModel()
        tv_cancel.onClickNew { dismiss() }
        tv_save.onClickNew {
            val code = et.text.toString()
            if (code.length < 6) {
                //字符不足6位
                ToastUtils.show("邀请码不正确，请重新填写")
                et.setText("")
                return@onClickNew
            }
            //请求后台
            mViewModel.updateCard(code)
        }
    }


    /**
     * 初始化ViewModel
     */
    private fun iniViewModel() {
        mViewModel.clearFlag.observe(this, Observer {
            if (it == true) {
//                ToastUtils.show("邀请码不正确，请重新填写")
                et.setText("")
                mViewModel.clearFlag.value = false
            }
        })
    }


    override fun configDialog() {
        setDialogSize(Gravity.CENTER, 20, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}