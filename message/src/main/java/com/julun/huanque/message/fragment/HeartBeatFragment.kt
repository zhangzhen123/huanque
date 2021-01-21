package com.julun.huanque.message.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.launcher.ARouter
import com.julun.huanque.common.base.BaseFragment
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.constant.MessageConstants
import com.julun.huanque.common.constant.ParamConstant
import com.julun.huanque.common.helper.MixedHelper
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.SessionUtils
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.message.R
import com.julun.huanque.message.activity.PrivateConversationActivity
import com.julun.huanque.message.adapter.HeartBeatAdapter
import com.julun.huanque.message.adapter.WatchAdapter
import com.julun.huanque.message.viewmodel.HeartBeatViewModel
import com.julun.huanque.message.viewmodel.WatchHistoryViewModel
import kotlinx.android.synthetic.main.frag_heart_beat.*
import kotlinx.android.synthetic.main.frag_watch.*
import kotlinx.android.synthetic.main.frag_watch.recycler_view
import kotlinx.android.synthetic.main.frag_watch.swipe_refresh

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

    private var mType = ""
    override fun getLayoutId() = R.layout.frag_heart_beat

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        mType = arguments?.getString(ParamConstant.TYPE) ?: ""
        initRecyclerView()
        initViewModel()
        mViewModel.queryData(mType, true)

        swipe_refresh.setOnRefreshListener {
            mViewModel.queryData(mType, true)
        }
    }


    override fun initEvents(rootView: View) {
        super.initEvents(rootView)
        tv_action.onClickNew {
            //点击事件
            var touchType = mViewModel.heartBeatData.value?.touchType ?: return@onClickNew
            when (touchType) {
                MessageConstants.MySign -> {
                    //个性签名
                    //UpdateSignActivity
                    mNeedRefresh = true
                    ARouter.getInstance().build(ARouterConstant.UpdateSignActivity).navigation()
                }
                MessageConstants.Voice -> {
                    //语音签名
                    //VOICE_SIGN_ACTIVITY
                    mNeedRefresh = true
                    ARouter.getInstance().build(ARouterConstant.VOICE_SIGN_ACTIVITY).navigation()
                }
                MessageConstants.HomeTown -> {
                    //家乡
                    //HomeTownActivity
                    mNeedRefresh = true
                    ARouter.getInstance().build(ARouterConstant.HomeTownActivity).navigation()
                }
                MessageConstants.Birthday -> {
                    //生日
                    //UpdateBirthdayActivity
                    mNeedRefresh = true
                    ARouter.getInstance().build(ARouterConstant.UpdateBirthdayActivity).navigation()
                }
                MessageConstants.Figure -> {
                    //身材
                    //FigureActivity
                    mNeedRefresh = true
                    ARouter.getInstance().build(ARouterConstant.FigureActivity).navigation()
                }
                MessageConstants.School -> {
                    //学校
                    //SchoolActivity
                    mNeedRefresh = true
                    ARouter.getInstance().build(ARouterConstant.SchoolActivity).navigation()
                }
                MessageConstants.Professional -> {
                    //职业
                    mNeedRefresh = true
                    ARouter.getInstance().build(ARouterConstant.ProfessionActivity).navigation()
                }
                MessageConstants.EditMineHomePage -> {
                    //编辑资料页面
                    mNeedRefresh = true
                    ARouter.getInstance().build(ARouterConstant.EDIT_INFO_ACTIVITY).navigation()
                }
                else -> {
                }
            }


        }
    }

    /**
     * 初始化RecyclerView
     */
    private fun initRecyclerView() {
        recycler_view.layoutManager = GridLayoutManager(context, 3)
        recycler_view.adapter = mAdapter
        mAdapter.setEmptyView(MixedHelper.getEmptyView(requireContext(), "暂无数据"))
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
                    cardView.hide()
                } else {
                    cardView.show()
                    tv_attention.text = it.guideText
                    tv_action.text = it.touchText
                    mActViewModel.unLockCount = it.remainUnlockTimes
                }
            }
        })
        mActViewModel.unlockLogId.observe(this, Observer {
            if (it != null) {
                mAdapter.data.forEachIndexed { index, singleHeartBean ->
                    if (singleHeartBean.logId == it) {
                        singleHeartBean.unLock = BusiConstant.True
                        mAdapter.notifyItemChanged(index)
                        return@Observer
                    }
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if (mNeedRefresh) {
            mViewModel.refreshGuide()
            mNeedRefresh = false
        }
    }

}