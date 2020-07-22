package com.julun.huanque.message.fragment

import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.bean.beans.ConversationBasicBean
import com.julun.huanque.common.bean.beans.IntimatePrivilege
import com.julun.huanque.common.constant.ParamConstant
import com.julun.huanque.common.constant.ParamKey
import com.julun.huanque.common.helper.DensityHelper
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.common.utils.IntimateUtil
import com.julun.huanque.message.R
import com.julun.huanque.message.adapter.PrivilegeAdapter
import com.julun.huanque.message.viewmodel.IntimateDetailViewModel
import kotlinx.android.synthetic.main.fragment_meet_detail.*
import kotlinx.android.synthetic.main.fragment_single_intimate_privilege.*

/**
 *@创建者   dong
 *@创建时间 2020/7/9 11:28
 *@描述 单个亲密度特权说明弹窗
 */
class SingleIntimateprivilegeFragment : BaseDialogFragment() {
    companion object {
        fun newInstance(bean: IntimatePrivilege, currentLevel: Int): SingleIntimateprivilegeFragment {
            val fragment = SingleIntimateprivilegeFragment()
            SingleIntimateprivilegeFragment()
            fragment.arguments = Bundle().apply {
                putSerializable(ParamConstant.IntimatePrivilege, bean)
                putInt(ParamConstant.CurrentIntimatePrivilege, currentLevel)
            }
            return fragment
        }
    }


    override fun getLayoutId() = R.layout.fragment_single_intimate_privilege

    override fun initViews() {
        val bean = arguments?.getSerializable(ParamConstant.IntimatePrivilege) as? IntimatePrivilege
            ?: return
        val currentLevel = arguments?.getInt(ParamConstant.CurrentIntimatePrivilege) ?: 0
        tv_title.text = bean.title
        ImageUtils.loadImage(sdv_pic, bean.privilegeExplainPic, 265f, 118f)
        tv_unlock_level.text = "Lv.${bean.minLevel}解锁"
        if (currentLevel >= bean.minLevel) {
            //显示解锁状态
            sdv_privilege.isSelected = true
            ImageUtils.loadImage(sdv_privilege, bean.icon, 24f, 24f)
        } else {
            //显示未解锁状态
            sdv_privilege.isSelected = false
            ImageUtils.loadImage(sdv_privilege, bean.grayIcon, 24f, 24f)
        }
        tv_attention.text = bean.attentionContent
        tv_certain.onClickNew {
            dismiss()
        }
    }


    override fun onStart() {
        super.onStart()
        setDialogSize(Gravity.CENTER, 35, 379)
    }


    override fun needEnterAnimation() = false
}