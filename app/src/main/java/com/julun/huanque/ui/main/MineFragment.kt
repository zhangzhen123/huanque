package com.julun.huanque.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.launcher.ARouter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.BuildConfig
import com.julun.huanque.R
import com.julun.huanque.activity.SettingActivity
import com.julun.huanque.common.base.BaseVMFragment
import com.julun.huanque.common.basic.NetState
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.bean.beans.RechargeAdInfo
import com.julun.huanque.common.bean.beans.UserDataTab
import com.julun.huanque.common.bean.beans.UserDetailInfo
import com.julun.huanque.common.bean.beans.UserTool
import com.julun.huanque.common.bean.events.LoginEvent
import com.julun.huanque.common.bean.events.PayResultEvent
import com.julun.huanque.common.bean.events.RHVerifyResult
import com.julun.huanque.common.bean.events.WithdrawSuccessEvent
import com.julun.huanque.common.constant.*
import com.julun.huanque.common.helper.MixedHelper
import com.julun.huanque.common.interfaces.routerservice.IRealNameService
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.ui.web.WebActivity
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.common.utils.ScreenUtils
import com.julun.huanque.common.utils.StatusBarUtil
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.common.widgets.bgabanner.BGABanner
import com.julun.huanque.core.ui.recharge.RechargeCenterActivity
import com.julun.huanque.core.ui.withdraw.WithdrawActivity
import com.julun.huanque.message.activity.ContactsActivity
import com.julun.huanque.viewmodel.MineViewModel
import com.julun.rnlib.RNPageActivity
import com.julun.rnlib.RnConstant
import kotlinx.android.synthetic.main.fragment_mine.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.startActivity

/**
 *@创建者   dong
 *@创建时间 2020/6/29 19:21
 *@描述 我的
 */
class MineFragment : BaseVMFragment<MineViewModel>() {

    companion object {
        fun newInstance() = MineFragment()
    }

    private val mIRealNameService: IRealNameService by lazy {
        ARouter.getInstance().build(ARouterConstant.REALNAME_SERVICE)
            .navigation() as IRealNameService
    }

    override fun getLayoutId() = R.layout.fragment_mine

    override fun isRegisterEventBus(): Boolean = true
    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        val lt = llTitleRootView.layoutParams as ConstraintLayout.LayoutParams
        lt.topMargin = StatusBarUtil.getStatusBarHeight(requireContext())

        rv_tabs_info.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        rv_tabs_info.adapter = infoTabAdapter
        infoTabAdapter.setOnItemClickListener { adapter, view, position ->
            val tempData = adapter.getItem(position) as? UserDataTab
            if (tempData?.userDataTabType != ContactsTabType.Visit) {
                ContactsActivity.newInstance(requireActivity(), tempData?.userDataTabType ?: "")
            }
        }

