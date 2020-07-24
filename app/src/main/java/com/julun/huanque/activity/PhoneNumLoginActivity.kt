package com.julun.huanque.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.julun.huanque.R
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.base.dialog.LoadingDialog
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.constant.IntentParamKey
import com.julun.huanque.common.constant.ParamConstant
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.GlobalUtils
import com.julun.huanque.common.utils.ScreenUtils
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.common.utils.VerificationUtils
import com.julun.huanque.viewmodel.PhoneNumLoginViewModel
import com.trello.rxlifecycle4.android.ActivityEvent
import com.trello.rxlifecycle4.kotlin.bindUntilEvent
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.android.synthetic.main.act_phone_num.*
import org.jetbrains.anko.sdk23.listeners.textChangedListener
import org.jetbrains.anko.textColor
import java.util.concurrent.TimeUnit


/**
 *@创建者   dong
 *@创建时间 2020/7/3 13:47
 *@描述 验证码登录页面
 * 新增手机绑定功能
 */
class PhoneNumLoginActivity : BaseActivity() {
    private val loadingDialog: LoadingDialog by lazy { LoadingDialog(this) }

    companion object {
        const val MAX_COUNT = 60L
        const val TYPE_LOGIN = 0
        const val TYPE_BIND = 1
    }

    //是登录操作还是绑定操作
    private var type: Int = 0
    private var mViewModel: PhoneNumLoginViewModel? = null

    //是否正在倒计时
    private var mIsCountting: Boolean = false

    override fun getLayoutId() = R.layout.act_phone_num

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        type = intent.getIntExtra(IntentParamKey.TYPE.name, 0)
        initViewModel()

        if (type == TYPE_LOGIN) {
            tv_title.show()
            csl_bind_tips.hide()
            login_btn.text = "登录"
        } else {
            tv_title.hide()
            csl_bind_tips.show()
            login_btn.text = "绑定"
        }
    }


    private fun initViewModel() {
        mViewModel = ViewModelProvider(this).get(PhoneNumLoginViewModel::class.java)

        mViewModel?.loadState?.observe(this, Observer {
            if (it != null) {
                if (it.state == NetStateType.LOADING) {
                    loadingDialog.showDialog()
                } else {
                    loadingDialog.hide()
                }
            }
        })

        //倒计时的观察
        mViewModel?.tickState?.observe(this, Observer {
            if (it != null && it) {
                //极验通过  开始倒计时
                startTick()
            }
        })

        mViewModel?.loginData?.observe(this, Observer {
            if (it != null) {
                if (it.regComplete) {
                    startActivity(Intent(this, LoginActivity::class.java))
                } else {
                    FillInformationActivity.newInstance(this)
                }
            }
        })
    }

    override fun initEvents(rootView: View) {
        view_top.imageViewBack.onClickNew {
            finish()
        }
        login_root.onClickNew {
            closeKeyBoard()
        }
        //手机号码编辑框信息变更
        phone_num.textChangedListener {
            afterTextChanged {
                if (it != null) {
                    when (it.length) {
                        in 0..10 -> {
                            when (mIsCountting) {
                                false -> get_code.isEnabled = false//验证码按钮不可点
                            }
                            login_btn?.isEnabled = false//登陆按钮不可点
                            when (it.length) {
                                0 -> phone_num_clear.visibility = View.INVISIBLE//清除号码编辑内容隐藏
                                else -> phone_num_clear.visibility = View.VISIBLE
                            }
                            get_code.isEnabled = false
                        }
                        11 -> {
                            when (mIsCountting) {
                                false -> {
                                    get_code.isEnabled = true
                                    get_code.textColor = GlobalUtils.getColor(R.color.black_333)
                                }
                            }
                            login_btn.isEnabled = code_num.editableText.length >= 4
                        }
                    }
                }
            }
        }
        code_num.textChangedListener {
            afterTextChanged {
                if (it != null) {
                    if (it.length >= 4) {
                        code_num_clear.visibility = View.VISIBLE

                        login_btn.isEnabled = phone_num.editableText.length >= 11
                    } else {
                        code_num_clear.visibility = View.GONE
                    }
                }
            }
        }

        login_btn.onClickNew {
            //登录
            val phoneNum = phone_num.text.toString()
            if (!checkPhone(phoneNum)) {
                ToastUtils.show("请输入正确手机号")
                return@onClickNew
            }
            val code = code_num.text.toString()
            if (code.length != 4) {
                ToastUtils.show("请输入4位验证码")
                return@onClickNew
            }

            mViewModel?.login(phoneNum, code)
            //直接跳转填写资料页面
//            FillInformationActivity.newInstance(this)
        }

        //验证码编辑框焦点
        code_num.setOnFocusChangeListener { v, hasFocus ->
            when (hasFocus) {
                true -> {
                    when (code_num?.editableText?.length) {
                        0 -> code_num_clear.visibility = View.INVISIBLE//清除号码编辑内容隐藏
                        else -> code_num_clear.visibility = View.VISIBLE
                    }
                }
                false -> code_num_clear.visibility = View.INVISIBLE//清除号码编辑内容隐藏
            }
        }

        get_code.onClickNew {
            //发送验证码
            val phone = phone_num.text.toString()
            if (checkPhone(phone)) {
                mViewModel?.startGetValidCode(phone)
            }
        }

        //验证码清除按钮
        code_num_clear.onClickNew {
            code_num.text.clear()
        }
        //手机号清除按钮
        phone_num_clear.onClickNew {
            phone_num.text.clear()
        }
    }

    /**
     * 验证手机号
     */
    private fun checkPhone(phone: String): Boolean {
        if (phone.isEmpty()) {
            ToastUtils.show(R.string.phone_error)
            return false
        }
        if (!VerificationUtils.matcherPhoneNum(phone)) {
            ToastUtils.show(R.string.phone_error)
            return false
        }
        return true
    }


    //倒计时使用
    var disposable: Disposable? = null

    /**
     * 开始倒计时
     */
    fun startTick() {
        get_code.isEnabled = false
        stopTick()
        mIsCountting = true
        ToastUtils.show(R.string.send_code_success)
        disposable = Observable.intervalRange(1, MAX_COUNT + 1, 0, 1, TimeUnit.SECONDS)
            .bindUntilEvent(this, ActivityEvent.DESTROY)
            .observeOn(AndroidSchedulers.mainThread())
            .map { MAX_COUNT + 1 - it }
            .subscribe({
                get_code.text = "${it}s"
            }, {}, {
                logger.info("倒计时完成了----")
                get_code.isEnabled = true
                get_code.text = "重新发送"
                mIsCountting = false
            })
    }

    fun stopTick() {
        if (disposable != null) {
            if (!disposable!!.isDisposed)
                disposable!!.dispose()
        }
    }


    fun closeKeyBoard() {
        ScreenUtils.hideSoftInput(this)
    }


}