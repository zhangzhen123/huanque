package com.julun.huanque.message.fragment

import android.view.Gravity
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.message.R
import com.julun.huanque.message.viewmodel.HeartBeatViewModel
import kotlinx.android.synthetic.main.frag_heart_unlock.*

/**
 *@创建者   dong
 *@创建时间 2021/1/21 16:22
 *@描述 解锁弹窗
 */
class HeartUnLockFragment : BaseDialogFragment() {
    private val mActViewModel: HeartBeatViewModel by activityViewModels()
    override fun getLayoutId() = R.layout.frag_heart_unlock

    override fun initViews() {
        tv_cancel.onClickNew {
            dismiss()
        }
        tv_certain.onClickNew {
            val tempData = mActViewModel.mSingleHeartBean ?: return@onClickNew
            mActViewModel.unlock(tempData.logId, tempData.userId)
        }
    }


    override fun configDialog() {
        setDialogSize(Gravity.CENTER, 20, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onResume() {
        super.onResume()
        initViewModel()
        tv_count.text = "剩余解锁次数：${mActViewModel.unLockCount}次"
    }

    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {
        mActViewModel.unlockLogId.observe(this, Observer {
            if (it != null) {
                mActViewModel.unlockLogId.value = null
                ToastUtils.show("解锁成功")
                dismiss()
            }
        })
    }

}