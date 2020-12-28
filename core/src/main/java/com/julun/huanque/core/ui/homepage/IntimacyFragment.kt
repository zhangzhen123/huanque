package com.julun.huanque.core.ui.homepage

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.launcher.ARouter
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.bean.beans.CloseConfidantBean
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.constant.IntentParamKey
import com.julun.huanque.common.constant.MainPageIndexConst
import com.julun.huanque.common.constant.ParamConstant
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.suger.dp2px
import com.julun.huanque.common.suger.loadImage
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.utils.ScreenUtils
import com.julun.huanque.common.utils.SessionUtils
import com.julun.huanque.core.R
import com.julun.huanque.core.adapter.IntimacyListAdapter
import com.julun.huanque.core.viewmodel.HomePageViewModel
import kotlinx.android.synthetic.main.fragment_intimacy.*
import org.jetbrains.anko.imageResource

/**
 *@创建者   dong
 *@创建时间 2020/11/5 19:35
 *@描述 本周亲密度弹窗
 */
class IntimacyFragment : BaseDialogFragment() {
    //主页ViewModel
    private val mHomePageViewModel: HomePageViewModel by activityViewModels()
    private val mAdapter = IntimacyListAdapter()
    private val mRuleFragment: IntimacyRuleFragment by lazy { IntimacyRuleFragment() }
    override fun getLayoutId() = R.layout.fragment_intimacy

    override fun configDialog() {
        setDialogSize(Gravity.BOTTOM,  ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
    override fun initViews() {

        initRecyclerView()
        tv_jump.onClickNew {
            if (mHomePageViewModel.mineHomePage) {
                //我的主页
                val closeList = mHomePageViewModel.closeListData.value ?: return@onClickNew
                if (closeList.size == 1) {
                    //只有一个用户
                    val user = closeList[0]
                    val bundle = Bundle()
                    bundle.putLong(ParamConstant.TARGET_USER_ID, user.userId)
                    bundle.putString(ParamConstant.NICKNAME, user.nickname)
                    bundle.putBoolean(ParamConstant.FROM, false)
                    bundle.putString(ParamConstant.HeaderPic, user.headPic)
                    ARouter.getInstance().build(ARouterConstant.PRIVATE_CONVERSATION_ACTIVITY).with(bundle).navigation()
                } else if (closeList.size > 1) {
                    //有多个用户上榜,跳转消息列表
                    ARouter.getInstance().build(ARouterConstant.MAIN_ACTIVITY)
                        .withInt(IntentParamKey.TARGET_INDEX.name, MainPageIndexConst.MESSAGE_FRAGMENT_INDEX)
                        .navigation()
                }
            } else {
                //他人主页
                val bundle = Bundle()
                val homeBean = mHomePageViewModel.homeInfoBean.value
                bundle.putLong(ParamConstant.TARGET_USER_ID, mHomePageViewModel.targetUserId)
                bundle.putString(ParamConstant.NICKNAME, homeBean?.nickname ?: "")
                bundle.putBoolean(ParamConstant.FROM, false)
                bundle.putString(ParamConstant.HeaderPic, homeBean?.headPic)
                ARouter.getInstance().build(ARouterConstant.PRIVATE_CONVERSATION_ACTIVITY).with(bundle).navigation()
            }

        }

        iv_help.onClickNew { mRuleFragment.show(childFragmentManager, "IntimacyRuleFragment") }
    }

    private fun initRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = mAdapter
        //显示空页面
        val emptyText = LayoutInflater.from(context).inflate(R.layout.tv_intimacy_empty, null)
        emptyText.layoutParams = ViewGroup.LayoutParams(ScreenUtils.getScreenWidth(), dp2px(198))
        mAdapter.setEmptyView(emptyText)
    }

    override fun onStart() {
        super.onStart()
        initViewModel()
    }


    private fun initViewModel() {
        mHomePageViewModel.closeListData.observe(this, Observer {
            if (it != null) {
                //显示亲密榜数据
                showList(it)
            }
        })
    }

    /**
     * 显示榜单数据
     */
    private fun showList(list: List<CloseConfidantBean>) {
        if (list.isNotEmpty()) {
            //显示头部视图
            val firstUser = list[0]
            sdv_header.loadImage(StringHelper.getOssImgUrl(firstUser.headPic), 90f, 90f)
            sdv_header_border.imageResource = R.mipmap.pic_intim_big
            tv_intim_name.text = firstUser.nickname
            tv_intim_num.text = "亲密度${StringHelper.formatNumber(firstUser.score)}"
            if (mHomePageViewModel.mineHomePage) {
                //我的主页
                val userCount = list.size
                if (userCount > 0) {
                    tv_content.text = "恭喜你获得亲密的人，继续保持哦"
                }
                tv_jump.text = "保持亲密"
            } else {
                //他人主页
                if (firstUser.userId != SessionUtils.getUserId()) {
                    tv_content.text = "取代榜一，成为Ta的亲密知己"
                    tv_jump.text = "取代榜一"
                } else {
                    tv_content.text = "恭喜你成为亲密知己，继续保持哦"
                    tv_jump.text = "保持亲密"
                }
            }

            if (list.size > 1) {
                val listData = list.subList(1, list.size)
                mAdapter.setList(listData)
            }
        } else {
            //显示缺省状态
            sdv_header_border.imageResource = R.mipmap.pic_no_intim_big
            tv_content.text = "取代榜一，成为Ta的亲密知己"
            tv_jump.text = "取代榜一"
        }
    }

}