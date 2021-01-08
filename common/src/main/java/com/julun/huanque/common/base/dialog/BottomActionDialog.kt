package com.julun.huanque.common.base.dialog

import android.os.Bundle
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.julun.huanque.common.R
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.bean.beans.BottomAction
import com.julun.huanque.common.constant.IntentParamKey
import com.julun.huanque.common.suger.onAdapterClickNew
import kotlinx.android.synthetic.main.dialog_bottom_action.*

/**
 * 底部弹出框 通用的底部操作
 */
class BottomActionDialog : BaseDialogFragment() {

    companion object {
        fun newInstance(actions: ArrayList<BottomAction>): BottomActionDialog {
            val args = Bundle()
            args.putSerializable(IntentParamKey.OPERATE.name, actions)
            val fragment = BottomActionDialog()
            fragment.arguments = args
            return fragment
        }
        /**
         * 对于复用的弹窗 这里可以传入老的弹窗以复用
         */
        fun create(
            dialog: BottomActionDialog? = null,
            actions: ArrayList<BottomAction>,
            listener: OnActionListener? = null
        ): BottomActionDialog {
            return if (dialog == null) {
                newInstance(actions).apply {
                    this.listener = listener
                }
            } else {
                dialog.setActions(actions)
                dialog.listener = listener
                dialog
            }
        }
    }

    var listener: OnActionListener? = null
    private var actions: ArrayList<BottomAction>? = null
    fun setActions(actions: ArrayList<BottomAction>) {
        arguments?.putSerializable(IntentParamKey.OPERATE.name, actions)
    }

    override fun configDialog() {
        setDialogSize(width = ViewGroup.LayoutParams.MATCH_PARENT, height = ViewGroup.LayoutParams.WRAP_CONTENT)
    }
    override fun getLayoutId(): Int {
        return R.layout.dialog_bottom_action
    }

    override fun initViews() {
        action_list.layoutManager = LinearLayoutManager(requireContext())
        action_list.adapter = mAdapter
        mAdapter.onAdapterClickNew { _, _, position ->
            listener?.operate(mAdapter.getItemOrNull(position) ?: return@onAdapterClickNew)
            dismiss()
        }

        actions = arguments?.getSerializable(IntentParamKey.OPERATE.name) as? ArrayList<BottomAction>
        mAdapter.setList(actions)
    }

    override fun reCoverView() {
        actions = arguments?.getSerializable(IntentParamKey.OPERATE.name) as? ArrayList<BottomAction>
        mAdapter.setList(actions)
    }

    private val mAdapter =
        object : BaseQuickAdapter<BottomAction, BaseViewHolder>(R.layout.item_bottom_action), LoadMoreModule {
            override fun convert(holder: BaseViewHolder, item: BottomAction) {
                holder.setText(R.id.tv_action, item.tag)
            }
        }

    interface OnActionListener {
        fun operate(action: BottomAction)
    }
}