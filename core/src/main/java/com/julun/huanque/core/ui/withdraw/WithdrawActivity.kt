package com.julun.huanque.core.ui.withdraw

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.alibaba.android.arouter.launcher.ARouter
import com.julun.huanque.common.base.BaseVMActivity
import com.julun.huanque.common.base.dialog.MyAlertDialog
import com.julun.huanque.common.basic.NetState
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.bean.beans.WithdrawInfo
import com.julun.huanque.common.bean.beans.WithdrawTpl
import com.julun.huanque.common.bean.events.AliAuthCodeEvent
import com.julun.huanque.common.bean.events.WeiXinCodeEvent
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.constant.ErrorCodes
import com.julun.huanque.common.constant.WithdrawErrorCode
import com.julun.huanque.common.constant.WithdrawType
import com.julun.huanque.common.interfaces.routerservice.IRealNameService
import com.julun.huanque.common.interfaces.routerservice.WeiXinService
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.common.widgets.recycler.decoration.GridLayoutSpaceItemDecoration2
import com.julun.huanque.core.R
import com.julun.huanque.core.manager.AliPayManager
import com.julun.huanque.core.viewmodel.UserBindViewModel
import kotlinx.android.synthetic.main.activity_withdraw.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.startActivity

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/7/16 11:44
 *
 *@Description: 提现页面
 *
 */
class WithdrawActivity : BaseVMActivity<WithdrawViewModel>() {

    private val userBindViewModel: UserBindViewModel by viewModels()

    private val wxService: WeiXinService? by lazy {
        ARouter.getInstance().build(ARouterConstant.WEIXIN_SERVICE).navigation() as? WeiXinService
    }

    private val mAdapter: WithdrawAdapter by lazy { WithdrawAdapter() }

    // 用户充值选择的面额对象
    private var mSelectItem: WithdrawTpl? = null

    private val alertDialog by lazy { MyAlertDialog(this) }

    override fun getLayoutId(): Int = R.layout.activity_withdraw

    override fun isRegisterEventBus(): Boolean = true

    override fun setHeader() {
        pagerHeader.initHeaderView(titleTxt = "提现", operateTxt = "提现记录")
        pagerHeader.textOperation.textSize = 14f
        pagerHeader.textOperation.onClickNew {
            logger.info("打开提现记录")
            startActivity<WithdrawHistoryActivity>()
        }
    }

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {

        initViewModel()

        gridView.adapter = mAdapter
        gridView.layoutManager = GridLayoutManager(this, 3)
        gridView.addItemDecoration(GridLayoutSpaceItemDecoration2(dp2px(8f)))

        account_money.setTFDinAltB()

        mViewModel.queryInfo(queryType = QueryType.INIT)
    }

    private var currentWithdrawType: String = ""
    override fun initEvents(rootView: View) {

        pagerHeader.imageViewBack.onClickNew {
            finish()
        }
        //item click
        mAdapter.setOnItemClickListener { _, _, position ->
            checkItem(position)
        }

        //Wechat
        wxpay_ctr.onClick {
            checkPayType(WithdrawType.WXWithdraw)
        }

        //alipay
        alipay_ctr.onClick {
            checkPayType(WithdrawType.AliWithdraw)
        }

        btn_ensure.onClickNew {
            startCheckAndApply()
        }


    }

    private fun startCheckAndApply() {
        if (mSelectItem == null) {
            ToastUtils.show("请先选择提现金额")
            return
        }
        if (currentWithdrawType.isEmpty()) {
            ToastUtils.show("请先选择提现方式")
            return
        }
        val message = if (currentWithdrawType == WithdrawType.AliWithdraw) {
            "确定提现至你的支付宝账号（${aliNickname}）吗？"
        } else {
            "确定提现至你的微信账号（${wxNickname}）吗？"
        }
        alertDialog.showAlertWithOKAndCancel(
            message = message,
            okText = "提现",
            title = "提现确认",
            callback = MyAlertDialog.MyDialogCallback(onRight = {
                btn_ensure.isEnabled = false
                mViewModel.startApplyWithdraw(mSelectItem!!.tplId, currentWithdrawType)
            })
        )

    }

    private fun checkItem(position: Int) {
        val itemInfo = mAdapter.getItemOrNull(position) ?: return
        mSelectItem = itemInfo
        mAdapter.setSelection(position)
    }

    private fun checkPayType(withdrawType: String) {
        if (mSelectItem == null) {
            ToastUtils.show("请先选择提现金额")
            return
        }
        when (withdrawType) {
            WithdrawType.WXWithdraw -> {
                if (!wxAuthorized) {
                    alertDialog.showAlertWithOKAndCancel(
                        message = "微信未授权，完成授权即可提现",
                        okText = "去授权",
                        title = "授权提示",
                        callback = MyAlertDialog.MyDialogCallback(onRight = {
                            wxService?.weiXinAuth(this)
                        })
                    )
                    return
                }


            }
            WithdrawType.AliWithdraw -> {
                if (!aliAuthorized) {
                    alertDialog.showAlertWithOKAndCancel(
                        message = "支付宝未授权，完成授权即可提现",
                        okText = "去授权",
                        title = "授权提示",
                        callback = MyAlertDialog.MyDialogCallback(onRight = {
                            userBindViewModel.getAliPayAuthInfo()
                        })
                    )
                    return
                }

            }
        }
        currentWithdrawType = withdrawType
        btn_ensure.isEnabled = true
        when (currentWithdrawType) {
            WithdrawType.WXWithdraw -> {
                view_bg_wx.isSelected = true
                view_bg_ali.isSelected = false

            }
            WithdrawType.AliWithdraw -> {
                view_bg_wx.isSelected = false
                view_bg_ali.isSelected = true
            }
        }
    }

