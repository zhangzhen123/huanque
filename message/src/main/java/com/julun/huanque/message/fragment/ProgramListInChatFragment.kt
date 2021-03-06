package com.julun.huanque.message.fragment

import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.launcher.ARouter
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.bean.beans.SingleProgramInConversation
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.constant.IntentParamKey
import com.julun.huanque.common.constant.ParamConstant
import com.julun.huanque.common.constant.PlayerFrom
import com.julun.huanque.common.suger.dp2px
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.widgets.recycler.decoration.VerticalItemDecoration
import com.julun.huanque.message.R
import com.julun.huanque.message.adapter.ProgramInChatAdapter
import com.julun.huanque.message.viewmodel.PrivateConversationViewModel
import com.luck.picture.lib.tools.ScreenUtils
import kotlinx.android.synthetic.main.act_private_chat.view_bg
import kotlinx.android.synthetic.main.fragment_program_listin_chat.*

/**
 *@创建者   dong
 *@创建时间 2020/11/10 11:20
 *@描述 直播列表弹窗
 */
class ProgramListInChatFragment : BaseDialogFragment() {
    private val mPrivateConversationViewModel: PrivateConversationViewModel by activityViewModels()
    private val mAdapter = ProgramInChatAdapter()
    override fun getLayoutId() = R.layout.fragment_program_listin_chat

    override fun initViews() {
        //弹窗宽度
        val height = ScreenUtils.getScreenHeight(requireContext()) - ScreenUtils.getStatusBarHeight(requireContext())
        val blackWidth = (height - dp2px(45 + 19 + 10 * 2)) / 3 + dp2px(16)
        val blackParams = view_bg.layoutParams as? ConstraintLayout.LayoutParams
        blackParams?.width = blackWidth
        view_bg.layoutParams = blackParams

        initRecyclerView()

        iv_close.onClickNew {
            dismiss()
        }
    }

    /**
     * 初始化ViewModel
     */
    private fun initRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        recyclerView.adapter = mAdapter
        recyclerView.addItemDecoration(VerticalItemDecoration(dp2px(10)))
        mAdapter.setOnItemClickListener { adapter, view, position ->
            val tempBean = adapter.getItemOrNull(position) as? SingleProgramInConversation ?: return@setOnItemClickListener
            val bundle = Bundle()
            bundle.putLong(IntentParamKey.PROGRAM_ID.name, tempBean.programId)
            bundle.putString(ParamConstant.FROM, PlayerFrom.Chat)
            val targetId = mPrivateConversationViewModel?.targetIdData?.value ?: 0L
            bundle.putString(ParamConstant.ShareUserId, "$targetId")

            ARouter.getInstance().build(ARouterConstant.PLAYER_ACTIVITY).with(bundle).navigation()
            dismiss()
        }
    }

    override fun configDialog() {
        //不需要半透明遮罩层
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        setDialogSize(Gravity.END, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, 0)
    }
    override fun onStart() {
        super.onStart()

        initViewModel()
    }

    override fun setWindowAnimations() {
        dialog?.window?.setWindowAnimations(com.julun.huanque.common.R.style.dialog_right_right_style)
    }

    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {
        mPrivateConversationViewModel.programListData.observe(this, Observer {
            if (it != null) {
                mAdapter.setList(it)
            }
        })
    }
}