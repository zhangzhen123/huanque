package com.julun.huanque.realname

import android.os.Bundle
import android.view.View
import android.widget.EditText
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.constant.RealNameConstants
import com.julun.huanque.common.interfaces.routerservice.IRealNameService
import com.julun.huanque.common.interfaces.routerservice.RealNameCallback
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.utils.ScreenUtils
import com.julun.huanque.common.utils.ToastUtils
import kotlinx.android.synthetic.main.activity_main_realname.*
import org.jetbrains.anko.sdk23.listeners.textChangedListener

/**
 * 实名认证首页
 * @author WanZhiYuan
 * @since 1.0.0
 * @date 2020/07/09
 */
@Route(path = ARouterConstant.REALNAME_MAIN_ACTIVITY)
class RealNameActivity : BaseActivity() {

    private var mRealNameService: IRealNameService? = null

    override fun getLayoutId(): Int = R.layout.activity_main_realname

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {

    }

    override fun initEvents(rootView: View) {
        ivback.onClickNew {
            finish()
        }
        clRealRootView.onClickNew {
            closeKeyBoard()
        }
        edtRealname.onClickNew {
            openKeyBoard(edtRealname)
        }
        edtIDCard.onClickNew {
            openKeyBoard(edtIDCard)
        }
        edtRealname.textChangedListener {
            afterTextChanged {
                isHighLight()
            }
        }
        edtIDCard.textChangedListener {
            afterTextChanged {
                isHighLight()
            }
        }
        btnCommit.onClickNew {
            mRealNameService =
                mRealNameService ?: ARouter.getInstance().build(ARouterConstant.REALNAME_SERVICE)
                    .navigation() as? IRealNameService
            mRealNameService?.startRealName(this,edtRealname.text.toString(),edtIDCard.text.toString(),object :RealNameCallback{
                override fun onCallback(status: String, des: String) {
                    when (status) {
                        RealNameConstants.TYPE_SUCCESS -> {
                            //认证成功
                            ToastUtils.show(des)
                        }
                        RealNameConstants.TYPE_FAIL, RealNameConstants.TYPE_ERROR -> {
                            //认证失败 or 认证网络请求异常
                            ToastUtils.show(des)
                        }
                        else -> {
                            //认证取消
                            ToastUtils.show("${if(des.isNotEmpty()){
                                des
                            }else{
                                "认证取消"
                            }}")
                        }
                    }
                }
            })
        }
    }

    private fun isHighLight() {
        if (edtRealname.editableText.isNotEmpty() && edtIDCard.editableText.length >= 18) {
            btnCommit?.isEnabled = true
        }
    }

    private fun openKeyBoard(view: EditText) {
        view.isFocusable = true
        view.isFocusableInTouchMode = true
        view.requestFocus()
        ScreenUtils.showSoftInput(this, view)
    }

    private fun closeKeyBoard() {
        when (ScreenUtils.isSoftInputShow(this)) {
            true -> {
                ScreenUtils.hideSoftInput(this)
                hideViewFocus(edtRealname, edtIDCard)
            }
        }
    }

    private fun hideViewFocus(vararg view: EditText) {
        view.forEach {
            it.isFocusable = false
            it.isFocusableInTouchMode = false
            it.requestFocus()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mRealNameService?.release()
    }
}