package com.julun.huanque.message.fragment

import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.message.R
import kotlinx.android.synthetic.main.fragment_prop.*
import org.jetbrains.anko.imageResource

/**
 *@创建者   dong
 *@创建时间 2020/9/22 20:42
 *@描述 道具说明弹窗
 */
@Route(path = ARouterConstant.PROP_FRAGMENT)
class PropFragment : BaseDialogFragment() {

    companion object {
        val VOICE = "VOICE"
        val TICKET_COUNT = "TICKET_COUNT"
        fun newInstance(voice: Boolean,count : Int = 0): PropFragment {
            val mFragment = PropFragment()
            val bundle = Bundle()
            bundle.putBoolean(VOICE, voice)
            bundle.putInt(TICKET_COUNT,count)
            mFragment.arguments = bundle
            return mFragment
        }
    }

    //是否是语音
    private var mVoice = false

    override fun getLayoutId() = R.layout.fragment_prop

    override fun initViews() {
        mVoice = arguments?.getBoolean(VOICE) ?: false
        if (mVoice) {
            //语音券
            tv_title.text = "语音券"
            iv_prop.imageResource = R.mipmap.icon_voice_big
            tv_content.text = "语音券可以抵扣语音通话1分钟，当你有优惠券时会优先使用"
            tv_get_content.text = "语音券在欢鹊乐园活跃奖励中产出"
        } else {
            //聊天券
            tv_title.text = "聊天券"
            iv_prop.imageResource = R.mipmap.icon_msg_big
            tv_content.text = "聊天券可抵扣一次付费信息聊天，当你有聊天券时会优先使用"
            tv_get_content.text = "聊天券在欢鹊乐园活跃奖励中产出"
        }
//        iv_close.onClickNew {
//            dismiss()
//        }
        tv_get.onClickNew {
            ARouter.getInstance().build(ARouterConstant.LEYUAN_BIRD_ACTIVITY).navigation()
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        setDialogSize(Gravity.BOTTOM, 0, ViewGroup.LayoutParams.WRAP_CONTENT)
        tv_count.text = "剩余：${arguments?.getInt(TICKET_COUNT) ?: 0}张"
    }
}