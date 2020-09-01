package com.julun.huanque.core.ui.main.bird

import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.launcher.ARouter
import com.julun.huanque.common.base.BaseVMDialogFragment
import com.julun.huanque.common.basic.NetState
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.constant.*
import com.julun.huanque.common.helper.MixedHelper
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.core.R
import com.julun.rnlib.RNPageActivity
import com.julun.rnlib.RnConstant
import com.julun.rnlib.RnManager
import kotlinx.android.synthetic.main.fragment_bird_tasks.*

/**
 * [leYuanViewModel]将主面板的viewModel传过来 供商店调用
 */
class BirdTaskDialogFragment(private val leYuanViewModel: LeYuanViewModel) : BaseVMDialogFragment<BirdTaskViewModel>() {


    private val taskAdapter: BirdTaskAdapter by lazy { BirdTaskAdapter() }
    override fun getLayoutId(): Int {
        return R.layout.fragment_bird_tasks
    }

    override fun onStart() {
        super.onStart()
        setDialogSize(width = ViewGroup.LayoutParams.MATCH_PARENT, height = 500)
    }

    override fun initViews() {
        initViewModel()
        tasksList.layoutManager = LinearLayoutManager(requireContext())
        tasksList.adapter = taskAdapter
        ivClose.onClickNew {
            dismiss()
        }
        taskAdapter.setOnItemChildClickListener { _, _, position ->
            val item = taskAdapter.getItemOrNull(position) ?: return@setOnItemChildClickListener
            when (item.taskStatus) {
                BirdTaskStatus.NotFinish -> {
                    when (item.jumpType) {
                        BirdTaskJump.FriendHome -> {
                            ARouter.getInstance().build(ARouterConstant.MAIN_ACTIVITY)
                                .withInt(IntentParamKey.TARGET_INDEX.name, MainPageIndexConst.MAIN_FRAGMENT_INDEX).navigation()
                        }
                        BirdTaskJump.InviteFriend -> {
                            RNPageActivity.start(requireActivity(), RnConstant.INVITE_FRIENDS_PAGE)
                        }

                        BirdTaskJump.LiveRoom -> {
                            ARouter.getInstance().build(ARouterConstant.PLAYER_ACTIVITY).navigation()
                        }
                        BirdTaskJump.Message -> {
                            ARouter.getInstance().build(ARouterConstant.MAIN_ACTIVITY)
                                .withInt(IntentParamKey.TARGET_INDEX.name, MainPageIndexConst.MESSAGE_FRAGMENT_INDEX).navigation()
                        }

                    }
                    this@BirdTaskDialogFragment.dismiss()
                }
                BirdTaskStatus.NotReceive -> {
                    mViewModel.receiveTask(item.taskCode)
                }
            }

        }
        mRefreshLayout.setOnRefreshListener {
            mViewModel.queryInfo(QueryType.REFRESH)
        }
        mViewModel.queryInfo()
    }

    override fun reCoverView() {
        initViewModel()
        mViewModel.queryInfo()
    }

    private fun initViewModel() {

        mViewModel.taskInfo.observe(this, Observer {
            mRefreshLayout.isRefreshing = false
            if (it.isSuccess()) {
                val data = it.requireT()
                taskAdapter.setNewInstance(data.taskList)
            }

        })
        mViewModel.receiveTaskResult.observe(this, Observer {
            if (it.isSuccess()) {
                mViewModel.queryInfo(QueryType.REFRESH)
            } else if (it.state == NetStateType.ERROR) {
                ToastUtils.show("${it.error?.busiMessage}")
            }
        })
    }

    override fun showLoadState(state: NetState) {
        when (state.state) {
            NetStateType.SUCCESS -> {//showSuccess()
                state_pager_view.showSuccess()
                mRefreshLayout.show()
                taskAdapter.setEmptyView(
                    MixedHelper.getEmptyView(
                        requireContext()
                    )
                )

            }
            NetStateType.LOADING -> {//showLoading()
                mRefreshLayout.hide()
                state_pager_view.showLoading()
            }
            NetStateType.ERROR, NetStateType.NETWORK_ERROR -> {
                state_pager_view.showError(showBtn = true, btnClick = View.OnClickListener {
                    mViewModel.queryInfo()
                })
            }
        }
    }
}