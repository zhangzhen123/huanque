package com.julun.huanque.message.fragment

import android.content.DialogInterface
import android.view.Gravity
import android.view.KeyEvent
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.message.R
import com.julun.huanque.message.adapter.PrivateOrderAdapter
import com.julun.huanque.message.viewmodel.ChatSendGiftViewModel
import com.julun.huanque.message.viewmodel.PrivateConversationViewModel
import kotlinx.android.synthetic.main.frag_private_order.*

/**
 *@创建者   dong
 *@创建时间 2021/1/20 20:56
 *@描述 私信排队Fragment
 */
class PrivateOrderFragment : BaseDialogFragment(), DialogInterface.OnKeyListener {
    private val mPrivateConversationViewModel: PrivateConversationViewModel by activityViewModels()
    private val sendGiftviewModel: ChatSendGiftViewModel by activityViewModels()
    private val mPrivateOrderAdapter = PrivateOrderAdapter()
    override fun getLayoutId() = R.layout.frag_private_order

    override fun initViews() {
        initRecyclerView()
        tv_cancel.onClickNew {
            //关闭弹窗和页面
            mPrivateConversationViewModel.finishState.value = true
            dismiss()
        }
        tv_send.onClickNew {
            //送礼
            val tempGift = mPrivateOrderAdapter.getItemOrNull(mPrivateOrderAdapter.selectPosition) ?: return@onClickNew
            val targetID = mPrivateConversationViewModel.targetIdData.value ?: return@onClickNew
            sendGiftviewModel.sendGift(targetID, tempGift, 1)
        }
    }

    override fun configDialog() {
        setDialogSize(Gravity.CENTER, 20, ViewGroup.LayoutParams.WRAP_CONTENT)
    }


    private fun initRecyclerView() {
        recycler_view.layoutManager = GridLayoutManager(requireContext(), 3)
        recycler_view.adapter = mPrivateOrderAdapter
        mPrivateOrderAdapter.setOnItemClickListener { adapter, view, position ->
            mPrivateOrderAdapter.selectPosition = position
            mPrivateOrderAdapter.notifyDataSetChanged()
        }
    }


    override fun onStart() {
        super.onStart()
        initViewModel()
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.setOnKeyListener(this)
    }

    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {
        mPrivateConversationViewModel.basicBean.observe(this, Observer {
            if (it != null) {
                val guideInfo = it.guideInfo ?: return@Observer
                tv_num.text = "${guideInfo.waitNum}人正在等待聊天…"
                mPrivateOrderAdapter.setList(guideInfo.giftList)
            }
        })

        sendGiftviewModel.sendResult.observe(viewLifecycleOwner, Observer {
            //
//            tv_send.isEnabled = true
            it ?: return@Observer
            logger.info("赠送返回=${it.state}")
            if (it.state == NetStateType.SUCCESS) {
//                refreshSendResult(it.getT())
                dismiss()
            } else if (it.state == NetStateType.ERROR) {
                //dodo
                if (it.error?.busiCode == 1001) {
                    //余额不足
                    mPrivateConversationViewModel.balanceNotEnoughFlag.value = true
                } else {
                    ToastUtils.show(it.error?.busiMessage ?: return@Observer)
                }
                sendGiftviewModel.sendResult.value = null
            }
        })
        sendGiftviewModel.sendGiftBean.observe(this, Observer {
            if (it != null) {
                mPrivateConversationViewModel.sendGiftSuccessData.value = it
                sendGiftviewModel.sendGiftBean.value = null
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