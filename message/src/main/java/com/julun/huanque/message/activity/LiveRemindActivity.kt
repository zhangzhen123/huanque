package com.julun.huanque.message.activity

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.launcher.ARouter
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.bean.beans.LiveRemindBeans
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.constant.ActivityCodes
import com.julun.huanque.common.helper.MixedHelper
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.onAdapterChildClickNew
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.message.R
import com.julun.huanque.message.adapter.LiveRemindAdapter
import com.julun.huanque.message.viewmodel.LiveRemindViewModel
import kotlinx.android.synthetic.main.activity_live_remind.*
import org.jetbrains.anko.backgroundResource

/**
 * 开播提醒
 * @author WanZhiYuan
 * @since 1.0.0
 * @date 2020/07/15 0015
 */
class LiveRemindActivity : BaseActivity() {

    private var mViewModel: LiveRemindViewModel? = null

    private var mIsChange: Boolean = false

    private var mClickItem: LiveRemindBeans? = null

    private val mAdapter: LiveRemindAdapter by lazy { LiveRemindAdapter() }

    override fun getLayoutId(): Int = R.layout.activity_live_remind

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        prepareViewModel()

        MixedHelper.setSwipeRefreshStyle(rlRefreshView)
        rvList.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        rvList.adapter = mAdapter
        mAdapter.loadMoreModule.isEnableLoadMore = true

        mViewModel?.queryData?.value = true
    }

    override fun onStart() {
        super.onStart()
        mViewModel?.queryData?.value = true
    }

    override fun initEvents(rootView: View) {
        ivback.onClickNew {
            onBackPressed()
        }
        mAdapter.onAdapterChildClickNew { _, view, position ->
            when (view?.id) {
                R.id.ivPush -> {
                    val item = mAdapter.data[position]
                    val push = if (item.pushOpen) {
                        "False"
                    } else {
                        "True"
                    }
                    mClickItem = item
                    mViewModel?.updatePush(push, item.programId)
                    mIsChange = true
                }
            }
        }
    }

    override fun onBackPressed() {
        if (mIsChange) {
            setResult(ActivityCodes.RESPONSE_CODE_REFRESH)
        }
        super.onBackPressed()
    }

    private fun prepareViewModel() {
        mViewModel = ViewModelProvider(this).get(LiveRemindViewModel::class.java)
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
                commonView.showEmpty(false, R.mipmap.icon_default_empty, "暂未关注主播，快去热门看看吧~", View.OnClickListener {
                    ARouter.getInstance().build(ARouterConstant.PLAYER_ACTIVITY).navigation()
                }, "去看看")
            }
        })
        mViewModel?.success?.observe(this, Observer
        {
            try {
                if (it == true && mClickItem != null) {
                    mClickItem!!.pushOpen = !mClickItem!!.pushOpen
                    val index = mAdapter.data.indexOf(mClickItem!!)
                    if (index != -1) {
                        mAdapter.notifyItemChanged(index, 1)
                    } else {
                        mAdapter.notifyDataSetChanged()
                    }
                    mClickItem = null
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
    }
}
