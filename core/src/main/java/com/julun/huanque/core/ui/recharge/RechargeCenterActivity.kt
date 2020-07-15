package com.julun.huanque.core.ui.recharge

import android.content.Context
import android.os.Bundle
import android.text.Html
import android.text.TextUtils
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.base.dialog.LoadingDialog
import com.julun.huanque.common.base.dialog.MyAlertDialog
import com.julun.huanque.common.bean.beans.PayResultInfo
import com.julun.huanque.common.bean.events.PayResultEvent
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.constant.PayResult
import com.julun.huanque.common.constant.PayType
import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.common.interfaces.routerservice.WeiXinPayService
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.common.widgets.ColorFlipPagerTitleView
import com.julun.huanque.core.R
import com.julun.huanque.core.pay.AliPayManager
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import kotlinx.android.synthetic.main.activity_recharge.*
import net.lucode.hackware.magicindicator.ViewPagerHelper
import net.lucode.hackware.magicindicator.buildins.UIUtil
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.dip
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.textSizeDimen
import java.util.concurrent.TimeUnit


@Route(path = ARouterConstant.RECHARGE_ACTIVITY)
class RechargeCenterActivity : BaseActivity() {

    private val tabList: ArrayList<String> = arrayListOf("我的账户"/*, "帮TA充值"*/)

    private var mPagerAdapter: RechargePagerAdapter? = null
    private val mViewModel: RechargeCenterViewModel by viewModels()
    private lateinit var mCommonNavigator: CommonNavigator

    // 加载框
    private var mLoadingDialog: LoadingDialog? = null

    //tab index
    private var mCurrentTabIndex: Int = 0

    private var mHelpUrl: String = ""
    private var mPayPosition: Int = -1
    private var wxService: WeiXinPayService? =
        ARouter.getInstance().build(ARouterConstant.WEIXIN_PAY_SERVICE).navigation() as? WeiXinPayService

    //是否充值成功
    private var mIsPaySuccess: Boolean = false

    private val mHelperAlert: MyAlertDialog by lazy { MyAlertDialog(this, true) }

    override fun getLayoutId(): Int = R.layout.activity_recharge

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {

        headerPageView.initHeaderView(titleTxt = "充值", operateRes = R.mipmap.icon_help_black_01)
        initViewModel()
        mPagerAdapter = RechargePagerAdapter(supportFragmentManager, this)
        mPagerAdapter?.setTabList(tabList)
        recharge_pager.adapter = mPagerAdapter
        if (tabList.size == 1) {
            magic_indicator.hide()
        } else {
            initMagicIndicator(tabList)
        }


        val attention = "<font color='#999999'>充值遇到问题？点击联系</font><font color='#20A9F0'>在线客服</font>"
        recharge_problem.text = Html.fromHtml(attention)

    }

