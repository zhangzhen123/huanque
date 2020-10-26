package com.julun.huanque.core.ui.live.fragment

import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.KeyEvent
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.bean.beans.FirstRechargeInfo
import com.julun.huanque.common.bean.beans.GiftIcon
import com.julun.huanque.common.bean.beans.PayResultInfo
import com.julun.huanque.common.bean.beans.SinglePack
import com.julun.huanque.common.bean.events.PayResultEvent
import com.julun.huanque.common.bean.forms.PayForm
import com.julun.huanque.common.constant.*
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.common.interfaces.routerservice.LoginAndShareService
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.inVisible
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.ui.web.WebActivity
import com.julun.huanque.common.utils.*
import com.julun.huanque.core.R
import com.julun.huanque.core.manager.AliPayManager
import com.julun.huanque.core.ui.live.adapter.FirstRechargeGiftAdapter
import com.julun.huanque.common.viewmodel.FirstRechargeViewModel
import kotlinx.android.synthetic.main.fragment_first_recharge.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 *@创建者   dong
 *@创建时间 2020/10/22 10:14
 *@描述 首充弹窗
 */
@Route(path = ARouterConstant.FIRST_RECHARGE_FRAGMENT)
class FirstRechargeFragment : BaseDialogFragment() {

    companion object {
        fun newInstance(bean: FirstRechargeInfo): FirstRechargeFragment {
            val fragment = FirstRechargeFragment()
            fragment.arguments = Bundle().apply { putSerializable(ParamConstant.First_Recharge_Info, bean) }
            return fragment
        }
    }

    private val mFirstRechargeViewModel: FirstRechargeViewModel by activityViewModels()

    //选择支付方式弹窗
    private var mChoosePayTypeFragment: ChooseRechargeTypeFragment? = null

    private var mFirstRechargeInfo: FirstRechargeInfo? = null

    private var mGridLayoutManager: GridLayoutManager? = null

    private val giftAdapter = FirstRechargeGiftAdapter()

    private val wxService: LoginAndShareService? by lazy {
        ARouter.getInstance().build(ARouterConstant.LOGIN_SHARE_SERVICE).navigation() as? LoginAndShareService
    }

    //当前选中的礼包
    private var mCurrentPack: SinglePack? = null

    override fun getLayoutId() = R.layout.fragment_first_recharge

    override fun isRegisterEventBus() = true

    override fun initViews() {
        //当天已经显示
        SPUtils.commitBoolean(GlobalUtils.getFirstRechargeKey(), true)
        mFirstRechargeViewModel.startCountDown()
        initRecyclerView()
        initEvents()
    }

    /**
     * 初始化监听
     */
    private fun initEvents() {
        tv_left.onClickNew {
            //选中左侧
            showTitle(0)
        }
        tv_middle.onClickNew {
            //选中中间
            showTitle(1)
        }
        tv_right.onClickNew {
            //险种右侧
            showTitle(2)
        }

        ivClose.onClickNew {
            dismiss()
        }

        iv_helper.onClickNew {
            //帮忙按钮
        }
        iv_recharge.onClickNew {
            //去支付
            mChoosePayTypeFragment = mChoosePayTypeFragment ?: ChooseRechargeTypeFragment()
            mChoosePayTypeFragment?.show(childFragmentManager, "ChooseRechargeTypeFragment")
        }
        iv_helper.onClickNew {
            WebActivity.startWeb(requireActivity(), mFirstRechargeInfo?.explain ?: "", "首充送豪礼")
        }

    }

    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {
        mFirstRechargeViewModel.firstRechargeBean.observe(this, Observer {
            showViewByData(it)
        })
        mFirstRechargeViewModel.showTime.observe(this, Observer {
            if (it != null) {
                tv_time.text = it
            }
        })
        mFirstRechargeViewModel.payTypeFlag.observe(this, Observer {
            if (it != null) {
                saveAppPay(it)
                mFirstRechargeViewModel.payTypeFlag.value = null
            }
        })

        /**
         * 订单回调
         */
        mFirstRechargeViewModel.payValue.observe(this, Observer {
            if (it != null) {
                callPay(it)
                mFirstRechargeViewModel.payValue.value = null
            }

        })
    }


    private fun showViewByData(beann: FirstRechargeInfo?) {
        mFirstRechargeInfo = beann
        if (mFirstRechargeInfo == null || (mFirstRechargeInfo?.packetList?.size ?: 0) != 3) {
            dismiss()
            return
        }
        //显示默认视图
        var selectIndex = 0
        val packList = mFirstRechargeInfo?.packetList ?: return
        packList.forEachIndexed { index, it ->
            if (it.selected) {
                //默认选中
                selectIndex = index
            }
            when (index) {
                0 -> {
                    tv_left.text = "${it.money}元"
                    tv_left_sel.text = "${it.money}元"
                }
                1 -> {
                    tv_middle.text = "${it.money}元"
                    tv_middle_sel.text = "${it.money}元"
                }
                2 -> {
                    tv_right.text = "${it.money}元"
                    tv_right_sel.text = "${it.money}元"
                }
                else -> {
                }
            }
        }
        showTitle(selectIndex)
    }


    /**
     * 初始化ViewModel
     */
    private fun initRecyclerView() {
        mGridLayoutManager = GridLayoutManager(context, 3)
        rvNewUserGift.layoutManager = mGridLayoutManager
        rvNewUserGift.adapter = giftAdapter
    }

