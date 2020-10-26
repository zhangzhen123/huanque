package com.julun.huanque.core.ui.recharge

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.SpannedString
import android.text.TextUtils
import android.text.style.AbsoluteSizeSpan
import android.view.View
import android.widget.EditText
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder
import com.facebook.drawee.generic.RoundingParams
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.base.BaseVMFragment
import com.julun.huanque.common.basic.NetState
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.bean.beans.RechargeAdInfo
import com.julun.huanque.common.bean.beans.RechargeRespDto
import com.julun.huanque.common.bean.beans.RechargeTpl
import com.julun.huanque.common.bean.forms.PayForm
import com.julun.huanque.common.constant.*
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.ui.web.WebActivity
import com.julun.huanque.common.utils.*
import com.julun.huanque.common.widgets.bgabanner.BGABanner
import com.julun.huanque.common.widgets.recycler.decoration.GridLayoutSpaceItemDecoration2
import com.julun.huanque.core.R
import kotlinx.android.synthetic.main.fragment_recharge.*
import kotlinx.android.synthetic.main.view_recharge_beans.*
import org.jetbrains.anko.sdk23.listeners.textChangedListener

/**
 * 充值页
 * Created by WanZhiYuan on 2018/07/26 0026.
 * @iterativeAuthor WanZhiYuan
 * @iterativeDate 2019/11/94
 * @iterativeVersion 4.19.1
 * @iterativeDetail 我的账户增加广告轮播图
 */
class RechargeCenterFragment : BaseVMFragment<RechargeFragmentViewModel>() {

    private val mActivityViewModel: RechargeCenterViewModel by activityViewModels()
    private val mAdapter: RechargeAdapter by lazy { RechargeAdapter() }

    // 用户充值选择的面额对象
    private var mSelectItem: RechargeTpl? = null

    //当前页面position
    private var mTabPosition: Int = -1

    //需要充值的id
    private var mUserIdForRecharge: Long = -1

    //编辑框是否存在焦点
    private var mHasEditFocus: Boolean = true

    //确认账户后才能做其他操作
    private var mIsCheckUser: Boolean = false
    private var mIsFirst: Boolean = true
//    //选中优惠券信息
//    private var mCouponInfo: CouponItemInfo? = null
//    //优惠券总数
//    private var mTotalNum: Int = 0

