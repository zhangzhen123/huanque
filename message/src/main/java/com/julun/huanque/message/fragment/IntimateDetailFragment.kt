package com.julun.huanque.message.fragment

import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.launcher.ARouter
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.bean.beans.ConversationBasicBean
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.constant.ParamConstant
import com.julun.huanque.common.helper.DensityHelper
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.ui.web.WebActivity
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.common.utils.IntimateUtil
import com.julun.huanque.message.R
import com.julun.huanque.message.adapter.PrivilegeAdapter
import com.julun.huanque.message.viewmodel.IntimateDetailViewModel
import com.julun.rnlib.RNPageActivity
import com.julun.rnlib.RnConstant
import kotlinx.android.synthetic.main.fragment_meet_detail.*

/**
 *@创建者   dong
 *@创建时间 2020/7/9 11:28
 *@描述 欢遇详情弹窗
 */
class IntimateDetailFragment : BaseDialogFragment() {
    companion object {
        fun newInstance() = IntimateDetailFragment()
    }

    //当前亲密度等级
    private var mCurrentIntimateLevel = 0

    private val mViewModel: IntimateDetailViewModel by activityViewModels()
    private val mAdapter = PrivilegeAdapter()

    override fun getLayoutId() = R.layout.fragment_meet_detail

    override fun initViews() {
        initRecyclerView()
        iv_rule.onClickNew {
            RNPageActivity.start(requireActivity(),RnConstant.INTIMATE_LEVEL_PAGE,Bundle().apply { putLong("friendId",mViewModel.friendId) })
        }

        sdv_other.onClickNew {
            //打开他们主页
            val bundle = Bundle().apply {
                putLong(ParamConstant.UserId, mViewModel.basicBean?.value?.friendUser?.userId ?: return@onClickNew)
            }
            ARouter.getInstance().build(ARouterConstant.HOME_PAGE_ACTIVITY).with(bundle).navigation()
        }

        sdv_mine.onClickNew {
            RNPageActivity.start(
                requireActivity(),
                RnConstant.MINE_HOMEPAGE
            )
        }
    }

    /**
     * 初始化ViewModle
     */
    private fun initRecyclerView() {
        recycler_privilege.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        recycler_privilege.adapter = mAdapter
        mAdapter.setOnItemClickListener { adapter, view, position ->
            val bean = mAdapter.getItem(position)
            SingleIntimateprivilegeFragment.newInstance(bean, mCurrentIntimateLevel).show(childFragmentManager, "SingleIntimateprivilegeFragment")
        }
    }

    override fun onStart() {
        super.onStart()
        setWindowConfig()
        initViewModel()
    }

    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {
        mViewModel.basicBean.observe(this, Observer {
            if (it != null) {
                showViewByData(it)
            }
        })
    }

    private fun showViewByData(bean: ConversationBasicBean) {
        //显示对方头像和昵称
        val otherInfo = bean.friendUser
        ImageUtils.loadImage(sdv_other, otherInfo.headPic)
        tv_other_nickname.text = otherInfo.nickname
        //显示本人头像和昵称
        val mineInfo = bean.usr
        ImageUtils.loadImage(sdv_mine, mineInfo.headPic)
        tv_mine_nickname.text = mineInfo.nickname

        //显示亲密度相关
        val intimateBean = bean.intimate
        mCurrentIntimateLevel = intimateBean.intimateLevel
        mAdapter.currentLevel = mCurrentIntimateLevel

        tv_meet_level.text = "Lv.${mCurrentIntimateLevel}"

        val progress = if (mCurrentIntimateLevel == intimateBean.nextIntimateLevel) {
            //满级
            tv_meet_attention.text = "亲密度${intimateBean.intimateNum}"
            100
        } else {
            tv_meet_attention.text = "距离下一级差${intimateBean.nextIntimateNum - intimateBean.intimateNum}亲密度"
            if (intimateBean.nextIntimateNum == 0) {
                0
            } else {
                intimateBean.intimateNum * 100 / intimateBean.nextIntimateNum
            }
        }
        progress_meet.progress = progress

        val privilegeList = IntimateUtil.intimatePrivilegeList
        var enablePrivilege = 0
        privilegeList.forEach {
            if (mCurrentIntimateLevel >= it.minLevel) {
                enablePrivilege++
            }
            if (mCurrentIntimateLevel == it.minLevel) {
                tv_content.text = it.detailContent
            }
        }

        tv_intimate_privilege.text = "亲密特权($enablePrivilege/${privilegeList.size})"
        mAdapter.setList(privilegeList)

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

        //不需要半透明遮罩层
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
//        window.setWindowAnimations(R.style.dialog_bottom_bottom_style)
    }
}