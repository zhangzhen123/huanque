package com.julun.huanque.core.ui.homepage

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.alibaba.android.arouter.facade.annotation.Route
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.base.dialog.MyAlertDialog
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.constant.ParamConstant
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.ForceUtils
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.core.R
import com.julun.huanque.core.viewmodel.UpdateSignViewModel
import kotlinx.android.synthetic.main.act_update_sign.*
import org.greenrobot.eventbus.EventBus

/**
 *@创建者   dong
 *@创建时间 2020/12/30 14:36
 *@描述 修改个性签名页面
 */
@Route(path = ARouterConstant.UpdateSignActivity)
class UpdateSignActivity : BaseActivity() {

    companion object {
        fun newInstance(act: Activity, sign: String) {
            val intent = Intent(act, UpdateSignActivity::class.java)
            if (ForceUtils.activityMatch(intent)) {
                intent.putExtra(ParamConstant.SIGN, sign)
                act.startActivity(intent)
            }
        }
    }

    //更新用户昵称ViewModel
    private val mViewModel: UpdateSignViewModel by viewModels()

    override fun getLayoutId() = R.layout.act_update_sign

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        mViewModel.mOriginalSign = intent?.getStringExtra(ParamConstant.SIGN) ?: ""
        header_page.textTitle.text = "个性签名"
        header_page.textOperation.show()
        header_page.textOperation.text = "发布"

        initViewModel()
    }

    override fun initEvents(rootView: View) {
        header_page.imageViewBack.onClickNew {
            onBackPressed()
        }
        header_page.textOperation.onClickNew {
            //保存按钮
            mViewModel.updateSign(et_sign.text.toString())
        }
        et_sign.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                tv_num_hint.text = "${s?.length ?: 0}/50"
                header_page.textOperation.isEnabled = s?.isNotEmpty() == true && s.toString() != mViewModel.mOriginalSign
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })

        et_sign.setText(mViewModel.mOriginalSign)
    }

    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {
        mViewModel.perfectionData.observe(this, Observer {
            if (it != null) {
                ToastUtils.show("个性签名保存成功")
                EventBus.getDefault().post(it)
                finish()
            }
        })
    }

    override fun onBackPressed() {
        val localnickname = et_sign.text.toString()
        if (localnickname.isNotEmpty() && localnickname != mViewModel.mOriginalSign) {
            //昵称有变动，出现提示
            MyAlertDialog(
                this, true
            ).showAlertWithOKAndCancelAny(
                "你的个性签名已经修改，是否保存后退出？",
                MyAlertDialog.MyDialogCallback(onRight = {
                    mViewModel.updateSign(et_sign.text.toString())
                }, onCancel = { finish() }), "保存提示", "保存", noText = "不保存"
            )
        } else {
            //直接返回
            super.onBackPressed()
        }
    }

}