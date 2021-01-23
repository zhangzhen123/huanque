package com.julun.huanque.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.launcher.ARouter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder
import com.facebook.drawee.generic.RoundingParams
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.BuildConfig
import com.julun.huanque.R
import com.julun.huanque.activity.SettingActivity
import com.julun.huanque.common.base.BaseVMFragment
import com.julun.huanque.common.base.dialog.CommonDialogFragment
import com.julun.huanque.common.base.dialog.MyAlertDialog
import com.julun.huanque.common.basic.NetState
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.basic.ResponseError
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.bean.events.*
import com.julun.huanque.common.constant.*
import com.julun.huanque.common.helper.MixedHelper
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.interfaces.routerservice.IRealNameService
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.ui.web.WebActivity
import com.julun.huanque.common.utils.*
import com.julun.huanque.common.widgets.bgabanner.BGABanner
import com.julun.huanque.core.ui.homepage.EditInfoActivity
import com.julun.huanque.core.ui.homepage.HomePageActivity
import com.julun.huanque.core.ui.recharge.RechargeCenterActivity
import com.julun.huanque.fragment.InviteCodeFragment
import com.julun.huanque.fragment.UpdateInfoFragment
import com.julun.huanque.message.activity.ContactsActivity
import com.julun.huanque.ui.safe.AccountAndSecurityActivity
import com.julun.huanque.viewmodel.InviteCodeViewModel
import com.julun.huanque.viewmodel.MainViewModel
import com.julun.huanque.viewmodel.MineViewModel
import com.julun.rnlib.RNPageActivity
import com.julun.rnlib.RnConstant
import kotlinx.android.synthetic.main.fragment_mine.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.backgroundResource
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

    private val mMainViewModel: MainViewModel by activityViewModels()

    //邀请码ViewModel
    private val mInviteViewModel: InviteCodeViewModel by activityViewModels()

    //是否需要刷新数据
    private var mNeedRefresh = false

    //邀请弹窗
    private val mInviteCodeFragment: InviteCodeFragment by lazy { InviteCodeFragment() }

    //引导弹窗
    private val mUpdateInfoFragment: UpdateInfoFragment by lazy { UpdateInfoFragment() }

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
            } else {
                //访客
                val bundle = Bundle()
                bundle.putString("type", "SeenMe")
                RNPageActivity.start(requireActivity(), RnConstant.VISIT_HISTORY_PAGE, bundle)
                //清空新增的访客数量
                infoTabAdapter.data?.forEachIndexed { index, userDataTab ->
                    if (userDataTab.userDataTabType == ContactsTabType.Visit) {
                        userDataTab.addCount = 0
                        infoTabAdapter.notifyItemChanged(index)
                        judgeRedPoint()
                        return@forEachIndexed
                    }
                }
            }
        }

        rvUserTools.layoutManager = GridLayoutManager(context, 4)
        rvUserTools.adapter = toolsAdapter
        tvQueBi.setTFDinAltB()
        tvLingQian.setTFDinAltB()
        MixedHelper.setSwipeRefreshStyle(refreshView)
        initViewModel()
    }

    private fun initViewModel() {
        mViewModel.userInfo.observe(this, Observer {
            refreshView.isRefreshing = false
            if (it.isSuccess()) {
                loadData(it.requireT())
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
//                        RNPageActivity.start(requireActivity(), RnConstant.EDIT_MINE_HOMEPAGE)
                        requireActivity().startActivity<EditInfoActivity>()
                    }
                    ErrorCodes.NOT_BIND_WECHAT -> {
                        //账号与安全
                        val intent =
                            Intent(requireActivity(), AccountAndSecurityActivity::class.java)
                        if (ForceUtils.activityMatch(intent)) {
                            startActivity(intent)
                        }
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

        mViewModel.codeShowStatus.observe(this, Observer {
            if (it == true) {
                iv_invite.show()
            } else {
                iv_invite.hide()
            }
        })

        mInviteViewModel.codeSuccessFlag.observe(this, Observer {
            if (it == true) {
                //邀请码校验成功
                mInviteCodeFragment.dismiss()
                mViewModel.clearCodeTimer()
            }
        })

    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            BalanceUtils.queryLatestBalance()
        }
    }

    var showed = false
    private fun loadData(info: UserDetailInfo) {
        //显示引导弹窗
        if (!showed) {
            mUpdateInfoFragment.show(childFragmentManager, "UpdateInfoFragment")
            showed = true
        }
        judgeRedPoint()
        loadGuide(info.perfectGuide, info.userBasic.perfection)
        SharedPreferencesUtils.commitString(SPParamKey.CUSTOMER_URL, info.customerUrl)
        headImage.loadImage(info.userBasic.headPic + BusiConstant.OSS_160, 60f, 60f)
        tvNickName.text = info.userBasic.nickname
        tvUserId.text = "欢鹊ID: ${info.userBasic.userId}"
        tvQueBi.text = "${info.userBasic.beans}"
        tvLingQian.text = info.userBasic.cash

//        if (info.userBasic.userLevel > 0) {
//        sdv_wealth.show()
//        tv_wealth_privilege.hide()
        val wealthAddrss = GlobalUtils.getString(R.string.wealth_address)
        sdv_wealth.loadImage(String.format(wealthAddrss, info.userBasic.userLevel), 55f, 16f)
//        } else {
//            sdv_wealth.hide()
//            tv_wealth_privilege.show()
//        }

        if (info.userBasic.royalLevel > 0) {
            tv_royal_privilege.hide()
            sdv_royal_level.show()
            sdv_royal_level.loadImage(info.userBasic.royalPic, 55f, 16f)
        } else {
            tv_royal_privilege.show()
            sdv_royal_level.hide()
        }

        if (info.userBasic.anchorLevel > 0) {
            sdv_author_level.show()
            tv_author_privilege.hide()
            val wealthAddrss = GlobalUtils.getString(R.string.anchor_address)
//            sdv_author_level.loadImage(String.format(wealthAddrss, info.userBasic.anchorLevel), 55f, 16f)
            ImageUtils.loadImageWithHeight_2(sdv_author_level, String.format(wealthAddrss, info.userBasic.anchorLevel), dp2px(16))

        } else {
            tv_author_privilege.show()
            sdv_author_level.hide()
        }
        //更新用户信息
        SessionUtils.setUserType(info.userBasic.userType)

        if (info.userBasic.headRealPeople) {
            tvCertification.hide()
            sdv_real.loadImage(info.userBasic.authMark, 53f, 16f)
            sdv_real.show()
        } else {
            tvCertification.show()
            sdv_real.hide()
        }
//        sd_wealth.loadImage(info.userBasic.royalLevel)
//        if (info.userBasic.sex == Sex.FEMALE) {
//            ivInviteFriend.hide()
//        } else {
//
//            ivInviteFriend.show()
//        }
        when (info.userBasic.sex) {//Male、Female、Unknow

            Sex.FEMALE -> {
                val drawable = ContextCompat.getDrawable(
                    requireContext(),
                    com.julun.huanque.core.R.mipmap.icon_sex_female_white
                )
                if (drawable != null) {
                    drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
                    tvSex.setCompoundDrawables(drawable, null, null, null)
                }
                tvSex.backgroundResource =
                    com.julun.huanque.core.R.drawable.bg_shape_mine_sex_female
                tvSex.text = "${info.userBasic.age}"
            }
            else -> {
                val drawable = ContextCompat.getDrawable(
                    requireContext(),
                    com.julun.huanque.core.R.mipmap.icon_sex_male_white
                )
                if (drawable != null) {
                    drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
                    tvSex.setCompoundDrawables(drawable, null, null, null)
                }
                tvSex.backgroundResource = com.julun.huanque.core.R.drawable.bg_shape_mine_sex_male
                tvSex.text = "${info.userBasic.age}"
            }
        }

        loadAd(info.adList)

        infoTabAdapter.setNewInstance(info.userDataTabList)
        toolsAdapter.setNewInstance(info.tools)
    }

    private fun loadGuide(perfectGuide: PerfectGuideBean?, perfection: Int) {
        if (perfectGuide != null) {
            if (perfection == 100) {
                rl_guide.hide()
            } else {
                rl_guide.show()
                tv_guide_title.text=perfectGuide.guideText
                tv_guide_title2.text = "资料完整度：${perfection}%"
            }
        } else {
            rl_guide.hide()
        }
    }

    private fun loadAd(adList: MutableList<AdInfoBean>?) {
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
            bannerAD?.setDelegate(bannerItemClick)
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

    private var commonDialogFragment: CommonDialogFragment? = null

    override fun initEvents(rootView: View) {
        clHeadRoot.onClickNew {
//            RNPageActivity.start(requireActivity(), RnConstant.MINE_HOMEPAGE)
            HomePageActivity.newInstance(requireActivity(), SessionUtils.getUserId())
        }

        headImage.onClickNew {

            if (mViewModel.userInfo.value?.getT()?.userBasic?.headRealPeople != true) {
                //未处于头像认证状态
//                MyAlertDialog(requireActivity()).showAlertWithOKAndCancel(
//                    "通过人脸识别技术确认照片为真人将获得认证标识，提高交友机会哦~",
//                    MyAlertDialog.MyDialogCallback(onRight = {
//                        (ARouter.getInstance().build(ARouterConstant.REALNAME_SERVICE)
//                            .navigation() as? IRealNameService)?.checkRealHead { e ->
//                            if (e is ResponseError && e.busiCode == ErrorCodes.REAL_HEAD_ERROR) {
//                                MyAlertDialog(requireActivity(), false).showAlertWithOKAndCancel(
//                                    e.busiMessage.toString(),
//                                    title = "修改提示",
//                                    okText = "修改头像",
//                                    noText = "取消",
//                                    callback = MyAlertDialog.MyDialogCallback(onRight = {
//                                        RNPageActivity.start(requireActivity(), RnConstant.EDIT_MINE_HOMEPAGE)
//                                    })
//                                )
//
//                            }
//                        }
//                    }), "真人照片未认证", okText = "去认证", noText = "取消"
//                )
                CommonDialogFragment.create(
                    dialog = commonDialogFragment,
                    title = "真人照片认证",
                    content = "通过人脸识别技术确认照片为真人将获得认证标识，提高交友机会哦~",
                    imageRes = com.julun.huanque.core.R.mipmap.bg_dialog_real_auth,
                    okText = "去认证",
                    cancelText = "取消",
                    callback = CommonDialogFragment.Callback(
                        onOk = {
                            (ARouter.getInstance().build(ARouterConstant.REALNAME_SERVICE)
                                .navigation() as? IRealNameService)?.checkRealHead { e ->
                                if (e is ResponseError && e.busiCode == ErrorCodes.REAL_HEAD_ERROR) {
                                    MyAlertDialog(requireActivity(), false).showAlertWithOKAndCancel(
                                        e.busiMessage.toString(),
                                        title = "修改提示",
                                        okText = "修改头像",
                                        noText = "取消",
                                        callback = MyAlertDialog.MyDialogCallback(onRight = {
                                            val intent = Intent(requireActivity(), EditInfoActivity::class.java)
                                            if (ForceUtils.activityMatch(intent)) {
                                                startActivity(intent)
                                            }
                                        })
                                    )
//                                    CommonDialogFragment.create(
//                                        title = "修改提示",
//                                        content = e.busiMessage.toString(),
//                                        imageRes = com.julun.huanque.core.R.mipmap.bg_dialog_heart_full,
//                                        okText = "修改头像",
//                                        cancelText = "取消",
//                                        callback = CommonDialogFragment.MyDialogCallback(
//                                            onOk = {
//                                                val intent = Intent(requireActivity(), EditInfoActivity::class.java)
//                                                if (ForceUtils.activityMatch(intent)) {
//                                                    startActivity(intent)
//                                                }
//                                            }
//                                        )
//                                    ).show(requireActivity(), "CommonDialogFragment")

                                }
                            }

                        }
                    )
                ).show(requireActivity(), "CommonDialogFragment")
            }
//            ARouter.getInstance().build(ARouterConstant.REAL_HEAD_ACTIVITY).navigation()

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
                    RNPageActivity.start(requireActivity(), RnConstant.MY_CAR_PAGE)
                }
                MineToolType.ChatBubble -> {
                    RNPageActivity.start(requireActivity(), RnConstant.CHAT_BUBBLE_PAGE)
                }
                MineToolType.VisitHistory -> {
                    val bundle = Bundle()
                    bundle.putString("type", "HaveSeen")
                    RNPageActivity.start(requireActivity(), RnConstant.VISIT_HISTORY_PAGE, bundle)
                }
                MineToolType.InviteFriend -> {
                    RNPageActivity.start(requireActivity(), RnConstant.INVITE_FRIENDS_PAGE)
                }
                MineToolType.ToMaster -> {//聊主学院
                    mNeedRefresh = true
                    RNPageActivity.start(requireActivity(), RnConstant.CHAT_COLLEGE_PAGE)
                }
                MineToolType.ToAnchor -> {
                    mViewModel.checkToAnchor()
                }
            }
        }
        ivSetting.onClickNew {
            val anchorLevel = mViewModel.userInfo.value?.getT()?.userBasic?.anchorLevel ?: 0
            val act = requireActivity()
            SettingActivity.newInstance(act, anchorLevel)
        }
        rlQueBi.onClickNew {
            requireActivity().startActivity<RechargeCenterActivity>()
        }
        rlLingQian.onClickNew {
//            requireActivity().startActivity<WithdrawActivity>()
            RNPageActivity.start(requireActivity(), RnConstant.SmallChangePage)
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
            extra.putString(
                BusiConstant.WEB_URL,
                mViewModel.userInfo.value?.getT()?.customerUrl ?: ""
            )
            var intent = Intent(requireActivity(), WebActivity::class.java)
            intent.putExtras(extra)
            startActivity(intent)
        }

        iv_invite.onClickNew {
            //邀请码弹窗
            mInviteCodeFragment.show(childFragmentManager, "InviteCodeFragment")
        }

        rl_guide.onClickNew {
            val intent = Intent(requireActivity(), EditInfoActivity::class.java)
            if (ForceUtils.activityMatch(intent)) {
                startActivity(intent)
            }
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

    override fun onResume() {
        super.onResume()
        if (mNeedRefresh) {
            mNeedRefresh = false
            mViewModel.queryInfo(QueryType.REFRESH)
        }
    }

    override fun lazyLoadData() {
        mViewModel.queryInfo()
    }

    /**
     * 判断是否需要显示我的红点
     */
    private fun judgeRedPoint() {
        //聊主学院数量
        var messageCount = 0
        mViewModel.userInfo.value?.getT()?.tools?.forEach {
            if (it.toolType == MineToolType.ToMaster) {
                messageCount = it.messageCount
                return@forEach
            }
        }
        //访客数量
        var visitorCount = 0
        mViewModel.userInfo.value?.getT()?.userDataTabList?.forEach {
            if (it.userDataTabType == ContactsTabType.Visit) {
                visitorCount = it.addCount
                return@forEach
            }
        }

        mMainViewModel.showMineRedPoint.value = !(visitorCount <= 0 && messageCount <= 0)

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
        logger.info("收到支付结果：${result.payResult} type=${result.payType}")
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
                val count = item.count
                tvCount.text = "${StringHelper.formatNum(count)}"
                holder.setText(R.id.tvTitle, item.userTabName)

                if (item.addCount == 0) {
                    holder.setGone(R.id.tv_tag, true)
                } else {
                    holder.setGone(R.id.tv_tag, false).setText(R.id.tv_tag, StringHelper.formatNum(item.addCount.toLong()))
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
                val messageCount = item.messageCount
                if (messageCount > 0) {
                    holder.setVisible(R.id.tv_count, true)
                        .setText(R.id.tv_count, "${item.messageCount}")
                } else {
                    holder.setGone(R.id.tv_count, true)
                }

            }
        }
    }


    private val bannerAdapter by lazy {
        BGABanner.Adapter<SimpleDraweeView, AdInfoBean> { _, itemView, model, _ ->
            val hierarchy = GenericDraweeHierarchyBuilder.newInstance(resources)
                .setRoundingParams(RoundingParams.fromCornersRadius(dp2pxf(10)))
                .build()
            itemView.hierarchy = hierarchy
            when (model?.resType) {
                BannerResType.Pic -> {
                    val screenWidth = ScreenUtils.screenWidthFloat.toInt() - dp2px(15f) * 2
                    val height = dp2px(72f)
                    ImageUtils.loadImageInPx(itemView, model.resUrl, screenWidth, height)
                }
            }

        }
    }

    private val bannerItemClick by lazy {
        BGABanner.Delegate<SimpleDraweeView, AdInfoBean> { _, _, model, _ ->
            when (model?.touchType) {
                BannerTouchType.Url -> {
                    val extra = Bundle()
                    extra.putString(BusiConstant.WEB_URL, model.touchValue)
                    extra.putBoolean(IntentParamKey.EXTRA_FLAG_GO_HOME.name, false)
                    jump(WebActivity::class.java, extra = extra)
                }
                BannerTouchType.Recharge -> {
                    //充值页面
                    requireActivity().startActivity<RechargeCenterActivity>()
                }
                BannerTouchType.InviteFriend -> {
                    //邀请好友
                    RNPageActivity.start(requireActivity(), RnConstant.INVITE_FRIENDS_PAGE)
                }
                else -> {
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun informationChange(event: UserInfoEditEvent) {
        mViewModel.queryInfo(QueryType.REFRESH)
    }

}