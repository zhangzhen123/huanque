package com.julun.huanque.ui.safe

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.alibaba.android.arouter.launcher.ARouter
import com.julun.huanque.R
import com.julun.huanque.activity.WelcomeActivity
import com.julun.huanque.common.base.BaseVMActivity
import com.julun.huanque.common.base.dialog.MyAlertDialog
import com.julun.huanque.common.basic.NetState
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.constant.*
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.utils.SharedPreferencesUtils
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.core.ui.live.PlayerActivity
import com.julun.huanque.support.LoginManager
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.android.synthetic.main.activity_destroy_account.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import java.util.concurrent.TimeUnit

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2019/12/5 9:57
 *
 *@Description: DestroyAccountActivity 注销账号页面
 *
 */
class DestroyAccountActivity : BaseVMActivity<DestroyAccountModel>() {

    private var dispose: Disposable? = null

    override fun getLayoutId(): Int = R.layout.activity_destroy_account

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {

        prepareViewModel()
        header.initHeaderView(titleTxt = "账号注销")
        apply_cancel.isEnabled = false
        val time = 10L
        apply_cancel.text = "申请注销(${time}s)"
        dispose = Observable.intervalRange(1, time, 0, 1L, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe({
            apply_cancel.text = "申请注销(${time - it}s)"
        }, {}, {
            apply_cancel.text = "申请注销"
            apply_cancel.isEnabled = true
            logger.info("倒计时完成")
        })

    }

    override fun initEvents(rootView: View) {
        header.imageViewBack.onClickNew {
            finish()
        }
        apply_cancel.onClickNew {
            if (SharedPreferencesUtils.getBoolean(SPParamKey.VOICE_ON_LINE, false)) {
                ToastUtils.show("正在语音通话，请稍后再试")
                return@onClickNew
            }
            mViewModel.checkDestroy()
        }

    }


    private fun prepareViewModel() {
        mViewModel.applyResult.observe(this, Observer {
            if (it != null && it.isSuccess()) {
                //
                LoginManager.loginOutSuccess({
                    toast("账号注销成功")
                    setResult(BusiConstant.DESTROY_ACCOUNT_RESULT_CODE)
                    startActivity<WelcomeActivity>()
                    finish()
                })

            } else if (it.state == NetStateType.ERROR) {
                val error = it.error ?: return@Observer
                val okText = if (error.busiCode == 1307) {
                    "取消"
                } else {
                    "去使用"
                }
                MyAlertDialog(this).showAlertWithOKAndCancel(error.busiMessage, MyAlertDialog.MyDialogCallback(onRight = {
                    when (error.busiCode) {
                        1305 -> {
                            startActivity<PlayerActivity>(ParamConstant.FROM to PlayerFrom.DESTROY_ACCOUNT)
                        }
                        1304 -> {
                            ARouter.getInstance().build(ARouterConstant.MAIN_ACTIVITY)
                                .withInt(IntentParamKey.TARGET_INDEX.name, MainPageIndexConst.MAIN_FRAGMENT_INDEX)
                                .navigation()
                        }
                        1307 -> {
                            //什么都不操作
                        }
                    }
                    setResult(BusiConstant.DESTROY_ACCOUNT_RESULT_CODE)
                    finish()

                }, onCancel = {
                    //注销
                    mViewModel.realDestroyAccount()
                }), title = "温馨提示", okText = okText, noText = "注销")

            }
        })

    }


    override fun onDestroy() {
        dispose?.dispose()
        super.onDestroy()
    }

    override fun showLoadState(state: NetState) {
    }


}