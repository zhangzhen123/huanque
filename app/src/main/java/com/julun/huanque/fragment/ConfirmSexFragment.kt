package com.julun.huanque.fragment

import android.content.DialogInterface
import android.view.Gravity
import android.view.KeyEvent
import androidx.activity.viewModels
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.julun.huanque.R
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.constant.Sex
import com.julun.huanque.common.suger.dp2px
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.viewmodel.SelectSexViewModel
import kotlinx.android.synthetic.main.frag_confirm_sex.*
import org.jetbrains.anko.imageResource

/**
 *@创建者   dong
 *@创建时间 2020/12/23 10:48
 *@描述 确认性别弹窗
 */
class ConfirmSexFragment : BaseDialogFragment(), DialogInterface.OnKeyListener {
    private val mViewModel: SelectSexViewModel by activityViewModels()
    override fun getLayoutId() = R.layout.frag_confirm_sex

    override fun initViews() {
        tv_rechoose.onClickNew {
            dismiss()
        }

        tv_go.onClickNew {
            mViewModel.updateSex(mViewModel.sexData.value == Sex.MALE)
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.setOnKeyListener(this)
        setDialogSize(Gravity.CENTER, 20, 384)
        initViewModel()
    }

    private fun initViewModel() {
        mViewModel.sexData.observe(this, Observer {
            if (it != null) {
                if (it == Sex.MALE) {
                    //男
                    iv_sex.imageResource = R.drawable.sel_male
                    tv_sex.text = "当前选择：男生"
                    tv_go.text = "我是男生"
                } else {
                    //女
                    iv_sex.imageResource = R.drawable.sel_female
                    tv_sex.text = "当前选择：女生"
                    tv_go.text = "我是女生"
                }

            }
        })
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
}