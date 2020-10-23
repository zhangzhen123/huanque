package com.julun.huanque.message.activity

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
import com.julun.huanque.common.bean.beans.FateInfo
import com.julun.huanque.common.bean.beans.FateQuickMatchChangeBean
import com.julun.huanque.common.constant.ParamConstant
import com.julun.huanque.common.helper.MixedHelper
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.ForceUtils
import com.julun.huanque.message.R
import com.julun.huanque.message.adapter.YuanFenAdapter
import com.julun.huanque.message.fragment.FateWeekDetailFragment
import com.julun.huanque.message.viewmodel.FateQuickMatchViewModel
import com.julun.huanque.message.viewmodel.YuanFenViewModel
import com.julun.rnlib.RNPageActivity
import com.julun.rnlib.RnConstant
import kotlinx.android.synthetic.main.act_yuanfen.*
import kotlinx.android.synthetic.main.act_yuanfen.commonView
import kotlinx.android.synthetic.main.act_yuanfen.rlRefreshView
import kotlinx.android.synthetic.main.activity_live_remind.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.imageResource

/**
 *@创建者   dong
 *@创建时间 2020/9/22 11:14
 *@描述 缘分页面
 */
class YuanFenActivity : BaseActivity() {

    companion object {

        fun newInstance(activity: Activity, count: Int) {
            val intent = Intent(activity, YuanFenActivity::class.java)
            if (ForceUtils.activityMatch(intent)) {
                intent.putExtra(ParamConstant.FATE_UNREPLY, count)
                activity.startActivity(intent)
            }
        }
    }

    private val mAdapter = YuanFenAdapter()

    //规则图片地址
    private var rulePicUrl = ""
    private val mViewModel: YuanFenViewModel by viewModels()

    private val mFateQuickMatchViewModel: FateQuickMatchViewModel by viewModels()

    private val mFateWeekDetailFragment = FateWeekDetailFragment()

    override fun getLayoutId() = R.layout.act_yuanfen

    override fun isRegisterEventBus() = true

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        val count = intent?.getIntExtra(ParamConstant.FATE_UNREPLY, 0) ?: 0
        updateTitle(count)
        header_page.imageOperation.show()
        header_page.imageOperation.imageResource = R.mipmap.icon_fate_help

        MixedHelper.setSwipeRefreshStyle(rlRefreshView)

        initRecyclerview()
        initViewModel()
        mViewModel.queryData.value = true
        mViewModel.getFateDetail()
    }

    override fun initEvents(rootView: View) {
        super.initEvents(rootView)
        header_page.imageViewBack.onClickNew { finish() }
        header_page.imageOperation.onClickNew {
            PicContentActivity.newInstance(this, rulePicUrl,"缘分速配说明")
        }

        rlRefreshView.setOnRefreshListener {
            mViewModel?.queryData?.value = true
        }
        iv_word.onClickNew {
            UsefulWordActivity.newInstance(this)
        }
        iv_arrow_up.onClickNew {
            view_performance.performClick()
        }
        view_performance.onClickNew {
            //显示弹窗
            mFateWeekDetailFragment.show(supportFragmentManager, "FateWeekDetailFragment")
        }
    }

    /**
     * 更新标题
     */
    private fun updateTitle(count: Int) {
        val title = if (count > 0) {
            "缘分（$count）"
        } else {
            "缘分"
        }
        header_page.textTitle.text = title
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
        mAdapter.setOnItemChildClickListener { adapter, view, position ->
            val tempData = mAdapter.getItem(position)
            when (view.id) {
                R.id.iv_reply -> {
                    mFateQuickMatchViewModel.getRandomWords(tempData.userId)
                }
                R.id.sdv_header -> {
                    RNPageActivity.start(
                        this,
                        RnConstant.PERSONAL_HOMEPAGE,
                        Bundle().apply { putLong("userId", tempData.userId) })
                }
                else -> {
                }
            }
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
                    if (mAdapter.itemCount <= 0) {
                        commonView.showLoading()
                    }
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
            rulePicUrl = it.extData?.ruleUrl ?: ""
            if (it.list.isNotEmpty()) {
                commonView.hide()
                if (it.isPull) {
                    rlRefreshView.isRefreshing = false
                    mAdapter.setList(it.list)
                    mViewModel.countDown()
                } else {
                    mAdapter.addData(it.list)
                }
                if (!it.hasMore) {
                    mAdapter.loadMoreModule.loadMoreEnd()
                } else {
                    mAdapter.loadMoreModule.loadMoreComplete()
                }
            } else {
                commonView.showEmpty(false, R.mipmap.icon_default_empty, "暂无数据", null, "去看看")
            }
        })

        mViewModel.refreshFlag.observe(this, Observer {
            if (it == true) {
                mAdapter.notifyDataSetChanged()
            }
        })
        mViewModel.fateWeekInfoBean.observe(this, Observer {
            if (it != null) {
                tv_left.text = it.result
                tv_middle.text = "${it.totalNum}"
                tv_right.text = "${it.replyNum}"
            }
        })
        mFateQuickMatchViewModel.msgData.observe(this, Observer {
            if (it != null) {
                //发言
                mViewModel.getFateDetail()
            }
        })
        mFateQuickMatchViewModel.showAlertFlag.observe(this, Observer {
            if (it == true) {
                //引导添加常用语
                MyAlertDialog(this).showAlertWithOKAndCancel(
                    "您还没有添加搭讪常用语，快去添加吧",
                    MyAlertDialog.MyDialogCallback(onRight = {
                        UsefulWordActivity.newInstance(this)
                    }), "提示", "去添加"
                )
                mFateQuickMatchViewModel.showAlertFlag.value = null
            }
        })
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun fateChange(bean: FateQuickMatchChangeBean) {
        updateTitle(bean.noReplyNum)
        mViewModel.updateFate(bean.fateInfo)
    }

}