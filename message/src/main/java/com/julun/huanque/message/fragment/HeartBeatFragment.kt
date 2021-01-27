package com.julun.huanque.message.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.alibaba.android.arouter.launcher.ARouter
import com.julun.huanque.common.base.BaseFragment
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.bean.forms.HeartBeanForm
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.constant.ParamConstant
import com.julun.huanque.common.helper.AppHelper
import com.julun.huanque.common.helper.MixedHelper
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.message.R
import com.julun.huanque.message.adapter.HeartBeatAdapter
import com.julun.huanque.message.viewmodel.HeartBeatViewModel
import kotlinx.android.synthetic.main.frag_heart_beat.*

/**
 *@创建者   dong
 *@创建时间 2021/1/18 20:11
 *@描述 访问历史
 */
class HeartBeatFragment : BaseFragment() {
    companion object {
        fun newInstance(type: String): HeartBeatFragment {
            val fragment = HeartBeatFragment()
            fragment.arguments = Bundle().apply { putString(ParamConstant.TYPE, type) }
            return fragment
        }
    }

    //需要刷新的标记位
    private var mNeedRefresh = false

    private val mViewModel: HeartBeatViewModel by viewModels()

    private val mActViewModel: HeartBeatViewModel by activityViewModels()

    private val mAdapter = HeartBeatAdapter()

    //解锁弹窗
    private val mHeartUnLockFragment: HeartUnLockFragment by lazy { HeartUnLockFragment() }

    private var tv_action: TextView? = null
    private var tv_attention: TextView? = null
    private var mHeaderView: View? = null

    private var mType = ""
    override fun getLayoutId() = R.layout.frag_heart_beat

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        MixedHelper.setSwipeRefreshStyle(swipe_refresh)
        mType = arguments?.getString(ParamConstant.TYPE) ?: ""
        initRecyclerView()
        initViewModel()
        state_pager_view.showLoading()

        mViewModel.queryData(mType, true)

        swipe_refresh.setOnRefreshListener {
            mViewModel.queryData(mType, true)
        }
    }


    override fun initEvents(rootView: View) {
        super.initEvents(rootView)
        tv_action?.onClickNew {
            //点击事件
            var touchType = mViewModel.guideInfo.value?.touchType ?: return@onClickNew
            mNeedRefresh = true
            AppHelper.openTouch(touchType, activity = requireActivity())
        }
    }

    /**
     * 初始化RecyclerView
     */
    private fun initRecyclerView() {
        recycler_view.layoutManager = GridLayoutManager(context, 3)
        recycler_view.adapter = mAdapter
        if (mType == HeartBeanForm.HeartTouchToMe) {
            val headerView = LayoutInflater.from(requireContext()).inflate(R.layout.header_heart_beat, null)
            tv_action = headerView.findViewById(R.id.tv_action)
            tv_attention = headerView.findViewById(R.id.tv_attention)
            mAdapter.addHeaderView(headerView)
            mHeaderView = headerView
        }

        mAdapter.headerWithEmptyEnable = true
        mAdapter.setEmptyView(MixedHelper.getEmptyView(requireContext(), "暂无数据", imgResId = R.mipmap.icon_no_data_01))
        mAdapter.setOnItemClickListener { adapter, view, position ->
            val tempData = mAdapter.getItemOrNull(position) ?: return@setOnItemClickListener
            if (tempData.unLock == BusiConstant.True) {
                //跳转主页
                //本人发送消息
                val bundle = Bundle().apply {
                    putLong(ParamConstant.UserId, tempData.userId)
                }
                ARouter.getInstance().build(ARouterConstant.HOME_PAGE_ACTIVITY).with(bundle).navigation()
            } else {
                //未解锁
                val unlockCount = mViewModel.unLockCount
                if (unlockCount <= 0) {
                    ToastUtils.show("您还没有解锁次数，完善资料可获得")
                } else {
                    mActViewModel.mSingleHeartBean = tempData
                    mHeartUnLockFragment.show(childFragmentManager, "HeartUnLockFragment")
                }
            }
        }

        mAdapter.loadMoreModule.setOnLoadMoreListener {
            mViewModel.queryData(mType)
        }
    }

    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {
        mViewModel.loadState.observe(this, Observer {
            swipe_refresh.isRefreshing = false
            when (it.state) {
                NetStateType.SUCCESS -> {
                    state_pager_view.showSuccess()
                }
                NetStateType.ERROR -> {
                    state_pager_view.showError(R.mipmap.icon_no_data_01)
                }
                NetStateType.NETWORK_ERROR -> {
                    state_pager_view.showError(btnClick = View.OnClickListener {
                        state_pager_view.showLoading()
                        mViewModel.queryData(mType, true)
                    })
                }
            }
        })
        mViewModel.heartBeatData.observe(this, Observer {
            if (it != null) {
                swipe_refresh.isRefreshing = false
                if (it.isPull) {
                    mAdapter.setList(it.list)
                } else {
                    //加载更多
                    mAdapter.addData(it.list)
                }
                if (it.hasMore) {
                    mAdapter.loadMoreModule.loadMoreComplete()
                } else {
                    mAdapter.loadMoreModule.loadMoreEnd()
                }
            }
        })

        mViewModel.guideInfo.observe(this, Observer {
            if (it != null) {
                //刷新
                if (it.guideText.isEmpty()) {
                    mHeaderView?.hide()
                } else {
                    mHeaderView?.show()
                    tv_attention?.text = it.guideText
                    tv_action?.text = it.touchText
                    mActViewModel.unLockCount = it.remainUnlockTimes
                }
            }
        })
        if (mType == HeartBeanForm.HeartTouchToMe) {
            mActViewModel.unlockLogId.observe(this, Observer {
                if (it != null) {
                    mAdapter.data.forEachIndexed { index, singleHeartBean ->
                        if (singleHeartBean.logId == it) {
                            singleHeartBean.unLock = BusiConstant.True
                            mAdapter.notifyItemChanged(index + mAdapter.headerLayoutCount)
                            return@Observer
                        }
                    }
                }
            })
        }

    }

    override fun onResume() {
        super.onResume()
        if (mNeedRefresh) {
            mViewModel.refreshGuide()
            mNeedRefresh = false
        }
    }

}