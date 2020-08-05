package com.julun.huanque.core.ui.live.fragment

import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.alibaba.android.arouter.launcher.ARouter
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.constant.ParamConstant
import com.julun.huanque.core.R

/**
 *@创建者   dong
 *@创建时间 2020/8/5 9:26
 *@描述 直播间使用的私聊列表弹窗
 */
class PrivateFragment : BaseDialogFragment() {

    override fun getLayoutId() = R.layout.fragment_private

    override fun initViews() {
        val bundle = Bundle()
        bundle.putBoolean(ParamConstant.PLAYER,true)
        val messageFragment = ARouter.getInstance().build(ARouterConstant.MessageFragment).with(bundle).navigation() as? Fragment

        val transaction = childFragmentManager.beginTransaction()
        messageFragment?.let {
            transaction.add(R.id.frame, it)
        }
        transaction.commitAllowingStateLoss()
        childFragmentManager.executePendingTransactions()

    }

    override fun onStart() {
        super.onStart()
        setDialogSize(Gravity.BOTTOM, ViewGroup.LayoutParams.MATCH_PARENT, 350)
    }
}