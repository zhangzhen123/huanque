package com.julun.huanque.core.ui.homepage

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.base.dialog.MyAlertDialog
import com.julun.huanque.common.constant.ParamConstant
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.ForceUtils
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.core.R
import com.julun.huanque.core.viewmodel.UpdateNicknameViewModel
import kotlinx.android.synthetic.main.act_update_nickname.*
import org.greenrobot.eventbus.EventBus

/**
 *@创建者   dong
 *@创建时间 2020/12/30 14:36
 *@描述 修改昵称页面
 */
class UpdateNicknameActivity : BaseActivity() {

    companion object {
        fun newInstance(act: Activity, nickname: String) {
            val intent = Intent(act, UpdateNicknameActivity::class.java)
            if (ForceUtils.activityMatch(intent)) {
                intent.putExtra(ParamConstant.NICKNAME, nickname)
                act.startActivity(intent)
            }
        }
    }

    //更新用户昵称ViewModel
    private val mViewModel: UpdateNicknameViewModel by viewModels()

    override fun getLayoutId() = R.layout.act_update_nickname

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        mViewModel.mOriginalNickname = intent?.getStringExtra(ParamConstant.NICKNAME) ?: ""
        header_page.textTitle.text = "修改昵称"
        header_page.textOperation.show()
        header_page.textOperation.text = "保存"

//        tv_num_hint.text = "${mViewModel.mOriginalNickname.length}/10"
        initViewModel()
    }

    override fun initEvents(rootView: View) {
        header_page.imageViewBack.onClickNew {
            onBackPressed()
        }
        header_page.textOperation.onClickNew {
            //保存按钮
            mViewModel.updateNickname(et_sign.text.toString())
        }
        et_sign.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                tv_num_hint.text = "${s?.length ?: 0}/10"
                header_page.textOperation.isEnabled = s?.isNotEmpty() == true && s.toString() != mViewModel.mOriginalNickname
                if (s?.isNotEmpty() == true) {
                    phone_num_clear.show()
                } else {
                    phone_num_clear.hide()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })
        phone_num_clear.onClickNew {
            et_sign.setText("")
        }

        et_sign.setText(mViewModel.mOriginalNickname)
    }

    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {
        mViewModel.nicknameUpdateSuccessData.observe(this, Observer {
            if (it == true) {
                ToastUtils.show("昵称保存成功")
                finish()
            }
        })
    }

    override fun onBackPressed() {
        val localnickname = et_sign.text.toString()
        if (localnickname.isNotEmpty() && localnickname != mViewModel.mOriginalNickname) {
            //昵称有变动，出现提示
            MyAlertDialog(
                this, true
            ).showAlertWithOKAndCancelAny(
                "你的昵称已经修改，是否保存后退出？",
                MyAlertDialog.MyDialogCallback(onRight = {
                    mViewModel.updateNickname(et_sign.text.toString())
                }, onCancel = { finish() }), "保存提示", "保存", noText = "不保存"
            )
        } else {
            //直接返回
            super.onBackPressed()
        }
    }

}