package com.julun.huanque.message.fragment

import android.view.Gravity
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.suger.onClick
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.message.R
import com.julun.huanque.message.viewmodel.UsefulWordViewModel
import kotlinx.android.synthetic.main.fragment_words_action.*

/**
 *@创建者   dong
 *@创建时间 2020/10/12 14:46
 *@描述 常用语操作Fragment
 */
class WordsActionFragment : BaseDialogFragment() {

    private val mUsefulWordViewModel: UsefulWordViewModel by activityViewModels()

    override fun getLayoutId() = R.layout.fragment_words_action

    override fun initViews() {
        tv_edit.onClickNew {
            //编辑
            mUsefulWordViewModel.actionData.value = UsefulWordViewModel.ACTION_EDIT
            dismiss()
        }
        tv_del.onClickNew {
            //删除
            mUsefulWordViewModel.actionData.value = UsefulWordViewModel.ACTION_DELETE
            dismiss()
        }
        tv_cancel.onClickNew {
            //取消
            dismiss()
        }
    }

    override fun configDialog() {
        setDialogSize(Gravity.BOTTOM, 0, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}