    private fun initViewModel() {
        mViewModel.withdrawData.observe(this, Observer {
            if (it.state == NetStateType.SUCCESS) {
                val data = it.getT()
                refreshData(data)
            }
        })
        mViewModel.withdrawResult.observe(this, Observer {
            btn_ensure.isEnabled = true
            if (it.state == NetStateType.SUCCESS) {
                val data = it.getT()
                ToastUtils.show(data.msg)
                account_money.text = data.cash
            } else if (it.state == NetStateType.ERROR) {
                when (it.error?.busiCode) {
                    WithdrawErrorCode.NO_BIND_PHONE -> {
                        alertDialog.showAlertWithOKAndCancel(
                            message = "你还未绑定手机，完成授权即可提现",
                            okText = "去绑定",
                            title = "未绑定手机提示",
                            callback = MyAlertDialog.MyDialogCallback(onRight = {
                                //todo 跳转绑定手机
                            })
                        )

                    }
                    WithdrawErrorCode.NO_VERIFIED -> {
                        alertDialog.showAlertWithOKAndCancel(
                            message = "你还未实名认证，完成认证即可提现",
                            okText = "去认证",
                            title = "实名认证提示",
                            callback = MyAlertDialog.MyDialogCallback(onRight = {
                                ARouter.getInstance().build(ARouterConstant.REALNAME_MAIN_ACTIVITY).navigation()
                            })
                        )

                    }
                    ErrorCodes.CASH_NOT_ENOUGH -> {
                        logger.info("零钱不足")
                        alertDialog.showAlertWithOKAndCancel(
                            message = "当前零钱余额不足，无法提现，快去赚钱获得更多吧~",
                            okText = "去赚钱",
                            title = "零钱不足",
                            callback = MyAlertDialog.MyDialogCallback(onRight = {
                                //男用户跳转到养鹊乐园，女用户跳转到任务页面
                            })
                        )

                    }
                }
            }
        })
        userBindViewModel.aliPayAuth.observe(this, Observer {
            btn_ensure.isEnabled = true
            if (it.state == NetStateType.SUCCESS) {
                val data = it.getT()
                logger.info("获取后台支付宝授权信息成功 开始发起授权")
                AliPayManager.aliAuth(this, data.authInfo)
            } else if (it.state == NetStateType.ERROR) {
//                ToastUtils.show("${it.error?.busiMessage}")
            }
        })

        userBindViewModel.bindAliData.observe(this, Observer {
            btn_ensure.isEnabled = true
            if (it.state == NetStateType.SUCCESS) {
                val data = it.getT()
                ToastUtils.show("绑定支付宝成功")
                refreshAliBindTag(true)
                aliNickname = data.nickname
            } else if (it.state == NetStateType.ERROR) {
                logger.info("绑定支付宝报错")
//                ToastUtils.show("${it.error?.busiMessage}")
            }
        })
        userBindViewModel.bindWinXinData.observe(this, Observer {
            btn_ensure.isEnabled = true
            if (it.state == NetStateType.SUCCESS) {
                val data = it.getT()
                ToastUtils.show("绑定微信成功")
                refreshWxBindTag(true)
                wxNickname = data.nickname
            } else if (it.state == NetStateType.ERROR) {
                logger.info("绑定微信报错")
//                ToastUtils.show("${it.error?.busiMessage}")
            }
        })

    }

    private var wxAuthorized: Boolean = false

    private var aliAuthorized: Boolean = false


    private var wxNickname: String = ""
    private var aliNickname: String = ""

    /**
     * 刷新数据
     */
    private fun refreshData(info: WithdrawInfo) {
        account_money.text = info.cash
        withdrawMoney.text = "今日奖励：${info.todayCash}元"
        totalMoney.text = "累计提现：${info.withdrawCash}元"
        mSelectItem = null
        btn_ensure.isEnabled = false
        mAdapter.setNewInstance(info.tplList)
        info.typeList.forEach { type ->
            if (type.type == WithdrawType.AliWithdraw) {
                refreshAliBindTag(type.authorized)
                aliNickname = type.nickname
            }
            if (type.type == WithdrawType.WXWithdraw) {
                refreshWxBindTag(type.authorized)
                wxNickname = type.nickname
            }
        }

    }

    private fun refreshAliBindTag(authorized: Boolean) {
        aliAuthorized = authorized
        if (aliAuthorized) {
            withdraw_tips_ali.hide()
        } else {
            withdraw_tips_ali.show()
        }
    }

    private fun refreshWxBindTag(authorized: Boolean) {
        wxAuthorized = authorized
        if (wxAuthorized) {
            withdraw_tips_wx.hide()
        } else {
            withdraw_tips_wx.show()
        }
    }

    override fun showLoadState(state: NetState) {
        when (state.state) {
            NetStateType.LOADING -> {
                state_pager_view.showLoading()
                sv_withdraw_root.hide()
                btn_ensure.hide()
            }
            NetStateType.SUCCESS -> {
                state_pager_view.showSuccess()
                sv_withdraw_root.show()
                btn_ensure.show()
            }
            NetStateType.ERROR, NetStateType.NETWORK_ERROR -> {
                sv_withdraw_root.hide()
                btn_ensure.hide()
                state_pager_view.showError(btnClick = View.OnClickListener {
                    mViewModel.queryInfo(QueryType.INIT)
                })
            }

        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun receiveWeiXinCode(event: WeiXinCodeEvent) {
        logger.info("收到微信授权code:${event.code}")
        userBindViewModel.bindWinXin(event.code)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun receiveAliCode(event: AliAuthCodeEvent) {
        logger.info("收到支付宝授权code:${event.code}")
        userBindViewModel.bindAliPay(event.code)
    }

}