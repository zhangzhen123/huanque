package com.julun.huanque.message.fragment

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
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
class PropFragment : BaseDialogFragment() {

    companion object {
        val VOICE = "VOICE"
        fun newInstance(voice: Boolean): PropFragment {
            val mFragment = PropFragment()
            val bundle = Bundle()
            bundle.putBoolean(VOICE, voice)
            mFragment.arguments = bundle
            return mFragment
        }
    }

    override fun getLayoutId() = R.layout.fragment_prop

    override fun initViews() {
        val voice = arguments?.getBoolean(VOICE) ?: false
        if (voice) {
            //语音券
            tv_title.text = "语音券"
            iv_prop.imageResource = R.mipmap.icon_voice_big
            tv_content.text = "语音券可以抵扣语音通话1分钟，当你有语音券时会优先使用。语音券在养鹊乐园活跃奖励中产出。"
            tv_get.text = "免费领取语音券"
        } else {
            //聊天券
            tv_title.text = "聊天券"
            iv_prop.imageResource = R.mipmap.icon_msg_big
            tv_content.text = "聊天券可以抵扣一次付费信息聊天，当你有聊天券时会优先使用。聊天券在养鹊乐园活跃奖励中产出。"
            tv_get.text = "免费领取聊天券"
        }
        iv_close.onClickNew {
            dismiss()
        }
        tv_get.onClickNew {
            ARouter.getInstance().build(ARouterConstant.LEYUAN_BIRD_ACTIVITY).navigation()
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        setDialogSize(Gravity.CENTER, 50)
    }
}