    override fun initEvents(rootView: View) {
        //帮助入口按钮
        headerPageView.imageOperation.imageResource = R.mipmap.icon_help_black_01
        headerPageView.imageOperation.onClickNew {
            mHelperAlert.showAlertWithOK(getString(R.string.recharge_help), null, "帮助说明", "知道了")
        }

        headerPageView.imageViewBack.onClickNew {
            onBackPressed()
        }
        recharge_pager.addOnPageChangeListener(object : androidx.viewpager.widget.ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                setTextViewBg(position)
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })
        //客服
        recharge_problem.onClickNew {
            if (TextUtils.isEmpty(mHelpUrl)) {
                ToastUtils.show("客服信息获取失败~！")
                return@onClickNew
            }
            //todo
//            ARouter.getInstance().build(ARouterConstant.WEB_ACTIVITY)
//                    .withString(BusiConstant.PUSH_URL, mViewModel?.customerUrl)
//                    .withBoolean(IntentParamKey.EXTRA_FLAG_DO_NOT_GO_HOME.name, true)
//                    .navigation()

        }

    }

    private fun initViewModel() {

        /**
         * 设置帮助url
         */
        mViewModel.getHelpUrl.observe(this, Observer {
            mHelpUrl = it ?: return@Observer
        })

        /**
         * 订单回调
         */
        mViewModel.payValue.observe(this, Observer { callPay(it ?: return@Observer) })

        /**
         * 展示支付Loading
         */
        mViewModel.isShowPayLoading.observe(this, Observer {
            isShowPayLoading(it ?: return@Observer)
        })

        /**
         * 获取充值的id
         */
        mViewModel.payPosition.observe(this, Observer {
            if (it != null && it != -1) {
                mPayPosition = it
            }
        })
    }

    /**
     * 初始化指示器
     */
    private fun initMagicIndicator(list: ArrayList<String>) {
        magic_indicator.show()
        mCommonNavigator = CommonNavigator(this)
        mCommonNavigator.scrollPivotX = 0.65f
        mCommonNavigator.isAdjustMode = true
        mCommonNavigator.adapter = object : CommonNavigatorAdapter() {
            override fun getCount(): Int {
                return list.size
            }

            override fun getTitleView(context: Context, index: Int): IPagerTitleView {
                val simplePagerTitleView = ColorFlipPagerTitleView(context)
                simplePagerTitleView.textSizeDimen = R.dimen.text_size_big
                simplePagerTitleView.text = list[index]
                simplePagerTitleView.normalColor = ContextCompat.getColor(context, R.color.black_666)
                simplePagerTitleView.selectedColor = ContextCompat.getColor(context, R.color.black_333)
                when (index) {
                    0 -> simplePagerTitleView.paint.isFakeBoldText = true
                }
                simplePagerTitleView.setOnClickListener {
                    if (index != mCurrentTabIndex) {
                        magic_indicator.onPageSelected(index)
                        mCurrentTabIndex = index
                        setTextViewBg(index)
                    }
                    recharge_pager.currentItem = index
                }
                return simplePagerTitleView
            }

            override fun getIndicator(context: Context): IPagerIndicator {
                val indicator = LinePagerIndicator(context)
                indicator.mode = LinePagerIndicator.MODE_EXACTLY
                indicator.lineHeight = UIUtil.dip2px(context, 3.0).toFloat()
                indicator.lineWidth = UIUtil.dip2px(context, 61.0).toFloat()
                indicator.roundRadius = UIUtil.dip2px(context, 3.5).toFloat()
                indicator.startInterpolator = AccelerateInterpolator()
                indicator.endInterpolator = DecelerateInterpolator(2.0f)
                indicator.yOffset = dip(4f).toFloat()
                indicator.setColors(ContextCompat.getColor(context, R.color.primary_color))
                return indicator
            }
        }
        magic_indicator.navigator = mCommonNavigator
        //绑定tab和viewpager
        ViewPagerHelper.bind(magic_indicator, recharge_pager)
    }

    /**
     * 支付时展示的loading
     */
    private fun isShowPayLoading(isShow: Boolean) {
        when (isShow) {
            true -> {
                // 显示加载框，直到得到后台计算的订单数据
                if (mLoadingDialog == null) {
                    mLoadingDialog = LoadingDialog(this@RechargeCenterActivity)
                    mLoadingDialog?.setCancelable(false)
                }
                if (mLoadingDialog?.isShowing == false) {
                    mLoadingDialog?.showDialog(R.string.query_order_info)
                }
            }
            else -> mLoadingDialog?.dismiss()
        }
    }

    private fun setTextViewBg(index: Int) {
        //获取两个texeview实例才能设置字体样式
        val currentTitleView = mCommonNavigator.getPagerTitleView(index) ?: return//Elvis运算符

        when (index) {
            0 -> {
                if (currentTitleView is ColorFlipPagerTitleView) {
                    currentTitleView.paint.isFakeBoldText = true
                }
                val beforeTitleView = mCommonNavigator.getPagerTitleView(1) ?: return
                if (beforeTitleView is ColorFlipPagerTitleView) {
                    beforeTitleView.paint.isFakeBoldText = false
                }
            }
            1 -> {
                if (currentTitleView is ColorFlipPagerTitleView) {
                    currentTitleView.paint.isFakeBoldText = true
                }
                val beforeTitleView = mCommonNavigator.getPagerTitleView(0) ?: return
                if (beforeTitleView is ColorFlipPagerTitleView) {
                    beforeTitleView.paint.isFakeBoldText = false
                }
            }
        }
    }

    /*************************************支付回调相关处理*****************************************/
    private fun callPay(pay: PayResultInfo) {
        when (pay.payType) {
            PayType.WXPayApp -> {
                if (pay.contentString.isNotEmpty()) {
                    //调用H5充值
//                    val extra = Bundle()
//                    extra.putString(BusiConstant.PUSH_URL, pay.contentString)
//                    extra.putBoolean(IntentParamKey.EXTRA_FLAG_DO_NOT_GO_HOME.name, true)
//                    jump(PushWebActivity::class.java, extra = extra)
                } else {
                    //调用SDK支付
//                    WXApiManager.doPay(this, pay.wxOrderInfo)
                    wxService?.weixinPay(this, pay.wxOrderInfo)
                }
            }
            //优先使用mainActivity防止内存泄漏
            PayType.AliPayApp -> AliPayManager.payOrder(
                CommonInit.getInstance().getMainActivity()
                    ?: this, pay.alipayOrderInfo
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
     * 注册EventBus事件
     */
    override fun isRegisterEventBus(): Boolean = true

    //处理页面回来刷新支付的
    private var refreshPay = false

    override fun onResume() {
        super.onResume()
        if (refreshPay) {
            Observable.timer(200, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    mViewModel.isRefresh.value = mPayPosition
                }
            refreshPay = false
        }
    }

    /**
     * 接收支付结果
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun receivePayResult(result: PayResultEvent) {
        logger.info("收到支付结果：${result.payResult} type=${result.payType}" )
        when (result.payResult) {
            PayResult.PAY_SUCCESS -> {
                mViewModel.isRefresh.value = mPayPosition
                paySuccess()
            }
            PayResult.PAY_CANCEL -> {
                payCancel()
            }
            PayResult.PAY_FAIL -> {
                payFail()
            }
            PayResult.PAY_REPETITION -> {
                ToastUtils.show(R.string.pay_repetition)
            }
            else -> {

            }
        }

    }

    /**
     * 支付成功
     */
    private fun paySuccess() {
        ToastUtils.show(getString(R.string.pay_success))
        mIsPaySuccess = true
    }

    /**
     * 支付取消
     */
    private fun payCancel() {
        ToastUtils.show(R.string.pay_cancel)
    }

    /**
     * 支付失败
     */
    private fun payFail() {
        ToastUtils.show(R.string.pay_fail)
    }
}