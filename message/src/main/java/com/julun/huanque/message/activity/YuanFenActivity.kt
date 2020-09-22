package com.julun.huanque.message.activity

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.bean.beans.FateInfo
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.message.R
import com.julun.huanque.message.adapter.YuanFenAdapter
import com.julun.huanque.message.viewmodel.YuanFenViewModel
import kotlinx.android.synthetic.main.act_yuanfen.*
import kotlinx.android.synthetic.main.act_yuanfen.commonView

/**
 *@创建者   dong
 *@创建时间 2020/9/22 11:14
 *@描述 缘分页面
 */
class YuanFenActivity : BaseActivity() {

    private val mAdapter = YuanFenAdapter()
    private val mViewModel: YuanFenViewModel by viewModels()

    override fun getLayoutId() = R.layout.act_yuanfen

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        header_page.textTitle.text = "缘分"
        initRecyclerview()
        initViewModel()
        mViewModel.queryData.value = true
    }

    override fun initEvents(rootView: View) {
        super.initEvents(rootView)
        header_page.imageViewBack.onClickNew { finish() }
    }

    /**
     * 初始化ViewModel
     */
    private fun initRecyclerview() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = mAdapter
        mAdapter.loadMoreModule.isEnableLoadMore = true
        mAdapter.loadMoreModule.setOnLoadMoreListener {
            mViewModel.queryData.value = false
        }
        mAdapter.setOnItemClickListener { adapter, view, position ->
            val fateInfo = adapter.getItem(position) as? FateInfo ?: return@setOnItemClickListener
            PrivateConversationActivity.newInstance(this, fateInfo.userId, fateInfo.nickname, fateInfo.headPic)
        }
    }

    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {
        mViewModel?.loadState?.observe(this, Observer {
            it ?: return@Observer
            when (it.state) {
                NetStateType.LOADING -> {
                    //加载中
                    commonView.showLoading("加载中~！")
                }
                NetStateType.SUCCESS -> {
                    //成功
//                    commonView.hide()
                }
                NetStateType.IDLE -> {
                    //闲置，什么都不做
                }
                else -> {
                    //都是异常
                    commonView.showError(errorTxt = "网络异常~！", btnClick = View.OnClickListener {
                        mViewModel?.queryData?.value = true
                    })
                }
            }
        })
        mViewModel?.result?.observe(this, Observer {
            it ?: return@Observer
            if (it.list.isNotEmpty()) {
                commonView.hide()
                if (it.isPull) {
                    mAdapter.setList(it.list)
                } else {
                    mAdapter.addData(it.list)
                }
                if (!it.hasMore) {
                    mAdapter.loadMoreModule.loadMoreEnd()
                } else {
                    mAdapter.loadMoreModule.loadMoreComplete()
                }
            } else {
//                commonView.showEmpty(false, R.mipmap.icon_default_empty, "暂未关注主播，快去热门看看吧~", View.OnClickListener {
//                    ARouter.getInstance().build(ARouterConstant.PLAYER_ACTIVITY).navigation()
//                }, "去看看")
            }
        })

        mViewModel.refreshFlag.observe(this, Observer {
            if (it == true) {
                mAdapter.notifyDataSetChanged()
            }
        })
    }

}