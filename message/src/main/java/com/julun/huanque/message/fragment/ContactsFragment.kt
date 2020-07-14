package com.julun.huanque.message.fragment

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.julun.huanque.common.base.BaseFragment
import com.julun.huanque.common.base.BaseVMFragment
import com.julun.huanque.common.basic.NetState
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.basic.RootListData
import com.julun.huanque.common.bean.beans.FollowResultBean
import com.julun.huanque.common.bean.beans.HomeItemBean
import com.julun.huanque.common.bean.beans.SocialUserInfo
import com.julun.huanque.common.constant.ContactsTabType
import com.julun.huanque.common.constant.FollowStatus
import com.julun.huanque.common.constant.ParamConstant
import com.julun.huanque.common.helper.MixedHelper
import com.julun.huanque.common.suger.dp2px
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.GlobalUtils
import com.julun.huanque.message.R
import com.julun.huanque.message.activity.PrivateConversationActivity
import com.julun.huanque.message.adapter.ContactsAdapter
import com.julun.huanque.message.viewmodel.ContactsActivityViewModel
import com.julun.huanque.message.viewmodel.ContactsFragmentViewModel
import com.julun.rnlib.RNPageActivity
import com.julun.rnlib.RnConstant
import kotlinx.android.synthetic.main.fragment_contacts.*
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.textColor
import org.jetbrains.anko.textSizeDimen

/**
 *@创建者   dong
 *@创建时间 2020/7/7 10:22
 *@描述 联系人Fragment
 */
class ContactsFragment : BaseVMFragment<ContactsFragmentViewModel>() {

    companion object {
        fun newInstance(type: String): ContactsFragment {
            val fragment = ContactsFragment()
            val bundle = Bundle()
            bundle.putString(ParamConstant.TYPE, type)
            fragment.arguments = bundle
            return fragment
        }
    }

    //当前联系人type
    private val mAdapter = ContactsAdapter()

    private val mActivityViewModel: ContactsActivityViewModel by activityViewModels()

    override fun getLayoutId() = R.layout.fragment_contacts

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        initViewModel()
        state_pager_view.showEmpty()
        state_pager_view.show()
        mViewModel.mType = arguments?.getString(ParamConstant.TYPE, "") ?: ""
        mAdapter.type = mViewModel.mType

