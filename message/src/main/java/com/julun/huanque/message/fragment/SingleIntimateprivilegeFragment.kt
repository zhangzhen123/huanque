package com.julun.huanque.message.fragment

import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.bean.beans.ConversationBasicBean
import com.julun.huanque.common.helper.DensityHelper
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.common.utils.IntimateUtil
import com.julun.huanque.message.R
import com.julun.huanque.message.adapter.PrivilegeAdapter
import com.julun.huanque.message.viewmodel.IntimateDetailViewModel
import kotlinx.android.synthetic.main.fragment_meet_detail.*

/**
 *@创建者   dong
 *@创建时间 2020/7/9 11:28
 *@描述 单个亲密度特权说明弹窗
 */
class SingleIntimateprivilegeFragment : BaseDialogFragment() {
    companion object {
        fun newInstance() = SingleIntimateprivilegeFragment()
    }


    override fun getLayoutId() = R.layout.fragment_meet_detail

    override fun initViews() {
    }


    override fun onStart() {
        super.onStart()
        setWindowConfig()
    }


    override fun needEnterAnimation() = false

    private fun setWindowConfig() {
        val window = dialog?.window ?: return
        val params = window.attributes
        params.gravity = Gravity.TOP
        params.width = window.windowManager.defaultDisplay.width - DensityHelper.dp2px(15f) * 2
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT
        params.y = DensityHelper.dp2px(60)
        window.attributes = params
    }
}