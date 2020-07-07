package com.julun.huanque.message.fragment

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.julun.huanque.common.base.BaseFragment
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.bean.beans.FollowResultBean
import com.julun.huanque.common.bean.beans.SocialUserInfo
import com.julun.huanque.common.constant.ContactsTabType
import com.julun.huanque.common.constant.FollowStatus
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.message.R
import com.julun.huanque.message.activity.PrivateConversationActivity
import com.julun.huanque.message.adapter.ContactsAdapter
import com.julun.huanque.message.viewmodel.ContactsViewModel
import kotlinx.android.synthetic.main.fragment_contacts.*

/**
 *@创建者   dong
 *@创建时间 2020/7/7 10:22
 *@描述 联系人Fragment
 */
class ContactsFragment : BaseFragment() {

    companion object {
        const val TYPE = "TYPE"

        fun newInstance(type: String): ContactsFragment {
            val fragment = ContactsFragment()
            val bundle = Bundle()
            bundle.putString(TYPE, type)
            fragment.arguments = bundle
            return fragment
        }
    }

    //当前联系人type
    private var mType = ""
    private val mViewModel: ContactsViewModel by activityViewModels()
    private val mAdapter = ContactsAdapter()

    override fun getLayoutId() = R.layout.fragment_contacts

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        mType = arguments?.getString(TYPE, "") ?: ""
        mAdapter.type = mType

        initRecyclerView()
        initViewModel()

    }

    override fun initEvents(rootView: View) {
        swiperefreshlayout.setOnRefreshListener {

        }
    }

    /**
     * 初始化RecyclerView
     */
    private fun initRecyclerView() {
        recycler_view.layoutManager = LinearLayoutManager(context)
        recycler_view.adapter = mAdapter
        mAdapter.setOnItemClickListener { adapter, view, position -> }

        mAdapter.setOnItemChildClickListener { adapter, view, position ->
            val tempData = adapter.getItem(position) as? SocialUserInfo ?: return@setOnItemChildClickListener
            if (view.id == R.id.tv_action) {
                //点击操作按钮
                when (mType) {
                    ContactsTabType.Intimate, ContactsTabType.Friend -> {
                        //点击私信
                        activity?.let { act ->
                            PrivateConversationActivity.newInstance(act, 20000003)
                        }
                    }
                    ContactsTabType.Follow, ContactsTabType.Fan -> {
                        //关注相关
                        when (tempData.follow) {
                            FollowStatus.Mutual, FollowStatus.True -> {
                                //执行取消关注操作
                                mViewModel.unFollow(mType, tempData.userId)
                            }
                            FollowStatus.False -> {
                                //执行关注操作
                                mViewModel.follow(mType, tempData.userId)
                            }
                            else -> {

                            }
                        }
                    }

                }
            }
        }
    }

    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {
        mViewModel.socialListData.observe(this, Observer {
//            if (it != null && mType == it.userDataTabType) {
            if (it != null) {
                //当前tab的数据
                mAdapter.setNewData(it.linkList)
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
}