        initRecyclerView()
        //加载数据
        val socialList = mActivityViewModel.socialListData.value
        val list = socialList?.linkList
        if (socialList != null && list != null && mViewModel.mType == socialList.userDataTabType) {

        } else {
            //从后台获取数据
            mViewModel.queryInfo(QueryType.REFRESH)
        }

    }

    override fun initEvents(rootView: View) {
        swiperefreshlayout.setOnRefreshListener {
            mViewModel.queryInfo(QueryType.REFRESH)
        }
    }

    /**
     * 初始化RecyclerView
     */
    private fun initRecyclerView() {
        recycler_view.layoutManager = LinearLayoutManager(context)
        recycler_view.adapter = mAdapter
        val type = mViewModel.mType
        if (type == ContactsTabType.Intimate || type == ContactsTabType.Friend) {
            //增加头部
            val content = if (type == ContactsTabType.Intimate) {
                "亲密等级1级及以上，即可成为密友"
            } else {
                "双方相互，关注即可成为鹊友"
            }
            val headerView = TextView(context).apply {
                backgroundColor = GlobalUtils.formatColor("#FFFAEE")
                textColor = GlobalUtils.formatColor("#FFBA00")
                textSizeDimen = R.dimen.sp_12
                gravity = Gravity.CENTER_VERTICAL
                text = content
            }

            val params = LinearLayout.LayoutParams(MATCH_PARENT, dp2px(30))

            headerView.setPadding(dp2px(15), 0, 0, 0)
            mAdapter.addHeaderView(headerView)
            headerView.layoutParams = params

        }
        mAdapter.setOnItemClickListener { adapter, view, position ->
            val userId = mAdapter.getItem(position).userId
            RNPageActivity.start(
                requireActivity(),
                RnConstant.PERSONAL_HOMEPAGE,
                Bundle().apply { putLong("userId", userId) })
        }

        mAdapter.setOnItemChildClickListener { adapter, view, position ->
            val tempData = adapter.getItem(position) as? SocialUserInfo ?: return@setOnItemChildClickListener
            if (view.id == R.id.tv_action) {
                //点击操作按钮
                when (type) {
                    ContactsTabType.Intimate, ContactsTabType.Friend -> {
                        //点击私信
                        activity?.let { act ->
                            PrivateConversationActivity.newInstance(act, 20000041)
                        }
                    }
                    ContactsTabType.Follow, ContactsTabType.Fan -> {
                        //关注相关
                        when (tempData.follow) {
                            FollowStatus.Mutual, FollowStatus.True -> {
                                //执行取消关注操作
                                mViewModel.unFollow(type, tempData.userId)
                            }
                            FollowStatus.False -> {
                                //执行关注操作
                                mViewModel.follow(type, tempData.userId)
                            }
                            else -> {

                            }
                        }
                    }
                }
            }
        }
        val loadMoreModel = mAdapter.loadMoreModule
        loadMoreModel.setOnLoadMoreListener {
            mViewModel.queryInfo(QueryType.LOAD_MORE)
        }
    }

    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {
        mActivityViewModel.socialListData.observe(this, Observer {
            if (it != null && mViewModel.mType == it.userDataTabType) {
                mAdapter.setList(it.linkList)
                mViewModel.offset = it.linkList.size
                if (mViewModel.offset == 0) {
                    state_pager_view.showSuccess()
                }
                if (it.hasMore) {
                    mAdapter.loadMoreModule.loadMoreComplete()
                } else {
                    mAdapter.loadMoreModule.loadMoreEnd()
                }
            }
        })

        mViewModel.stateList.observe(viewLifecycleOwner, Observer {
            //
            swiperefreshlayout.isRefreshing = false
            if (it.state == NetStateType.SUCCESS) {
                loadData(it.getT())
            } else if (it.state == NetStateType.ERROR) {
                //dodo
            }
        })

        //用object形式，不然会报错
        mViewModel.followStatusData.observe(this, object : Observer<FollowResultBean> {
            override fun onChanged(it: FollowResultBean?) {
                if (it != null) {
                    //关注状态变动
                }
            }

        })
    }

    private fun loadData(stateList: RootListData<SocialUserInfo>) {

        if (stateList.isPull) {
            mAdapter.setList(stateList.list)
        } else {
            mAdapter.addData(stateList.list)
        }

        if (stateList.list.isNotEmpty()) {
            state_pager_view.showSuccess()
        }

        if (stateList.hasMore) {
//            mAdapter.loadMoreComplete()
            mAdapter.loadMoreModule.loadMoreComplete()
        } else {
            if (stateList.isPull) {
//                mAdapter.loadMoreEnd(true)
                mAdapter.loadMoreModule.loadMoreEnd(true)
            } else {
//                mAdapter.loadMoreEnd()
                mAdapter.loadMoreModule.loadMoreEnd()
            }

        }
    }

    override fun showLoadState(state: NetState) {
        when (state.state) {
            NetStateType.SUCCESS -> {//showSuccess()
            }
            NetStateType.LOADING -> {//showLoading()
                mAdapter.setEmptyView(MixedHelper.getLoadingView(requireContext()))
            }
            NetStateType.ERROR -> {
                mAdapter.setEmptyView(
                    MixedHelper.getErrorView(
                        ctx = requireContext(),
                        msg = state.message,
                        onClick = View.OnClickListener {
                            mViewModel.queryInfo(QueryType.INIT)
                        })
                )

            }
            NetStateType.NETWORK_ERROR -> {
                mAdapter.setEmptyView(
                    MixedHelper.getErrorView(
                        ctx = requireContext(),
                        msg = "网络错误",
                        onClick = View.OnClickListener {
                            mViewModel.queryInfo(QueryType.INIT)
                        })
                )

            }
        }
    }

    override fun lazyLoadData() {
    }
}