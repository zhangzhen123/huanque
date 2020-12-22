package com.julun.huanque.fragment

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.annotation.NonNull
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.julun.huanque.R
import com.julun.huanque.activity.MainActivity
import com.julun.huanque.activity.PhoneNumLoginActivity
import com.julun.huanque.activity.SelectSexActivity
import com.julun.huanque.common.constant.Agreement
import com.julun.huanque.common.helper.DensityHelper
import com.julun.huanque.common.helper.StorageHelper
import com.julun.huanque.common.suger.dp2px
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.ui.web.WebActivity
import com.julun.huanque.common.utils.ScreenUtils
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.common.utils.VerificationUtils
import com.julun.huanque.support.WXApiManager
import com.julun.huanque.viewmodel.LoginViewModel
import com.julun.huanque.viewmodel.PhoneNumLoginViewModel
import com.julun.huanque.widget.PasswordView
import com.trello.rxlifecycle4.android.FragmentEvent
import com.trello.rxlifecycle4.kotlin.bind
import com.trello.rxlifecycle4.kotlin.bindToLifecycle
import com.trello.rxlifecycle4.kotlin.bindUntilEvent
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.android.synthetic.main.frag_login.*
import org.jetbrains.anko.padding
import org.jetbrains.anko.sdk23.listeners.textChangedListener
import java.util.concurrent.TimeUnit

/**
 *@创建者   dong
 *@创建时间 2020/12/18 9:33
 *@描述 登录弹窗
 */
class LoginFragment : BottomSheetDialogFragment() {

    private val mLoginViewModel: LoginViewModel by activityViewModels()
    private val mPhoneNumLoginViewModel: PhoneNumLoginViewModel by activityViewModels()

    //是否正在倒计时
    private var mIsCountting: Boolean = false

    private var mBottomSheetBehavior: BottomSheetBehavior<View>? = null

