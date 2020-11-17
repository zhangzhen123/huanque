package com.julun.huanque.core.ui.live.fragment

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.constant.BalanceNotEnoughType
import com.julun.huanque.common.constant.ParamConstant
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.ForceUtils
import com.julun.huanque.core.R
import com.julun.huanque.core.ui.main.bird.LeYuanBirdActivity
import com.julun.rnlib.RNPageActivity
import com.julun.rnlib.RnConstant
import kotlinx.android.synthetic.main.fragment_balance_not_enough.*

/**
 *@创建者   dong
 *@创建时间 2020/8/6 13:49
 *@描述 余额不足弹窗
 */
@Route(path = ARouterConstant.BalanceNotEnoughFragment)
class BalanceNotEnoughFragment : BaseDialogFragment() {

    companion object {
        fun newInstance(from: String): BalanceNotEnoughFragment {
            val fragment = BalanceNotEnoughFragment()
            val bundle = Bundle()
            bundle.putString(ParamConstant.TYPE, from)
            fragment.arguments = bundle
            return fragment
        }

    }

    override fun getLayoutId() = R.layout.fragment_balance_not_enough

    override fun needEnterAnimation() = true

    override fun initViews() {
        tv_attention_voice
        initEvents()
        val type = arguments?.getString(ParamConstant.TYPE, "")
        if (type == BalanceNotEnoughType.Small) {
            //直播间内显示
            tv_recharge_content.text = "送礼要趁热，别让Ta被人撩走"
            view_task.hide()
            iv_task.hide()
            tv_task_title.hide()
            tv_task_content.hide()
            tv_task_action.hide()
        } else {
            //其他地方显示
            tv_recharge_content.text = "聊天要趁热，别让Ta被人撩走"
        }
        if (type == BalanceNotEnoughType.Voice) {
            //语音券跳转
            tv_attention_voice.show()
            tv_task_content.text = "即可免费获得语音券"

        } else {
            tv_attention_voice.hide()
            tv_task_content.text = "即可获得免费聊天券"
        }
    }

    private fun initEvents() {
        iv_close.onClickNew {
            dismiss()
        }
        view_recharge.onClickNew {
            //去充值
            ARouter.getInstance().build(ARouterConstant.RECHARGE_ACTIVITY).navigation()
            dismiss()
        }

        view_invite.onClickNew {
            //去邀请
            RNPageActivity.start(requireActivity(), RnConstant.INVITE_FRIENDS_PAGE)
            dismiss()
        }

        view_task.onClickNew {
            //去完成 进入养鹊乐园
            val act = requireActivity()
            val intent = Intent(act, LeYuanBirdActivity::class.java)
            if (ForceUtils.activityMatch(intent)) {
                act.startActivity(intent)
            }
            dismiss()
        }
    }

    override fun setWindowAnimations() {
        dialog?.window?.setWindowAnimations(R.style.dialog_bottom_enter_style)
    }

    override fun onStart() {
        super.onStart()
        setDialogSize(Gravity.CENTER, 25)
    }
}