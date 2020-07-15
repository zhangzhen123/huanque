package com.julun.huanque.ui.main

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.launcher.ARouter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.R
import com.julun.huanque.common.base.BaseVMFragment
import com.julun.huanque.common.basic.NetState
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.bean.beans.UserDataTab
import com.julun.huanque.common.bean.beans.UserDetailInfo
import com.julun.huanque.common.bean.beans.UserTool
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.constant.RealNameConstants
import com.julun.huanque.common.constant.Sex
import com.julun.huanque.common.interfaces.routerservice.IRealNameService
import com.julun.huanque.common.interfaces.routerservice.RealNameCallback
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.StatusBarUtil
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.core.ui.recharge.RechargeCenterActivity
import com.julun.huanque.viewmodel.MineViewModel
import com.julun.rnlib.RNPageActivity
import com.julun.rnlib.RnConstant
import kotlinx.android.synthetic.main.fragment_mine.*
import org.jetbrains.anko.startActivity
import java.math.RoundingMode

/**
 *@创建者   dong
 *@创建时间 2020/6/29 19:21
 *@描述 我的
 */
class MineFragment : BaseVMFragment<MineViewModel>() {

    companion object {
        fun newInstance() = MineFragment()
    }

    private val mIRealNameService:IRealNameService by lazy{
        ARouter.getInstance().build(ARouterConstant.REALNAME_SERVICE).navigation() as IRealNameService
    }
    override fun getLayoutId() = R.layout.fragment_mine

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        val lt = llTitleRootView.layoutParams as ConstraintLayout.LayoutParams
        lt.topMargin = StatusBarUtil.getStatusBarHeight(requireContext())
        rv_tabs_info.adapter = infoTabAdapter
        rv_tabs_info.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)

        rvUserTools.adapter = toolsAdapter
        rvUserTools.layoutManager = GridLayoutManager(context, 4)
        tvQueBi.setTFDinAltB()
        tvLingQian.setTFDinAltB()
        initViewModel()
    }

    private fun initViewModel() {
        mViewModel.userInfo.observe(this, Observer {
            refreshView.isRefreshing = false
            if (it.state == NetStateType.SUCCESS) {
                loadData(it.getT())
            }
        })
    }

    private fun loadData(info: UserDetailInfo) {
        headImage.loadImage(info.userBasic.headPic, 60f, 60f)
        tvNickName.text = info.userBasic.nickname
        tvUserId.text = "欢鹊ID:${info.userBasic.userId}"
        tvQueBi.text = "${info.userBasic.beans}"
        tvLingQian.text = "${info.userBasic.cash.setScale(2, RoundingMode.HALF_UP)}"
        if (info.userBasic.headRealPeople) {
            tvCertification.hide()
            ivReal.showContextMenu()
        } else {
            tvCertification.show()
            tvCertification.onClickNew {
                mIRealNameService.startRealHead(requireActivity(),object :RealNameCallback{
                    override fun onCallback(status: String, des: String) {
                        if(status== RealNameConstants.TYPE_SUCCESS ){
                            mViewModel.queryInfo(QueryType.REFRESH)
                        }else{
                            ToastUtils.show("认证失败，请稍后重试")
                        }
                    }
                } )

            }
            ivReal.hide()
        }
//        sd_wealth.loadImage(info.userBasic.royalLevel)
        if(info.userBasic.sex== Sex.FEMALE){
            ivInviteFriend.hide()
        }else{
            ivInviteFriend.show()
        }

        infoTabAdapter.setNewInstance(info.userDataTabList)
        toolsAdapter.setNewInstance(info.tools)
    }

    override fun initEvents(rootView: View) {
        clHeadRoot.onClickNew {
            RNPageActivity.start(requireActivity(), RnConstant.MINE_HOMEPAGE)
        }

        refreshView.setOnRefreshListener {
            mViewModel.queryInfo(QueryType.REFRESH)
        }
        toolsAdapter.setOnItemClickListener { _, _, position ->
            //todo
        }
        ivSetting.onClickNew {
        }
        rlQueBi.onClickNew {
            requireActivity().startActivity<RechargeCenterActivity>()
        }
    }

    override fun showLoadState(state: NetState) {
        when (state.state) {
            NetStateType.SUCCESS -> {
                state_pager_view.showSuccess()
            }
            NetStateType.LOADING -> {
                state_pager_view.showLoading()
            }
            NetStateType.ERROR, NetStateType.NETWORK_ERROR -> {
                state_pager_view.showError(showBtn = true, btnClick = View.OnClickListener {
                    mViewModel.queryInfo()
                })
            }

        }

    }

    override fun lazyLoadData() {
        mViewModel.queryInfo()
    }

    private val infoTabAdapter: BaseQuickAdapter<UserDataTab, BaseViewHolder> by lazy {
        object : BaseQuickAdapter<UserDataTab, BaseViewHolder>(R.layout.item_tab_user_info) {
            override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
                val holder=super.onCreateDefViewHolder(parent, viewType)
                val tv=holder.getViewOrNull<TextView>(R.id.tvCount)
                tv?.setTFDinCdc2()
                return holder
            }
            override fun convert(holder: BaseViewHolder, item: UserDataTab) {
                val tvCount = holder.getView<TextView>(R.id.tvCount)
                tvCount.text = "${item.count}"
                holder.setText(R.id.tvTitle, item.userTabName)

                if (item.tagCount == 0) {
                    holder.setGone(R.id.tv_tag, true).setText(R.id.tv_tag, "${item.tagCount}")
                } else {
                    holder.setGone(R.id.tv_tag, false)
                }
            }
        }
    }
    private val toolsAdapter: BaseQuickAdapter<UserTool, BaseViewHolder> by lazy {
        object : BaseQuickAdapter<UserTool, BaseViewHolder>(R.layout.item_user_tools) {

            override fun convert(holder: BaseViewHolder, item: UserTool) {
                val sdvTool = holder.getView<SimpleDraweeView>(R.id.sdv_tool)
                sdvTool.loadImage(item.icon, 34f, 34f)
                holder.setText(R.id.tvTitle, item.name)

            }
        }
    }
}