    private val mBottomSheetBehaviorCallback: BottomSheetCallback = object : BottomSheetCallback() {
        override fun onSlide(bottomSheet: View, slideOffset: Float) {

        }

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                mBottomSheetBehavior?.setState(BottomSheetBehavior.STATE_COLLAPSED)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.frag_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

//    override fun getLayoutId() = R.layout.frag_login
//
//    override fun needEnterAnimation() = false

    //    override
    fun initViews() {
        view_weixin.onClickNew {
            mLoginViewModel.weixinLoginFlag.value = true
//            val intent = Intent(requireActivity(), TestAnimationActivity::class.java)
//            startActivity(intent)
        }
        register_rule.onClickNew {
            WebActivity.startWeb(requireActivity(), Agreement.UserAgreement)
        }
        tv_register_privacy.onClickNew {
            WebActivity.startWeb(requireActivity(), Agreement.PrivacyAgreement)
        }
//        tv_phone_num.onClickNew {
////            startActivity(Intent(requireActivity(), PhoneNumLoginActivity::class.java))
//            mLoginViewModel.currentFragmentState.value = LoginViewModel.Fragment_State_Phone
//        }
//        view_phone_number_fast_login.onClickNew {
//            mLoginViewModel.currentFragmentState.value = LoginViewModel.Fragment_State_Phone
//        }

        tv_phone_code.onClickNew {
            //获取短信验证码
            val phoneNum = phone_num.text.toString()
            if (checkPhone(phoneNum)) {
                tv_phone_num.text = "验证码已发送至 +86 $phoneNum"
                mPhoneNumLoginViewModel.startGetValidCode(phoneNum)
            }
//            mLoginViewModel.currentFragmentState.value = LoginViewModel.Fragment_State_Code
        }

        tv_resend.onClickNew {
            if (tv_resend.isSelected) {
                tv_phone_code.performClick()
            }
        }

        phone_num.textChangedListener {
            afterTextChanged {
                if (it != null) {
                    when (it.length) {
                        in 0..10 -> {
                            tv_phone_code?.isSelected = false

                        }
                        11 -> {
                            tv_phone_code.isSelected = true
                        }
                    }
                    if (it.isNotEmpty()) {
                        phone_num_clear.visibility = View.VISIBLE
                    } else {
                        phone_num_clear.visibility = View.INVISIBLE
                    }
                }
            }
        }

        tv_code_title.onClickNew {
            mLoginViewModel.currentFragmentState.value = LoginViewModel.Fragment_State_Phone
        }
        //手机号清除按钮
        phone_num_clear.onClickNew {
            phone_num.text.clear()
        }
        con.onClickNew {
            //关闭输入法
            closeKeyBoard()
        }
        password_view.setPasswordListener(object : PasswordView.PasswordListener {
            override fun keyEnterPress(password: String?, isComplete: Boolean) {
            }

            override fun passwordChange(changeText: String?) {
            }

            override fun passwordComplete() {
                val phoneNum = phone_num.text.toString()
                StorageHelper.setPhoneNumCache(phoneNum)
                mPhoneNumLoginViewModel.login(phoneNum, password_view.password)
            }

        })
    }

    override fun onStart() {
        super.onStart()
        mLoginViewModel.mShowLoginFragment = false
        initViewModel()
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        dialog?.setCanceledOnTouchOutside(false)
        val params = dialog?.window?.attributes
//        params?.flags =
//            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE; //这条就是控制点击背景的时候  如果被覆盖的view有点击事件那么就会直接触发(dialog消失并且触发背景下面view的点击事件)
//        setDialogSize(
//            Gravity.BOTTOM,
//            ViewGroup.LayoutParams.MATCH_PARENT,
//            ViewGroup.LayoutParams.MATCH_PARENT
//        )
        setWindowConfig()
    }

    private fun setWindowConfig() {
//        this.customDialogSize(Gravity.BOTTOM, 0, height = 450)
        //不需要半透明遮罩层
        val win = dialog?.window ?: return
//        win.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        win.setWindowAnimations(R.style.dialog_bottom_bottom_style)
        view?.post {
            val parent = view?.parent;
            if (parent is View) {
                val params = parent.layoutParams as? CoordinatorLayout.LayoutParams
                params?.width = ViewGroup.LayoutParams.MATCH_PARENT
                val behavior = params?.behavior;
                mBottomSheetBehavior = behavior as? BottomSheetBehavior
                mBottomSheetBehavior?.addBottomSheetCallback(mBottomSheetBehaviorCallback);
                mBottomSheetBehavior?.peekHeight = dp2px(850f)

                parent.setBackgroundColor(Color.TRANSPARENT);
            }

        }
//        setDialogSize1()
    }

    /**
     * 宽度显示比例
     */
    protected fun setDialogSize1(
        gravity: Int = Gravity.BOTTOM,
        width: Int = ViewGroup.LayoutParams.WRAP_CONTENT,
        height: Int = ViewGroup.LayoutParams.WRAP_CONTENT,
        padding: Int = 0
    ) {
        val window = dialog?.window ?: return
        val params = window.attributes
        window.decorView.padding = DensityHelper.dp2px(padding)
        params.gravity = gravity
        if (width > 0) {
            params.width = DensityHelper.dp2px(width)
        } else if (ViewGroup.LayoutParams.WRAP_CONTENT == width || ViewGroup.LayoutParams.MATCH_PARENT == width) {
            params.width = width
        }
        if (height > 0) {
            params.height = DensityHelper.dp2px(height)
        } else if (ViewGroup.LayoutParams.WRAP_CONTENT == height || ViewGroup.LayoutParams.MATCH_PARENT == height) {
            params.height = height
        }
        window.attributes = params
    }

    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {
        mLoginViewModel.currentFragmentState.observe(this, Observer {
            when (it) {
                LoginViewModel.Fragment_State_Phone -> {
                    //输入手机号状态
                    showInfoAnimation()
                }
                LoginViewModel.Fragment_State_Code -> {
                    //输入验证码页面
                    showCodeAnimation()
                }
                else -> {
                    //其他状态，不做处理
                }
            }

        })

        mPhoneNumLoginViewModel.codeReponse.observe(this, Observer {
            if (it == true) {
                //验证码发送成功
                tv_resend.isClickable = it == true
            }
        })

        mPhoneNumLoginViewModel.codeSendSuccess.observe(this, Observer {
            if (it == true) {
                mLoginViewModel.currentFragmentState.value = LoginViewModel.Fragment_State_Code
            }
        })


        //倒计时的观察
        mPhoneNumLoginViewModel?.tickState?.observe(this, Observer {
            if (it != null && it) {
                //极验通过  开始倒计时
                startTick()
            }
        })

        mPhoneNumLoginViewModel.codeErrorFlag.observe(this, Observer {
            if (it == true) {
                //验证码错误
                password_view.error = true
                Observable.timer(2, TimeUnit.SECONDS)
                    .bindToLifecycle(password_view)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        password_view.error = false
                        password_view.clearPassword()
                    }, {})
            }
        })

    }

    //倒计时使用
    var disposable: Disposable? = null

    /**
     * 开始倒计时
     */
    fun startTick() {
        tv_resend.isSelected = false
        stopTick()
        mIsCountting = true
        ToastUtils.show(R.string.send_code_success)
        val count = 61L
        disposable =
            Observable.intervalRange(1, count, 0, 1, TimeUnit.SECONDS)
//                .bindUntilEvent(this, FragmentEvent.DESTROY)
                .observeOn(AndroidSchedulers.mainThread())
                .map { count - it }
                .subscribe({
                    tv_resend.text = "重新获取(${it})"
                }, {}, {
//                    logger.info("倒计时完成了----")
                    tv_resend.isSelected = true
                    tv_resend.text = "重新获取"
                    mIsCountting = false
                })
    }

    fun stopTick() {
        if (disposable != null) {
            if (!disposable!!.isDisposed)
                disposable!!.dispose()
        }
    }


    private var mShowCodeSet: AnimatorSet = AnimatorSet()

    /**
     * 显示验证码动画
     */
    private fun showCodeAnimation() {
        if (con_code.visibility == View.VISIBLE && con_code.alpha == 1f) {
            //验证码布局处于可见状态
            return
        }
        if (mShowCodeSet.childAnimations.isEmpty()) {
            val showCodeAnimation = ObjectAnimator.ofFloat(con_code, "alpha", 0f, 1f)
                .apply { duration = 500 }

            val hideInfoAnimation = ObjectAnimator.ofFloat(con_phone_number, "alpha", 1f, 0f)
                .apply { duration = 500 }
            mShowCodeSet.playSequentially(showCodeAnimation, hideInfoAnimation)
            mShowCodeSet.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?) {
                    con_phone_number.hide()
                    password_view.requestFocus()
                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationStart(animation: Animator?) {
                    con_code.show()
                }

            })
        }
        mShowCodeSet.start()
    }

    private val mShowInfoSet: AnimatorSet = AnimatorSet()

    /**
     * 显示输入手机号布局
     */
    private fun showInfoAnimation() {
        if (con_phone_number.visibility == View.VISIBLE && con_phone_number.alpha == 1f) {
            //手机号布局处于可见状态
            return
        }
        if (mShowInfoSet.childAnimations.isEmpty()) {
            val hideCodeAnimation = ObjectAnimator.ofFloat(con_code, "alpha", 1f, 0f)
                .apply { duration = 500 }

            val showInfoAnimation = ObjectAnimator.ofFloat(con_phone_number, "alpha", 0f, 1f)
                .apply { duration = 500 }
            mShowInfoSet.playSequentially(hideCodeAnimation, showInfoAnimation)
            mShowInfoSet.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?) {
                    con_code.hide()
                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationStart(animation: Animator?) {
                    con_phone_number.show()
                }

            })
        }
        mShowInfoSet.start()
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

    /**
     * 关闭键盘
     */
    private fun closeKeyBoard() {
        ScreenUtils.hideSoftInput(phone_num)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        mLoginViewModel.finishState.value = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mShowCodeSet.cancel()
        mShowInfoSet.cancel()
        mBottomSheetBehavior?.removeBottomSheetCallback(mBottomSheetBehaviorCallback)
    }

}