    companion object {
        val POSITION = "recharge_position"
        fun newInstance(position: Int): RechargeCenterFragment {
            val fragment = RechargeCenterFragment()
            val bundle = Bundle()
            bundle.putInt(POSITION, position)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun getLayoutId(): Int = R.layout.fragment_recharge

    override fun lazyLoadData() {
        queryAppChannelRule(QueryType.INIT)
    }

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        mTabPosition = arguments?.getInt(POSITION, -1) as Int
        if (mTabPosition == 0) {
        }
        initViewModel()


        gridView.layoutManager = GridLayoutManager(context, 3)
        gridView.adapter = mAdapter
        gridView.addItemDecoration(GridLayoutSpaceItemDecoration2(dp2px(15f)))

        //设置hint的字体大小
        val ss = SpannableString(resources.getString(R.string.other_recharge_hint))
        val ass = AbsoluteSizeSpan(18, true)
        ss.setSpan(ass, 0, ss.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        other_userId.hint = SpannedString(ss)


        when (mTabPosition) {
            0 -> {
                my_count_layout.visibility = View.VISIBLE
                other_count_layout.visibility = View.GONE
            }
            else -> {
                my_count_layout.visibility = View.GONE
                other_count_layout.visibility = View.VISIBLE

            }
        }

        account_balance.setTFDinAltB()

    }

    private var currentPayType: String = ""
    override fun initEvents(rootView: View) {

        //item click
        mAdapter.setOnItemClickListener { _, _, position ->
            checkItem(position, true)
        }

        //Wechat
        wxpay_ctr.onClick {
            checkPayType(PayType.WXPayApp)
        }

        //alipay
        alipay_ctr.onClick {
            checkPayType(PayType.AliPayApp)
        }

        //oppo支付
        oppopay_ctr.onClickNew {
            checkPayType(PayType.OppoPay)
        }
        btn_ensure.onClickNew {
            saveAppPay(currentPayType)
        }

        other_userId.textChangedListener {
            afterTextChanged {
                if (it != null) {
                    when (it.isEmpty()) {
                        true -> others_btn.isEnabled = false
                        else -> others_btn.isEnabled = true
                    }
                    other_count_name.hide()
                    mUserIdForRecharge = -1
                    mAdapter.setSelection(-1)
                    mIsCheckUser = false
                }
            }
        }

        other_userId.onClickNew {
            mHasEditFocus = true
            other_userId.isCursorVisible = true
        }

        //帮人充值确定按钮
        others_btn.onClickNew {
            searchUserById(other_userId)
            when (mHasEditFocus) {
                true -> {
                    mHasEditFocus = false
                    other_userId.isCursorVisible = false
                }
            }
        }

        btn_ensure.post {
            checkPayType(PayType.WXPayApp)
        }
    }

    private fun checkPayType(payType: String) {
        currentPayType = payType
        when (currentPayType) {
            PayType.WXPayApp -> {
                wxpay_ctr.isSelected = true
                alipay_ctr.isSelected = false
                oppopay_ctr.isSelected = false
            }
            PayType.AliPayApp -> {
                wxpay_ctr.isSelected = false
                alipay_ctr.isSelected = true
                oppopay_ctr.isSelected = false
            }
            PayType.OppoPay -> {
                wxpay_ctr.isSelected = false
                alipay_ctr.isSelected = false
                oppopay_ctr.isSelected = true
            }
        }
        checkEnsureBtn()
    }

    private fun initViewModel() {
        mViewModel.mRechargeRespDto.observe(this, Observer {
            if (it.isSuccess()) {
                val data = it.requireT()
                refreshData(data)
                loadAd(data.adList)
            }
        })

        mViewModel.balance.observe(this, Observer {
            it?.let {
                account_balance.text = "$it"
            }
        })
        /**
         * 充值成功,刷新充值成功的金额列表
         */
        mActivityViewModel.isRefresh.observe(this, Observer {
            if (it != null) {

                val condition = it == mTabPosition
                if (condition) {
                    mAdapter.setSelection(-1)
                    queryAppChannelRule(QueryType.REFRESH)

                }
            }
        })

    }

    private fun loadAd(adList: MutableList<RechargeAdInfo>?) {
        if (adList != null) {
            if (mTabPosition != 0) {
                if (banner.isVisible()) {
                    banner.hide()
                }
                return
            }
            if (adList.isEmpty()) {
                if (banner.isVisible()) {
                    banner?.setAutoPlayAble(false)
                    banner?.setAllowUserScrollable(false)
                    banner?.setData(adList, null)
                    banner.hide()
                }
                return
            }
            banner.show()
            banner?.setAdapter(bannerAdapter)
            banner?.setDelegate(bannerItemCick)
            banner?.setData(adList, null)
            banner?.setAutoPlayAble(adList.size > 1)
            banner?.viewPager?.pageMargin = dp2px(10)
            if (adList.size > 1) {
                banner?.currentItem = 0
            }
        } else {
            banner?.hide()
        }

    }

    /**
     * 获取充值规则信息
     */
    private fun queryAppChannelRule(queryType: QueryType) {
        if (mTabPosition != 0 && mUserIdForRecharge > 0) {
            mViewModel.curTargetUserId = mUserIdForRecharge
        }

        mViewModel.queryInfo(queryType)
    }

    /**
     * 根据输入的id查询用户信息
     */
    private fun searchUserById(editText: EditText) {
        val searchKeyWord: Long
        mAdapter.setSelection(-1)
        ScreenUtils.hideSoftInput(other_userId)
        other_count_name.hide()
        try {
            searchKeyWord = "${editText.text}".toLong()
            if (searchKeyWord > 0) {
                mUserIdForRecharge = searchKeyWord
                queryAppChannelRule(QueryType.REFRESH)
            } else {
                ToastUtils.show("请输入正确的欢鹊ID")
            }
        } catch (e: Exception) {
            editText.setText("")
            ToastUtils.show("请输入正确的欢鹊ID")
        }
    }

    /**
     * 刷新数据
     */
    private fun refreshData(obj: RechargeRespDto) {
        mSelectItem = null
        when (mTabPosition) {
            0 -> {
                account_balance.text = obj.beans.toString()
                rechargeBeans.text = "充值鹊币: ${obj.rechargeBeans}"
                platformBeans.text = "赠送鹊币: ${obj.platformBeans}"
            }
            else -> {
                other_userId.tag = obj.userId
                if (mUserIdForRecharge > 0) {
                    other_count_name.show()
                    val string = StringBuffer()
                    string.append("当前充值账户(")
                    string.append(obj.nickname)
                    string.append(")")
                    other_count_name.text = string.toString()
                }
                if (other_userId != null && !TextUtils.isEmpty(other_userId.text.toString())) {
                    mIsCheckUser = true
                }
            }
        }

        if (mIsFirst) {
            if (mTabPosition != 0) {
                openKeyBoard(other_userId)
            }
            mIsFirst = false
        }

        // 加载支付面额
        obj.tplList.let {
            mAdapter.setNewInstance(it)
        }
        mUserIdForRecharge = obj.userId

        mActivityViewModel.getHelpUrl.value = "${obj.customerUrl}"

//        var payTypes = ""
//        if (!TextUtils.isEmpty(obj.payTypes)) {
//            payTypes = obj.payTypes
//        }

//        if (mActivityViewModel.mOppoService != null) {
//            //oppo联运模式
//            tv_oppo.show()
//        } else {
//            // 是否有微信支付权限
//            if (payTypes.contains("WXPayApp")) {
//                wxpay_ctr.visibility = View.VISIBLE
//            } else {
//                wxpay_ctr.visibility = View.GONE
//            }
//            // 是否有支付宝支付权限
//            if (payTypes.contains("AlipayApp")) {
//                alipay_ctr.visibility = View.VISIBLE
//            } else {
//                alipay_ctr.visibility = View.GONE
//            }
//        }

    }

    // 后台创建订单
    private fun saveAppPay(payType: String) {
        // 网络有问题
        if (!NetUtils.isNetConnected()) {
            ToastUtils.show(R.string.net_is_down)
            return
        }
        // 是否登录
        if (TextUtils.isEmpty(SessionUtils.getSessionId())) {
            ToastUtils.show(R.string.not_login)
            return
        }
        if (mUserIdForRecharge <= 0) {
            ToastUtils.show("你需要先确认账户")
            return
        }
        // 未选择充值面额
        if (mSelectItem == null) {
            ToastUtils.show(R.string.plz_choose_recharge_count)
            return
        }
        if (mTabPosition == 0) {
            mActivityViewModel.payPosition.value = mTabPosition
        }

        val form = PayForm()
        form.payType = payType
        mSelectItem?.let {
            form.tplId = it.tplId
        }
        form.targetUserId = mUserIdForRecharge
        form.rechargeEntry = RechargeEntry.My

        mActivityViewModel.queryNewCreateAppPay(form)

    }

    private fun closeKeyBoard() {
        when (ScreenUtils.isSoftInputShow(requireActivity())) {
            true -> {
                ScreenUtils.hideSoftInput(requireActivity())
            }
        }
    }

    private fun openKeyBoard(view: EditText) {
        if (view != null) {
            view.isFocusable = true
            view.isFocusableInTouchMode = true
            view.isCursorVisible = true
            view.requestFocus()
            ScreenUtils.showSoftInput(requireContext(), view)
        }
    }

    //todo 这里过时了
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        mHasEditFocus = true
        if (!isVisibleToUser) {
            return
        }
        when (mTabPosition) {
            0 -> {
                closeKeyBoard()
            }
            else -> {
                if (!mIsFirst && other_userId != null) {
                    if (mIsCheckUser) {//是否确认
                        closeKeyBoard()
                        other_userId.isCursorVisible = false
                    } else {
                        openKeyBoard(other_userId)
                    }
                }
            }
        }
    }


    private fun checkItem(position: Int, isClick: Boolean) {
        //避免用户先选择面额再确认账户
        if (mTabPosition != 0) {
            if (other_userId.text.toString().isEmpty()) {
                ToastUtils.show("请先输入要充值的账户")
                return
            }
            if (!mIsCheckUser) {
                ToastUtils.show("请先确认要充值的账户")
                return
            }
        }

        val itemInfo = mAdapter.getItem(position)
        //做一个点击获取的充值信息是否相同的标记
        var isSame = mSelectItem?.tplId == itemInfo?.tplId
        mSelectItem = itemInfo

        mAdapter.setSelection(position)

        checkEnsureBtn()
    }

    private fun checkEnsureBtn() {
//        btn_ensure.isEnabled = mSelectItem != null && currentPayType.isNotEmpty()
    }

    override fun showLoadState(state: NetState) {
        when (state.state) {
            NetStateType.LOADING -> {
                state_pager_view.showLoading()
                sv_recharge_root.hide()
                btn_ensure.hide()
            }
            NetStateType.SUCCESS -> {
                state_pager_view.showSuccess()
                sv_recharge_root.show()
                btn_ensure.show()
            }
            NetStateType.ERROR, NetStateType.NETWORK_ERROR -> {
                sv_recharge_root.hide()
                btn_ensure.hide()
                state_pager_view.showError(btnClick = View.OnClickListener {
                    mViewModel.queryInfo(QueryType.INIT)
                })
            }

        }

    }

    private val bannerAdapter by lazy {
        BGABanner.Adapter<SimpleDraweeView, RechargeAdInfo> { _, itemView, model, _ ->
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