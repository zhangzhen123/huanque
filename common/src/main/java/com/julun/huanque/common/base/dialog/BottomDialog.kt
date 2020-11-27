package com.julun.huanque.common.base.dialog

import android.os.Bundle
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.julun.huanque.common.R
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.constant.IntentParamKey
import com.julun.huanque.common.suger.onAdapterClickNew
import kotlinx.android.synthetic.main.dialog_bottom_action.*

/**
 * 底部弹出框 通用的底部操作
 */
class BottomDialog : BaseDialogFragment() {

    companion object {
        fun newInstance(actions: ArrayList<Action>): BottomDialog {
            val args = Bundle()
            args.putSerializable(IntentParamKey.OPERATE.name, actions)
            val fragment = BottomDialog()
            fragment.arguments = args
            return fragment
        }
    }

    var listener: OnActionListener? = null
    private var actions: ArrayList<Action>? = null
    fun setActions(actions: ArrayList<Action>) {
        arguments?.putSerializable(IntentParamKey.OPERATE.name, actions)
    }

    override fun onStart() {
        super.onStart()
        setDialogSize(width = ViewGroup.LayoutParams.MATCH_PARENT, height = ViewGroup.LayoutParams.WRAP_CONTENT)
//        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
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

        actions = arguments?.getSerializable(IntentParamKey.OPERATE.name) as? ArrayList<Action>
        mAdapter.setList(actions)
    }

    override fun reCoverView() {
        actions = arguments?.getSerializable(IntentParamKey.OPERATE.name) as? ArrayList<Action>
        mAdapter.setList(actions)
    }

    private val mAdapter =
        object : BaseQuickAdapter<Action, BaseViewHolder>(R.layout.item_bottom_action), LoadMoreModule {
            override fun convert(holder: BaseViewHolder, item: Action) {
                holder.setText(R.id.tv_action, item.tag)
            }
        }

    class Action(var code: String, var tag: String)
    interface OnActionListener {
        fun operate(action: Action)
    }
}