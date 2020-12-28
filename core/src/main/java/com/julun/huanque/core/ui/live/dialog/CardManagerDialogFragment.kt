package com.julun.huanque.core.ui.live.dialog

import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.base.dialog.MyAlertDialog
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.bean.beans.ManagerInfo
import com.julun.huanque.common.bean.beans.ManagerOptionInfo
import com.julun.huanque.common.constant.IntentParamKey
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.onAdapterClickNew
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.core.R
import com.julun.huanque.core.ui.live.adapter.CardManagerAdapter
import com.julun.huanque.core.viewmodel.CardManagerViewModel
import kotlinx.android.synthetic.main.dialog_card_manager.*

/**
 * 名片管理弹窗
 * @author WanZhiYuan
 * @since 1.0.0
 * @date 2020/07/31
 */
class CardManagerDialogFragment : BaseDialogFragment() {

    private val adapter by lazy { CardManagerAdapter() }
    private var mViewModel: CardManagerViewModel? = null
    private var mConfirmDialog: MyAlertDialog? = null
    private var mDataResult: ManagerOptionInfo? = null
    private var programId: Long = 0
    private var targetId: Long = 0
    private var nickname: String = ""

    companion object {
        fun newInstance(
            bean: ManagerOptionInfo? = null,
            programId: Long = 0,
            targetUserId: Long = 0,
            nickname: String = ""
        ): CardManagerDialogFragment {
            val fragment = CardManagerDialogFragment()
            val bundle = Bundle()
            if (bean != null) {
                bundle.putSerializable(IntentParamKey.BEAN.name, bean)
            }
            bundle.putLong(IntentParamKey.PROGRAM_ID.name, programId)
            bundle.putLong(IntentParamKey.USER_ID.name, targetUserId)
            bundle.putString(IntentParamKey.NICK_NAME.name, nickname)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun getLayoutId(): Int = R.layout.dialog_card_manager

    override fun configDialog() {
        val window = dialog?.window ?: return
        val params = window?.attributes
        params?.dimAmount = 0.1f
        window?.attributes = params
        setDialogSize(Gravity.BOTTOM, 0, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
    override fun initViews() {

        rvList.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        rvList.adapter = adapter
        prepareViewModel()
        mDataResult =
            arguments?.getSerializable(IntentParamKey.BEAN.name) as? ManagerOptionInfo
        programId = arguments?.getLong(IntentParamKey.PROGRAM_ID.name, 0) ?: 0
        targetId = arguments?.getLong(IntentParamKey.USER_ID.name, 0) ?: 0
        nickname = arguments?.getString(IntentParamKey.NICK_NAME.name) ?: ""

        if (mDataResult == null) {
            mViewModel?.getManage(programId, targetId)
        } else {
            adapter.setList(mDataResult?.manageOptions ?: arrayListOf())
        }

        adapter.onAdapterClickNew { _, _, position ->
            when (val item = adapter.getItem(position)) {
                is ManagerInfo -> {
                    showAlertDialog(item.apply {
                        mangeType = mDataResult?.mangeType ?: ""
                        mangeTypeDesc = mDataResult?.mangeTypeDesc ?: ""
                    })
                }
                is ManagerOptionInfo -> {
                    if (item.manageOptions.isNotEmpty()) {
                        val dialog =
                            newInstance(
                                bean = item,
                                programId = programId,
                                targetUserId = targetId,
                                nickname = nickname
                            )
                        dialog.show(childFragmentManager, "CardManagerDialogFragment")
                    } else {
                        showAlertDialog(ManagerInfo().apply {
                            itemName = item.mangeTypeDesc
                            mangeType = item.mangeType
                            mangeTypeDesc = item.mangeTypeDesc
                        })
                    }
                }
            }
        }
        tvCancel.onClickNew {
            dismiss()
        }
    }

    private fun prepareViewModel() {
        mViewModel = ViewModelProvider(activity ?: return).get(CardManagerViewModel::class.java)
        mViewModel?.loadState?.observe(this, Observer {
            it ?: return@Observer
            when (it.state) {
                NetStateType.LOADING -> {
                    //加载中
                    pbProgress.show()
                }
                NetStateType.IDLE -> {
                    //闲置，什么都不做
                }
                else -> {
                    //异常或者是成功了
                    pbProgress.hide()
                }
            }
        })
        mViewModel?.listResult?.observe(this, Observer {
            it ?: return@Observer
            if (mDataResult != null) {
                return@Observer
            }
            adapter.setList(it)
        })
        mViewModel?.auditResult?.observe(this, Observer {
            it ?: return@Observer
            dismiss()
        })
    }

    private fun showAlertDialog(item: ManagerInfo) {
        mConfirmDialog =
            mConfirmDialog ?: MyAlertDialog(activity ?: return)
        mConfirmDialog?.showAlertWithOKAndCancel("确认将${nickname}${item.itemName}吗?"
            , MyAlertDialog.MyDialogCallback(
                onCancel = {},
                onRight = {
                    mViewModel?.saveManage(item, programId, targetId)
                }
            ), okText = "确定", noText = "取消")
    }

    override fun onDestroy() {
        super.onDestroy()
        mViewModel?.auditResult?.value = null
        mViewModel?.listResult?.value = null
    }
}