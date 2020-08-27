package com.julun.huanque.core.ui.live.fragment

import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleEventObserver
import com.alibaba.android.arouter.launcher.ARouter
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.bean.beans.BottomActionBean
import com.julun.huanque.common.bean.beans.PrivateMessageBean
import com.julun.huanque.common.bean.events.OpenPrivateChatRoomEvent
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.constant.ClickType
import com.julun.huanque.common.constant.ParamConstant
import com.julun.huanque.common.viewmodel.PlayerMessageViewModel
import com.julun.huanque.core.R
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 *@创建者   dong
 *@创建时间 2020/8/5 9:26
 *@描述 直播间使用的私聊列表弹窗
 */
class PrivateFragment : BaseDialogFragment() {

    private var messageFragment: Fragment? = null
    override fun getLayoutId() = R.layout.fragment_private

    override fun initViews() {
        val bundle = Bundle()
        bundle.putBoolean(ParamConstant.PLAYER, true)
        messageFragment = ARouter.getInstance().build(ARouterConstant.MessageFragment).with(bundle).navigation() as? Fragment


    }


    override fun onStart() {
        super.onStart()
        setDialogSize(Gravity.BOTTOM, ViewGroup.LayoutParams.MATCH_PARENT, 480)
        showFragment()
    }


    override fun onStop() {
        super.onStop()
        removeFragment()
    }

    /**
     * 移除Fragment
     */
    private fun removeFragment() {
        val transaction = childFragmentManager.beginTransaction()
        messageFragment?.let {
            transaction.remove(it)
        }
        transaction.commitAllowingStateLoss()
        childFragmentManager.executePendingTransactions()
    }

    /**
     * 显示Fragment
     */
    private fun showFragment() {
        val transaction = childFragmentManager.beginTransaction()
        messageFragment?.let {
            transaction.add(R.id.frame, it)
        }
        transaction.commitAllowingStateLoss()
        childFragmentManager.executePendingTransactions()
    }

}