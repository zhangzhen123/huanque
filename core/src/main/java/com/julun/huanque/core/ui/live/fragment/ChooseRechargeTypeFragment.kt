package com.julun.huanque.core.ui.live.fragment

import android.view.Gravity
import android.view.WindowManager
import androidx.fragment.app.activityViewModels
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.constant.PayType
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.core.R
import com.julun.huanque.common.viewmodel.FirstRechargeViewModel
import kotlinx.android.synthetic.main.fragment_choose_recharge_type.*

/**
 *@创建者   dong
 *@创建时间 2020/10/22 20:33
 *@描述 选择支付方式弹窗
 */
class ChooseRechargeTypeFragment : BaseDialogFragment() {
    private val mFirstRechargeViewModel: FirstRechargeViewModel by activityViewModels()
    override fun getLayoutId() = R.layout.fragment_choose_recharge_type

    override fun initViews() {
        iv_close.onClickNew {
            dismiss()
        }

        tv_wechat.onClickNew {
            //微信支付
            mFirstRechargeViewModel.payTypeFlag.value = PayType.WXPayApp
            dismiss()
        }

        tv_alipay.onClickNew {
            //支付宝支付
            mFirstRechargeViewModel.payTypeFlag.value = PayType.AliPayApp
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        setDialogSize(Gravity.BOTTOM, 0, 149)
        //不需要半透明遮罩层
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    }
}