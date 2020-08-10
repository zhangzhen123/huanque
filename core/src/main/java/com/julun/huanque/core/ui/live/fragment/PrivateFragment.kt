package com.julun.huanque.core.ui.live.fragment

import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
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

    private val mPlayerMessageViewModel: PlayerMessageViewModel by activityViewModels()

    override fun getLayoutId() = R.layout.fragment_private

    override fun isRegisterEventBus() = true

    override fun initViews() {
        val bundle = Bundle()
        bundle.putBoolean(ParamConstant.PLAYER, true)
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
        setDialogSize(Gravity.BOTTOM, ViewGroup.LayoutParams.MATCH_PARENT, 480)
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun privateShow(event: OpenPrivateChatRoomEvent) {
        //打开私聊列表
        EventBus.getDefault().removeStickyEvent(event)
        mPlayerMessageViewModel.privateConversationData.value = event
    }
}