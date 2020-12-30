package com.julun.huanque.fragment

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.julun.huanque.R
import com.julun.huanque.common.constant.Agreement
import com.julun.huanque.common.constant.SPParamKey
import com.julun.huanque.common.helper.StorageHelper
import com.julun.huanque.common.suger.dp2px
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.ui.web.WebActivity
import com.julun.huanque.common.utils.ScreenUtils
import com.julun.huanque.common.utils.SharedPreferencesUtils
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.common.utils.VerificationUtils
import com.julun.huanque.support.LoginManager
import com.julun.huanque.viewmodel.LoginViewModel
import com.julun.huanque.viewmodel.PhoneNumLoginViewModel
import com.julun.huanque.widget.PasswordView
import com.trello.rxlifecycle4.kotlin.bindToLifecycle
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.android.synthetic.main.frag_login.*
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
        phone_num.post {
            val num = StorageHelper.getPhoneNumCache()
            if (num.isNotEmpty()) {
                phone_num.setText(num)
                phone_num.setSelection(phone_num.text.length)
            }
        }

    }

//    override fun getLayoutId() = R.layout.frag_login
//
//    override fun needEnterAnimation() = false

    //    override
    fun initViews() {
        val lastLogin = SharedPreferencesUtils.getInt(SPParamKey.Last_Login, -1)
        iv_last_login_wx.hide()
        iv_last_login_phone.hide()

        if (lastLogin == LoginManager.MOBILE_FAST_LOGIN || lastLogin == LoginManager.MOBILE_LOGIN) {
            iv_last_login_phone.show()
        }
        if (lastLogin == LoginManager.WECHAT_LOGIN) {
            iv_last_login_wx.show()
        }
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
            val phoneNum = phone_num.text.toString().trim().replace(" ", "")
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

        phone_num.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(it: Editable?) {
                if (it != null) {
                    val phneNum = phone_num.text.toString().trim().replace(" ", "")
                    tv_phone_code.isSelected = phneNum.length >= 11
                    if (it.isNotEmpty()) {
                        phone_num_clear.visibility = View.VISIBLE
                    } else {
                        phone_num_clear.visibility = View.INVISIBLE
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                if (charSequence == null || charSequence.isEmpty()) return
                val stringBuilder = StringBuilder()
                for (i in charSequence.indices) {
                    if (i != 3 && i != 8 && charSequence.get(i) === ' ') {
                        continue
                    } else {
                        stringBuilder.append(charSequence.get(i))
                        if ((stringBuilder.length == 4 || stringBuilder.length == 9)
                            && stringBuilder[stringBuilder.length - 1] != ' '
                        ) {
                            stringBuilder.insert(stringBuilder.length - 1, ' ')
                        }
                    }
                }
                if (stringBuilder.toString() != charSequence.toString()) {
                    var index = start + 1
                    if (stringBuilder[start] == ' ') {
                        if (before == 0) {
                            index++
                        } else {
                            index--
                        }
                    } else {
                        if (before == 1) {
                            index--
                        }
                    }
                    phone_num.setText(stringBuilder.toString())
                    phone_num.setSelection(phone_num.text.length)
                }

            }

        })

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
                val phoneNum = phone_num.text.toString().trim().replace(" ", "")
                StorageHelper.setPhoneNumCache(phoneNum)
                mPhoneNumLoginViewModel.login(phoneNum, password_view.password)
                closeKeyBoard()
            }

        })
        password_view.setOnLongClickListener {
            switchPopupWindow(password_view)
            return@setOnLongClickListener true
        }
    }

    override fun onStart() {
        super.onStart()
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
//                    ScreenUtils.showSoftInput(phone_num)
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

        mLoginViewModel.pasteFlag.observe(this, Observer {
            if (it == true) {
                //粘贴操作
                paste()
                mLoginViewModel.pasteFlag.value = null
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
                mPhoneNumLoginViewModel.codeSendSuccess.value = null
            }
        })


        //倒计时的观察
        mPhoneNumLoginViewModel?.tickState?.observe(this, Observer {
            if (it != null && it) {
                //极验通过  开始倒计时
                startTick()
                mPhoneNumLoginViewModel?.tickState?.value = null
            }
        })

        mPhoneNumLoginViewModel.codeErrorFlag.observe(this, Observer {
            if (it == true) {
                mPhoneNumLoginViewModel.codeErrorFlag.value = null
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

        mPhoneNumLoginViewModel.phoneNumError.observe(this, Observer {
            if (it == true) {
                phone_num.setText("")
                mPhoneNumLoginViewModel.phoneNumError.value = null
            }
        })

    }

    //粘贴
    private fun paste() {
        // 获取系统剪贴板
        val clipboard =
            requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager

        // 获取剪贴板的剪贴数据集
        val clipData = clipboard?.primaryClip ?: return
        val numList = mutableListOf<String>("0", "1", "2", "3", "4", "5", "6", "7", "8", "9")
        if (clipData.itemCount > 0) {
            // 从数据集中获取（粘贴）第一条文本数据
            val text = clipData.getItemAt(0).text?.toString() ?: return
            text.forEach {
                if (!numList.contains("$it")) {
                    //非数字
                    ToastUtils.show("复制的文本中应该全部为数字")
                    return
                }
            }
            if (text.length > 4) {
                ToastUtils.show("复制的数字超过4位")
                return
            }
            text.forEach {
                password_view.add("$it")
            }
        }
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
                    ScreenUtils.showSoftInput(password_view)
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


    //复制PopupWindow
    private var mCopyPopupWindow: PopupWindow? = null

    /**
     * 显示热度和时间切换的PopupWindow
     */
    private fun switchPopupWindow(parentView: View) {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.view_login_copy, null)
        val pWidht = (ScreenUtils.getScreenWidth() - dp2px(75)) / 4
        mCopyPopupWindow = PopupWindow(view, pWidht, dp2px(37))
        val drawable = ColorDrawable(Color.TRANSPARENT)
        mCopyPopupWindow?.setBackgroundDrawable(drawable)
        mCopyPopupWindow?.isOutsideTouchable = true
        view.findViewById<View>(R.id.con_paste).onClickNew {
            //粘贴
            mLoginViewModel.pasteFlag.value = true
            mCopyPopupWindow?.dismiss()
        }
        //获取自身的长宽高
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        val popupHeight = view.measuredHeight;
        val popupWidth = view.measuredWidth;
        mCopyPopupWindow?.showAsDropDown(
            parentView,
            0,
            -(popupHeight + parentView.height - dp2px(17))
        )
//        mCopyPopupWindow?.showAtLocation(parentView, Gravity.LEFT or Gravity.TOP, 0, 0)
//        mCopyPopupWindow?.show

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
            phone_num.setText("")
            return false
        }
        return true
    }

    /**
     * 关闭键盘
     */
    private fun closeKeyBoard() {
        ScreenUtils.hideSoftInput(phone_num)
        phone_num.clearFocus()
        password_view.clearFocus()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        mLoginViewModel.finishState.value = true
    }


    override fun onResume() {
        super.onResume()
        Observable.timer(500, TimeUnit.MILLISECONDS)
            .bindToLifecycle(con)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                phone_num.requestFocus()
                ScreenUtils.showSoftInput(phone_num)

            }, {})
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mShowCodeSet.cancel()
        mShowInfoSet.cancel()
        mBottomSheetBehavior?.removeBottomSheetCallback(mBottomSheetBehaviorCallback)
        //隐藏键盘
        closeKeyBoard()
    }

}