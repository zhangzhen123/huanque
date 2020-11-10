package com.julun.huanque.core.ui.main.makefriend

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.base.BaseFragment
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.bean.beans.FamousListMultiBean
import com.julun.huanque.common.bean.beans.SingleFamousMonth
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.suger.dp2px
import com.julun.huanque.common.suger.loadImage
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.utils.GlobalUtils
import com.julun.huanque.common.utils.ScreenUtils
import com.julun.huanque.common.utils.SessionUtils
import com.julun.huanque.common.utils.StatusBarUtil
import com.julun.huanque.core.R
import com.julun.huanque.core.adapter.FlowerFamousMonthAdapter
import com.julun.huanque.core.ui.homepage.HomePageActivity
import com.julun.huanque.core.viewmodel.PlumFlowerViewModel
import com.julun.rnlib.RNPageActivity
import com.julun.rnlib.RnConstant
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import kotlinx.android.synthetic.main.fragment_famous_list.*
import kotlinx.android.synthetic.main.fragment_famous_list.recyclerView
import kotlinx.android.synthetic.main.fragment_famous_list.statePage
import org.jetbrains.anko.textColor
import org.jetbrains.anko.textSizeDimen
import java.util.concurrent.TimeUnit

/**
 *@创建者   dong
 *@创建时间 2020/8/24 16:13
 *@描述 名人榜
 */
class FamousListFragment : BaseFragment() {
    //花魁ViewModel
    private val mViewModel: PlumFlowerViewModel by viewModels<PlumFlowerViewModel>()

    private val mAdapter = FlowerFamousMonthAdapter()

    override fun getLayoutId() = R.layout.fragment_famous_list

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        initViewModel()
        initRecyclerView()
        statePage.showLoading()
        mViewModel.getFamousList()
    }


    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {

        mViewModel.loadState.observe(this, Observer {
            swipeLayout.isRefreshing = false
            when (it.state) {
                NetStateType.SUCCESS -> {
                }
                NetStateType.ERROR -> {
                    statePage.showError()
                }
                NetStateType.NETWORK_ERROR -> {
                    statePage.showError(btnClick = View.OnClickListener {
                        statePage.showLoading()
                        mViewModel.getFamousList()
                    })
                }
            }
        })

        mViewModel.famousListData.observe(this, Observer {
            if (it != null) {
                if (it.monthList.isNotEmpty() || it.lastWeekTop.userId != 0L) {
                    statePage.showSuccess()
                    val data = mutableListOf<FamousListMultiBean>()
//                    data.add(FamousListMultiBean(FamousListMultiBean.HeaderView, it.inRank))
                    it.monthList.forEach { sfm ->
                        if (sfm.userList.isNotEmpty()) {
                            data.add(FamousListMultiBean(FamousListMultiBean.Content, sfm))
                        }
                    }
                    mAdapter.setList(data)
                    val weekInfo = it.lastWeekTop
                    sdv_header.loadImage(StringHelper.getOssImgUrl(weekInfo.headPic), 138f, 138f)
                    tv_nickname.text = weekInfo.nickname
                } else {
                    statePage.showEmpty(emptyTxt = "快去名人榜争得一席之地吧")
                }

            }
        })
    }

    override fun initEvents(rootView: View) {
        super.initEvents(rootView)
        swipeLayout.setOnRefreshListener {
            mViewModel.getFamousList()
        }
        sdv_header.onClickNew {
            val weekBean = mViewModel.famousListData.value?.lastWeekTop ?: return@onClickNew
            if (weekBean.userId == SessionUtils.getUserId()) {
                //跳转我的主页
                RNPageActivity.start(requireActivity(), RnConstant.MINE_HOMEPAGE)
            } else {
                //跳转他人主页
                HomePageActivity.newInstance(requireActivity(), weekBean.userId)
            }
        }
    }

    /**
     * 初始化ViewModel
     */
    private fun initRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = mAdapter

        //显示空页面
        val emptyText = LayoutInflater.from(context).inflate(R.layout.tv_famous_list_empty, null)
        emptyText.layoutParams = ViewGroup.LayoutParams(ScreenUtils.getScreenWidth(), dp2px(200))
        mAdapter.setEmptyView(emptyText)


        val params = con_top.layoutParams
        val height = ScreenUtils.getScreenWidth() * 978 / 1125 - dp2px(44) - StatusBarUtil.getStatusBarHeight(requireContext())
        params.height = height
        con_top.layoutParams = params

//        val recyclerParams = recyclerView.layoutParams as? ConstraintLayout.LayoutParams
//        recyclerParams?.topMargin = height
//        recyclerView.layoutParams = recyclerParams

//        mHeaderView = LayoutInflater.from(requireContext()).inflate(R.layout.view_daylist_header_famous, null)
//        mHeaderView?.let { view ->
//            mAdapter.addHeaderView(view)
//            val params = view.layoutParams
//            params.height = ScreenUtils.getScreenWidth() * 978 / 1125 - dp2px(44) - StatusBarUtil.getStatusBarHeight(requireContext())
//            view.layoutParams = params
//        }

    }

    override fun onStop() {
        super.onStop()
        recyclerView.handler.removeCallbacks(null)
    }
}