    /**
     * 显示标题
     */
    private fun showTitle(index: Int) {
        when (index) {
            0 -> {
                tv_left.inVisible()
                tv_left_sel.show()
                tv_middle.show()
                tv_middle_sel.hide()
                tv_right.show()
                tv_right_sel.hide()
            }
            1 -> {
                tv_left.show()
                tv_left_sel.hide()
                tv_middle.inVisible()
                tv_middle_sel.show()
                tv_right.show()
                tv_right_sel.hide()
            }
            2 -> {
                tv_left.show()
                tv_left_sel.hide()
                tv_middle.show()
                tv_middle_sel.hide()
                tv_right.inVisible()
                tv_right_sel.show()
            }
            else -> {
            }
        }

        val packList = mFirstRechargeInfo?.packetList ?: return
        if (ForceUtils.isIndexNotOutOfBounds(index, packList)) {
            val pack = packList[index]
            mCurrentPack = pack
            tv_recharge.text = "立即充值${pack.money}元"
            //设置副标题
            val subTitle = pack.subtitle
            val style = SpannableStringBuilder(subTitle)
            val firstColorContent = "${pack.money}元"
            val secondColorContent = "${pack.valueMoney}元"
            val color = GlobalUtils.formatColor("#FFF703")
            val firstStartIndex = subTitle.indexOf(firstColorContent)

            if (firstStartIndex > 0) {
                //匹配成功
                style.setSpan(
                    ForegroundColorSpan(color),
                    firstStartIndex,
                    firstStartIndex + firstColorContent.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            val secondStartIndex = subTitle.indexOf(secondColorContent)
            if (secondStartIndex > 0) {
                style.setSpan(
                    ForegroundColorSpan(color),
                    secondStartIndex,
                    secondStartIndex + secondColorContent.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            tv_subhead.text = style

            showGiftView(pack.awardDetails)
        }
    }

    /**
     * 显示礼物图案
     */
    private fun showGiftView(list: MutableList<GiftIcon>) {
        if (list.isNotEmpty()) {
            //移除所有的itemDecoration
//            while (rvNewUserGift.itemDecorationCount > 0) {
//                rvNewUserGift.removeItemDecorationAt(0)
//            }
//            if (list.size > 3) {
//                rvNewUserGift.addItemDecoration(HorizontalItemDecoration2(DensityUtils.dp2px(5f)))
//            } else {
//                rvNewUserGift.addItemDecoration(HorizontalItemDecoration2(DensityUtils.dp2px(20f)))
//            }
            mGridLayoutManager?.spanCount = list.size
            giftAdapter.setList(list)
        }
    }


    override fun onStart() {
        super.onStart()
        initViewModel()
        setDialogSize(Gravity.CENTER, 0, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog?.setCancelable(false)
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.setOnKeyListener { _, keyCode, _ ->
            keyCode == KeyEvent.KEYCODE_BACK
        }
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
        val pack = mCurrentPack ?: return

        val form = PayForm()
        form.payType = payType
        form.rechargeEntry = RechargeEntry.My
        form.tplId = pack.tplId

        mFirstRechargeViewModel.queryNewCreateAppPay(form)

    }

    /*************************************支付回调相关处理*****************************************/
    private fun callPay(pay: PayResultInfo) {
        when (pay.payType) {
            PayType.WXPayApp -> {
                if (pay.contentString.isNotEmpty()) {
                    //调用H5充值
                    val extra = Bundle()
                    extra.putString(BusiConstant.WEB_URL, pay.contentString)
                    extra.putBoolean(IntentParamKey.EXTRA_FLAG_GO_HOME.name, false)
                    val intent = Intent(requireActivity(), WebActivity::class.java)
                    if (ForceUtils.activityMatch(intent)) {
                        intent.putExtras(extra)
                        requireActivity().startActivity(intent)
                    }

                } else {
                    //调用SDK支付
//                    WXApiManager.doPay(this, pay.wxOrderInfo)
                    wxService?.weiXinPay(requireActivity(), pay.wxOrderInfo)
                }
            }
            //优先使用mainActivity防止内存泄漏
            PayType.AliPayApp -> AliPayManager.payOrder(
                CommonInit.getInstance().getMainActivity() ?: requireActivity(), pay.alipayOrderInfo
            )
//            "OppoPay" -> {
//                //oppo支付
//                mViewModel.mOppoService?.oppoPay(
//                    this, pay.oppoOrderInfo ?: return, pay.orderNo
//                        ?: return
//                )
//            }
            else -> {
            }
        }
    }

    /**
     * 接收支付结果
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun receivePayResult(result: PayResultEvent) {
        logger.info("收到支付结果：${result.payResult} type=${result.payType}")
        when (result.payResult) {
            PayResult.PAY_SUCCESS -> {
                ToastUtils.show(getString(R.string.pay_success))
                mFirstRechargeViewModel.firstRechargeFlag.value = false
                dismiss()
            }
            PayResult.PAY_CANCEL -> {
                ToastUtils.show(R.string.pay_cancel)
            }
            PayResult.PAY_FAIL -> {
                ToastUtils.show(R.string.pay_fail)
            }
            PayResult.PAY_REPETITION -> {
                ToastUtils.show(R.string.pay_repetition)
            }
            else -> {

            }
        }

    }
}