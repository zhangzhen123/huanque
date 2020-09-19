package com.julun.huanque.core.ui.main.bird

import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.core.R
import kotlinx.android.synthetic.main.fragment_bird_got_money.*

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/9/10 17:42
 *
 *@Description: 获取到金钱
 *
 */
class BirdGotMoneyDialogFragment : BaseDialogFragment() {


    companion object {
        fun newInstance(money: String): BirdGotMoneyDialogFragment {
            val args = Bundle()
            args.putString("money", money)
            val fragment = BirdGotMoneyDialogFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_bird_got_money
    }
    override fun setWindowAnimations() {
        dialog?.window?.setWindowAnimations(R.style.dialog_center_open_ani)
    }
    override fun onStart() {
        super.onStart()
        setDialogSize(gravity = Gravity.CENTER, marginWidth = 45, height = ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    fun setMoney(money: String) {
        arguments?.putString("money", money)
    }

    override fun initViews() {
        renderData()
        tv_ok.onClickNew {
            dismiss()
        }
    }

    private fun renderData() {
        val money = arguments?.getString("money")
        if (money != null) {
            tv_title.text = money
        }
    }

    override fun reCoverView() {
        renderData()
    }


}