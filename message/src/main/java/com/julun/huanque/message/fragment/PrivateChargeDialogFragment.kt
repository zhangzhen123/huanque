package com.julun.huanque.message.fragment

import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.message.R
import com.julun.huanque.message.viewmodel.MessageSettingViewModel
import kotlinx.android.synthetic.main.dialog_private_charge.*

/**
 * 切换私信费用弹窗
 * @author WanZhiYuan
 * @since 1.0.0
 * @date 2020/07/15
 */
class PrivateChargeDialogFragment : BaseDialogFragment() {

    private var mViewModel: MessageSettingViewModel? = null

    override fun getLayoutId(): Int = R.layout.dialog_private_charge

    override fun onStart() {
        super.onStart()
        val window = dialog?.window ?: return
        val params = window.attributes
        params.dimAmount = 0.1f
        window.attributes = params
        setDialogSize(
            width = ViewGroup.LayoutParams.MATCH_PARENT,
            height = ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun initViews() {
        btnCharge.onClickNew {
            mViewModel?.updateSetting(privateMsgFee = 30)
            dismiss()
        }
        btnFree.onClickNew {
            mViewModel?.updateSetting(privateMsgFee = 0)
            dismiss()
        }
        btnCancel.onClickNew {
            dismiss()
        }
        prepareViewModel()
    }

    override fun reCoverView() {
        prepareViewModel()
    }

    private fun prepareViewModel() {
        mViewModel = ViewModelProvider(activity ?: return).get(MessageSettingViewModel::class.java)
    }

}