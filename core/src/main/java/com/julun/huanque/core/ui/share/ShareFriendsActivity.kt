package com.julun.huanque.core.ui.share

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.base.dialog.MyAlertDialog
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.bean.beans.PostShareBean
import com.julun.huanque.common.bean.beans.SocialUserInfo
import com.julun.huanque.common.constant.ParamConstant
import com.julun.huanque.common.helper.MixedHelper
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.utils.ForceUtils
import com.julun.huanque.core.R
import com.julun.huanque.core.adapter.ShareContacsAdapter
import com.julun.huanque.core.viewmodel.ShareFriendsViewModel
import kotlinx.android.synthetic.main.act_share_friends.*
import kotlinx.android.synthetic.main.act_share_friends.recyclerView
import kotlinx.android.synthetic.main.fragment_day_list.*

/**
 *@创建者   dong
 *@创建时间 2020/11/26 9:38
 *@描述 站内分享  选择密友页面
 */
class ShareFriendsActivity : BaseActivity() {

    companion object {
        fun newInstance(act: Activity, post: PostShareBean, code: Int) {
            val intent = Intent(act, ShareFriendsActivity::class.java)
            if (ForceUtils.activityMatch(intent)) {
                intent.putExtra(ParamConstant.PostShareInfo, post)
                act.startActivityForResult(intent, code)
            }
        }
    }

    //获取密友的viewmodel
    private val mViewModel: ShareFriendsViewModel by viewModels()

    //选择联系人Adapter
    private val mShareContacsAdapter = ShareContacsAdapter()

    override fun getLayoutId() = R.layout.act_share_friends

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        mViewModel.mPostShareBean = intent?.getSerializableExtra(ParamConstant.PostShareInfo) as? PostShareBean

        header_page.textTitle.text = "分享密友"
        initViewModel()
        initRecyclerView()
        mViewModel.queryContacets()

    }

    /**
     * 初始化RecyclerView
     */
    private fun initRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = mShareContacsAdapter
        mShareContacsAdapter.setEmptyView(MixedHelper.getEmptyView(this, "暂无密友，试试分享到站外吧~"))
        mShareContacsAdapter.setOnItemClickListener { adapter, view, position ->
            val user = adapter.getItemOrNull(position) as? SocialUserInfo ?: return@setOnItemClickListener
            MyAlertDialog(this).showAlertWithOKAndCancel(
                "是否将该内容分享给Ta？",
                MyAlertDialog.MyDialogCallback(onRight = {
                    mViewModel.sendPostMessage(user,user.userId)
                    setResult(Activity.RESULT_OK)
                    finish()
                }), "提示", okText = "确定"
            )
        }

        mShareContacsAdapter.loadMoreModule.setOnLoadMoreListener {
            mViewModel.queryContacets()
        }
    }

    override fun initEvents(rootView: View) {
        super.initEvents(rootView)
        header_page.imageViewBack.onClickNew {
            finish()
        }
    }

    private fun initViewModel() {
        mViewModel.socialListBean.observe(this, Observer {
            if (it != null) {
                val list = it.linkList
                mShareContacsAdapter.addData(list)
                if (it.hasMore) {
                    //还有更多
                    mShareContacsAdapter.loadMoreModule.loadMoreComplete()
                } else {
                    //没有更多数据
                    mShareContacsAdapter.loadMoreModule.loadMoreEnd()
                }

            }
        })

        mViewModel.loadState.observe(this, Observer {
            if (it != null) {
                when (it.state) {
                    NetStateType.NETWORK_ERROR -> {
                        state_pager_view.showError()
                    }
                    NetStateType.SUCCESS -> {
                        state_pager_view.showSuccess()
                    }
                    else -> {
                    }
                }

            }
        })
    }
}