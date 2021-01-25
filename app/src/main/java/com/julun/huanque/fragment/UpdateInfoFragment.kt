package com.julun.huanque.fragment

import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.julun.huanque.R
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.bean.forms.UpdateInformationForm
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.constant.SPParamKey
import com.julun.huanque.common.interfaces.EventListener
import com.julun.huanque.common.suger.inVisible
import com.julun.huanque.common.suger.loadImage
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.SPUtils
import com.julun.huanque.common.utils.ScreenUtils
import com.julun.huanque.common.utils.SessionUtils
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.viewmodel.FillInformationViewModel
import kotlinx.android.synthetic.main.fragment_update_info.*
import kotlinx.android.synthetic.main.fragment_update_info.con_root
import kotlinx.android.synthetic.main.fragment_update_info.et_nickname
import kotlinx.android.synthetic.main.fragment_update_info.tv_next

/**
 *@创建者   dong
 *@创建时间 2020/9/18 16:57
 *@描述 更新用户信息的弹窗
 */
class UpdateInfoFragment : BaseDialogFragment(), DialogInterface.OnKeyListener {
    companion object {
        /**
         * @param defaultNickname 是否默认昵称
         * @param defaultHeader 是否默认头像
         */
        const val DefaultHeader = "DefaultHeader"
        const val DefaultNickname = "DefaultNickname"
        fun newInstance(defaultNickname: String, defaultHeader: String): UpdateInfoFragment {
            val fragment = UpdateInfoFragment()
            val bundle = Bundle()
            bundle.putString(DefaultNickname, defaultNickname)
            bundle.putString(DefaultHeader, defaultHeader)
            fragment.arguments = bundle
            return fragment
        }
    }

    private val mViewModel: FillInformationViewModel by activityViewModels()


    override fun getLayoutId() = R.layout.fragment_update_info

    override fun initViews() {
        logger.info("Main UpdateInfoFragment act = ${requireActivity()}")
        SPUtils.commitString(SPParamKey.DefaultHeader, "")
        SPUtils.commitString(SPParamKey.DefaultNickname, "")
        initEvents()
        initViewModel()
        val defaultName = arguments?.getString(DefaultNickname) ?: ""
        val defaultHeader = arguments?.getString(DefaultHeader) ?: ""
        if (defaultName != BusiConstant.True) {
            //不是默认昵称
            et_nickname.setText(SessionUtils.getNickName())
        }

        if (defaultHeader != BusiConstant.True) {
            //不是默认头像
            mViewModel.headerPicData.value = SessionUtils.getHeaderPic()
        }

        mViewModel.nicknameChange = false
        mViewModel.nicknameEnable.value = true

    }

    private fun initViewModel() {
        mViewModel.nicknameEnable.observe(this, androidx.lifecycle.Observer {
            if (it == true) {
//                judgeNextEnable()
            }
        })
        mViewModel.updateSuccessFlag.observe(this, androidx.lifecycle.Observer {
            if (it == true) {
                mViewModel.updateSuccessFlag.value = null
                dismiss()
            }
        })

        mViewModel.headerPicData.observe(this, androidx.lifecycle.Observer {
            if (it != null) {
                if (it.contains("http")) {
                    sdv_header.setImageURI(it)
                } else {
                    sdv_header.loadImage(it, 90f, 90f)
                }
                judgeNextEnable()
            }
        })
    }

    private fun initEvents() {
        et_nickname.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                mViewModel.nicknameEnable.value = false
                mViewModel.nicknameChange = true
                val contentLength = s?.length ?: 0
                if (contentLength > 0) {
                    iv_clear_nickname.show()
                } else {
                    iv_clear_nickname.inVisible()
                }
                judgeNextEnable()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })

        iv_clear_nickname.onClickNew {
            //清空昵称
            et_nickname.setText("")
        }
        con_root.mEventListener = object : EventListener {
            override fun onDispatch(ev: MotionEvent?) {
                //昵称有变化再请求，没有变化 无需请求
                closeKeyBoard()
            }
        }

        tv_next.onClickNew {
            //下一步
//            if (!tv_next.isSelected) {
//                ToastUtils.show("请完善信息")
//                return@onClickNew
//            }
            //判断数据是否和之前一致
            val nickname = et_nickname.text.toString()
            val headerPic = mViewModel.headerPicData.value ?: ""
            if (nickname.isEmpty() || headerPic.isEmpty()) {
                //数据不全
                return@onClickNew
            }
            //校验昵称
            mViewModel.checkNickName(nickname)
        }

        sdv_header.onClickNew {
            //上传头像
            mViewModel.openPicFlag.value = true
        }

        tv_cancel.onClickNew {
            dismiss()
        }

    }


    override fun onStart() {
        super.onStart()
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.setOnKeyListener(this)
    }

    override fun configDialog() {
        setDialogSize(Gravity.CENTER, width = ViewGroup.LayoutParams.MATCH_PARENT, padding = 30)
    }

    /**
     * 判断下一步是否可用
     */
    private fun judgeNextEnable() {
        val nickname = et_nickname.text.toString()
        val header = mViewModel.headerPicData.value ?: ""

        tv_next.isSelected = nickname.isNotEmpty() && header.isNotEmpty()
    }

    fun closeKeyBoard() {
        ScreenUtils.hideSoftInput(requireActivity())
    }

    override fun onKey(dialog: DialogInterface?, keyCode: Int, event: KeyEvent?): Boolean {
        logger.info("Message keyCode = $keyCode")
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        } else {
            //这里注意当不是返回键时需将事件扩散，否则无法处理其他点击事件
            return false;
        }
    }

    override fun order() = 200

}