        rvUserTools.layoutManager = GridLayoutManager(context, 4)
        rvUserTools.adapter = toolsAdapter
        tvQueBi.setTFDinAltB()
        tvLingQian.setTFDinAltB()
        MixedHelper.setSwipeRefreshStyle(refreshView, requireContext())
        initViewModel()
    }

    private fun initViewModel() {
        mViewModel.userInfo.observe(this, Observer {
            refreshView.isRefreshing = false
            if (it.isSuccess()) {
                loadData(it.getT())
            }
        })
        mViewModel.checkAuthorResult.observe(this, Observer {
            if (it.isSuccess()) {
                RNPageActivity.start(requireActivity(), RnConstant.ANCHOR_CERT_PAGE)
            } else if (it.state == NetStateType.ERROR) {
                val error = it.error ?: return@Observer
                ToastUtils.show(error.busiMessage)
                when (error.busiCode) {
                    ErrorCodes.NOT_INFO_COMPLETE -> {
                        RNPageActivity.start(requireActivity(), RnConstant.EDIT_MINE_HOMEPAGE)
                    }
                    ErrorCodes.NOT_BIND_WECHAT -> {
                        //todo 跳转到账号与安全
                    }
                    ErrorCodes.NOT_REAL_NAME -> {
                        ARouter.getInstance().build(ARouterConstant.REAL_NAME_MAIN_ACTIVITY)
                            .navigation()
                    }
                }
            }
        })

        mViewModel.balance.observe(this, Observer {
            if (it != null) {
                tvQueBi.text = "$it"
            }
        })


    }

    private fun loadData(info: UserDetailInfo) {
        headImage.loadImage(info.userBasic.headPic+ BusiConstant.OSS_160, 60f, 60f)
        tvNickName.text = info.userBasic.nickname
        tvUserId.text = "欢鹊ID: ${info.userBasic.userId}"
        tvQueBi.text = "${info.userBasic.beans}"
        tvLingQian.text = info.userBasic.cash

        sdv_wealth.loadImage(info.userBasic.userLevelIcon, 50f, 16f)

        sdv_royal_level.loadImage(info.userBasic.royalLevelIcon, 50f, 16f)
        if (info.userBasic.anchorLevel > 0) {
            sdv_author_level.show()
            sdv_author_level.loadImage(info.userBasic.anchorLevelIcon, 50f, 16f)
        } else {
            tv_author_privilege.show()
        }


        if (info.userBasic.headRealPeople) {
            tvCertification.hide()
            ivReal.showContextMenu()
        } else {
            tvCertification.show()
            tvCertification.onClickNew {
                ARouter.getInstance().build(ARouterConstant.REAL_HEAD_ACTIVITY).navigation()
//                mIRealNameService.startRealHead(requireActivity(), object : RealNameCallback {
//                    override fun onCallback(status: String, des: String, percent: Int?) {
//                        if (status == RealNameConstants.TYPE_SUCCESS) {
//                            mViewModel.queryInfo(QueryType.REFRESH)
//                        } else {
//                            ToastUtils.show("认证失败，请稍后重试")
//                        }
//                    }
//                })

            }
            ivReal.hide()
        }
//        sd_wealth.loadImage(info.userBasic.royalLevel)
//        if (info.userBasic.sex == Sex.FEMALE) {
//            ivInviteFriend.hide()
//        } else {
//
//            ivInviteFriend.show()
//        }
        loadAd(info.adList)

        infoTabAdapter.setNewInstance(info.userDataTabList)
        toolsAdapter.setNewInstance(info.tools)
    }

    private fun loadAd(adList: MutableList<RechargeAdInfo>?) {
        if (adList != null) {
            if (adList.isEmpty()) {
                if (bannerAD.isVisible()) {
                    bannerAD?.setAutoPlayAble(false)
                    bannerAD?.setAllowUserScrollable(false)
                    bannerAD?.setData(adList, null)
                    bannerAD.hide()
                }
                return
            }
            bannerAD.show()
            bannerAD?.setAdapter(bannerAdapter)
            bannerAD?.setDelegate(bannerItemCick)
            bannerAD?.setData(adList, null)
            bannerAD?.setAutoPlayAble(adList.size > 1)
            bannerAD?.viewPager?.pageMargin = dp2px(10)
            if (adList.size > 1) {
                bannerAD?.currentItem = 0
            }
        } else {
            bannerAD?.hide()
        }

    }

    override fun initEvents(rootView: View) {
        clHeadRoot.onClickNew {
            RNPageActivity.start(requireActivity(), RnConstant.MINE_HOMEPAGE)
        }

        refreshView.setOnRefreshListener {
            mViewModel.queryInfo(QueryType.REFRESH)
        }
        toolsAdapter.setOnItemClickListener { _, _, position ->
            val item = toolsAdapter.getItemOrNull(position) ?: return@setOnItemClickListener
            when (item.toolType) {
                MineToolType.Office -> {
                    RNPageActivity.start(requireActivity(), RnConstant.OFFICIAL_CERT_PAGE)
                }
                MineToolType.RoomSpecial -> {

                }
                MineToolType.ChatBubble -> {

                }
                MineToolType.VisitHistory -> {

                }
                MineToolType.InviteFriend -> {
                    RNPageActivity.start(requireActivity(), RnConstant.INVITE_FRIENDS_PAGE)
                }
                MineToolType.ToAnchor -> {
                    mViewModel.checkToAnchor()
                }
            }
        }
        ivSetting.onClickNew {
            requireActivity().startActivity<SettingActivity>()
        }
        rlQueBi.onClickNew {
            requireActivity().startActivity<RechargeCenterActivity>()
        }
        rlLingQian.onClickNew {
            requireActivity().startActivity<WithdrawActivity>()
        }
        cl_user_wealth_level.onClickNew {
            RNPageActivity.start(requireActivity(), RnConstant.WEALTH_LEVEL_PAGE)
        }
        cl_royal_level.onClickNew {
            RNPageActivity.start(requireActivity(), RnConstant.ROYAL_PAGE)
        }
        cl_author_level.onClickNew {
            RNPageActivity.start(requireActivity(), RnConstant.ANCHOR_LEVEL_PAGE)
        }
        if (BuildConfig.DEBUG) {
            tv_test.show()
            tv_test.onClickNew {
                ARouter.getInstance().build(ARouterConstant.TEST_ACTIVITY).navigation()
            }
        } else {
            tv_test.hide()
        }
        tvService.onClickNew {
            val extra = Bundle()
            extra.putString(BusiConstant.WEB_URL, "http://q.url.cn/s/raPDfcm?_type=wpa")
            var intent = Intent(requireActivity(), WebActivity::class.java)
            intent.putExtras(extra)
            startActivity(intent)
        }


    }

    override fun showLoadState(state: NetState) {
        when (state.state) {
            NetStateType.SUCCESS -> {
                state_pager_view.showSuccess()
                refreshView.show()
            }
            NetStateType.LOADING -> {
                state_pager_view.showLoading()
            }
            NetStateType.ERROR, NetStateType.NETWORK_ERROR -> {
                refreshView.hide()
                state_pager_view.showError(showBtn = true, btnClick = View.OnClickListener {
                    mViewModel.queryInfo()
                })
            }

        }

    }

    override fun lazyLoadData() {
        mViewModel.queryInfo()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun receiveLoginCode(event: LoginEvent) {
        logger.info("登录事件:${event.result}")
        if (event.result) {
            mViewModel.queryInfo(QueryType.REFRESH)
        }
    }

    /**
     * 真人头像认证结果返回
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun receiveRH(event: RHVerifyResult) {
        logger("收到真人头像认证结果：${event.result}")
        if (event.result == RealNameConstants.TYPE_SUCCESS) {
            mViewModel.queryInfo(QueryType.REFRESH)
        }
    }

    /**
     * 提现成功 刷新
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun receiveWithdrawSuccess(event: WithdrawSuccessEvent) {
        logger("收到提现结果：${event.cash}")
        mViewModel.queryInfo(QueryType.REFRESH)
    }

    /**
     * 接收支付结果
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun receivePayResult(result: PayResultEvent) {
        logger.info("收到支付结果：${result.payResult} type=${result.payType}" )
        if (result.payResult == PayResult.PAY_SUCCESS) {
            mViewModel.queryInfo(QueryType.REFRESH)
        }
    }
    private val infoTabAdapter: BaseQuickAdapter<UserDataTab, BaseViewHolder> by lazy {
        object : BaseQuickAdapter<UserDataTab, BaseViewHolder>(R.layout.item_tab_user_info) {
            override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
                val holder = super.onCreateDefViewHolder(parent, viewType)
                val tv = holder.getViewOrNull<TextView>(R.id.tvCount)
                tv?.setTFDinCdc2()
                return holder
            }

            override fun convert(holder: BaseViewHolder, item: UserDataTab) {
                val tvCount = holder.getView<TextView>(R.id.tvCount)
                tvCount.text = "${item.count}"
                holder.setText(R.id.tvTitle, item.userTabName)

                if (item.tagCount == 0) {
                    holder.setGone(R.id.tv_tag, true).setText(R.id.tv_tag, "${item.tagCount}")
                } else {
                    holder.setGone(R.id.tv_tag, false)
                }
            }
        }
    }
    private val toolsAdapter: BaseQuickAdapter<UserTool, BaseViewHolder> by lazy {
        object : BaseQuickAdapter<UserTool, BaseViewHolder>(R.layout.item_user_tools) {

            override fun convert(holder: BaseViewHolder, item: UserTool) {
                val sdvTool = holder.getView<SimpleDraweeView>(R.id.sdv_tool)
                sdvTool.loadImage(item.icon, 34f, 34f)
                holder.setText(R.id.tvTitle, item.name)

            }
        }
    }


    private val bannerAdapter by lazy {
        BGABanner.Adapter<SimpleDraweeView, RechargeAdInfo> { _, itemView, model, _ ->
            when (model?.resType) {
                BannerResType.Pic -> {
                    val screenWidth = ScreenUtils.screenWidthFloat.toInt() - dp2px(15f) * 2
                    val height = dp2px(72f)
                    ImageUtils.loadImageInPx(itemView, model.resUrl, screenWidth, height)
                }
            }

        }
    }

    private val bannerItemCick by lazy {
        BGABanner.Delegate<SimpleDraweeView, RechargeAdInfo> { _, _, model, _ ->
            when (model?.touchType) {
                BannerTouchType.Url -> {
                    val extra = Bundle()
                    extra.putString(BusiConstant.WEB_URL, model.touchValue)
                    extra.putBoolean(IntentParamKey.EXTRA_FLAG_GO_HOME.name, false)
                    jump(WebActivity::class.java, extra = extra)
                }
                BannerTouchType.Toast -> {
                    //弹窗类型
                    if (model.touchValue == "FirstRecharge") {
                        //todo
//                        val rechargeInfo = mFirstRechargeViewModel?.oneYuanInfo?.value
//                            ?: return@Delegate
//                        OneYuanDialogFragment.newInstance(rechargeInfo).showPositive(childFragmentManager, "OneYuanDialogFragment")
                    }
                }
            }
        }
    }
}