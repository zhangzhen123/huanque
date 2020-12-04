package com.julun.huanque.message.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.launcher.ARouter
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.bean.beans.SingleAccount
import com.julun.huanque.common.bean.beans.SingleAccountMsg
import com.julun.huanque.common.bean.events.LoginEvent
import com.julun.huanque.common.bean.events.LoginSubAccountEvent
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.constant.IntentParamKey
import com.julun.huanque.common.constant.MainPageIndexConst
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.utils.ForceUtils
import com.julun.huanque.common.utils.SessionUtils
import com.julun.huanque.message.R
import com.julun.huanque.message.adapter.AccountMsgAdapter
import com.julun.huanque.message.viewmodel.SubAccountMessageViewModel
import kotlinx.android.synthetic.main.act_sub_account_message.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 *@创建者   dong
 *@创建时间 2020/12/3 14:27
 *@描述 分身账号消息页面
 */
class SubAccountMessageActivity : BaseActivity() {
    private val mViewModel: SubAccountMessageViewModel by viewModels()
    private val mAdapter = AccountMsgAdapter()

    override fun getLayoutId() = R.layout.act_sub_account_message

    override fun isRegisterEventBus() = true

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        header_page.textTitle.text = "分身账号"
        initRecyclerView()
        initViewModel()
        mViewModel.getMsgList()
    }

    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {
        mViewModel.msgList.observe(this, Observer {
            if (it != null) {
                mAdapter.setList(it.msgList)
            }
        })
    }

    override fun initEvents(rootView: View) {
        super.initEvents(rootView)
        header_page.imageViewBack.onClickNew {
            finish()
        }
    }

    /**
     * 初始化RecyclerView
     */
    private fun initRecyclerView() {
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.adapter = mAdapter
        mAdapter.setOnItemClickListener { adapter, view, position ->
            val account = adapter.getItemOrNull(position) as? SingleAccountMsg ?: return@setOnItemClickListener
            if (account.userId != SessionUtils.getUserId()) {
                //登录分身账号
                EventBus.getDefault().post(LoginSubAccountEvent(account.userId))
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun loginEvent(event: LoginEvent) {
        if (event.result) {
            //登录成功，重新获取数据
//            mViewModel.getMsgList()

            ARouter.getInstance().build(ARouterConstant.MAIN_ACTIVITY)
                .withInt(IntentParamKey.TARGET_INDEX.name, MainPageIndexConst.MAIN_FRAGMENT_INDEX)
                .navigation()
        }
    }


}