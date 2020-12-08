package com.julun.huanque.core.ui.dynamic

import android.app.Activity
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.util.SparseArray
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.Observer
import com.alibaba.android.arouter.facade.annotation.Route
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import com.julun.huanque.common.base.BaseVMActivity
import com.julun.huanque.common.base.dialog.MyAlertDialog
import com.julun.huanque.common.basic.NetState
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.bean.beans.CircleGroup
import com.julun.huanque.common.bean.beans.DynamicGroup
import com.julun.huanque.common.bean.beans.SquareTab
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.constant.IntentParamKey
import com.julun.huanque.common.constant.PublicStateCode
import com.julun.huanque.common.constant.StatisticCode
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.widgets.indicator.ScaleTransitionPagerTitleView
import com.julun.huanque.core.R
import com.julun.huanque.core.ui.main.dynamic_square.DynamicTabFragment
import com.julun.huanque.core.ui.publish_dynamic.PublishStateActivity
import com.julun.huanque.core.viewmodel.AttentionCircleViewModel
import kotlinx.android.synthetic.main.activity_circle_dynamic.*
import net.lucode.hackware.magicindicator.ViewPagerHelper
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator
import org.jetbrains.anko.startActivity

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/11/24 14:26
 *
 *@Description: 圈子动态
 *
 */
@Route(path = ARouterConstant.CIRCLE_DYNAMIC_ACTIVITY)
class CircleDynamicActivity : BaseVMActivity<CircleDynamicViewModel>() {


    companion object {
        fun start(act: Activity, groupId: Long) {
            act.startActivity<CircleDynamicActivity>(IntentParamKey.ID.name to groupId)
        }
    }

    private var state: CollapsingToolbarLayoutState? = null

    private enum class CollapsingToolbarLayoutState {
        EXPANDED,
        COLLAPSED,
        INTERNEDIATE
    }

    private val attentionCircleViewModel: AttentionCircleViewModel by viewModels()
    private lateinit var mCommonNavigator: CommonNavigator
    private var mFragmentList = SparseArray<Fragment>()
    private val mTabTitles = arrayListOf<SquareTab>()

    private var currentGroupId: Long? = null
    override fun getLayoutId(): Int {
        return R.layout.activity_circle_dynamic
    }

    private val viewModel: CircleDynamicViewModel by viewModels()


    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        currentGroupId = intent.getLongExtra(IntentParamKey.ID.name, 0L)
        //
        logger.info("initViews =$currentGroupId")
        //设置头部边距
        initViewModel()
        initViewPager()
        initMagicIndicator()

        publish_dynamic.onClickNew {
            currentGroup ?: return@onClickNew
            reportClick(StatisticCode.PubPost + StatisticCode.Group)
            this.startActivity<PublishStateActivity>(PublicStateCode.CIRCLE_DATA to CircleGroup().apply {
                this.groupId = groupId
                this.groupName = currentGroup!!.groupName
            })
        }
        iv_back.onClickNew {
            finish()
        }
//        view_pager.post {
//            val params: ViewGroup.LayoutParams = view_pager.layoutParams
//            params.height = ScreenUtils.getScreenHeight() - ScreenUtils.statusHeight
//            view_pager.layoutParams = params
//        }

        btn_action.onClickNew {
            currentGroup ?: return@onClickNew
            if (currentGroup!!.join) {
                MyAlertDialog(this).showAlertWithOKAndCancel(
                    "退出圈子将错过精彩内容和动态推送",
                    MyAlertDialog.MyDialogCallback(onCancel = {
                        attentionCircleViewModel.groupQuit(currentGroupId ?: return@MyDialogCallback)
                    }), "提示", "继续关注", "残忍退出"
                )

            } else {
                attentionCircleViewModel.groupJoin(currentGroupId ?: return@onClickNew)
            }
        }
        btn_action_tb.onClickNew {
            currentGroup ?: return@onClickNew
            if (currentGroup!!.join) {
                attentionCircleViewModel.groupQuit(currentGroupId ?: return@onClickNew)
            } else {
                attentionCircleViewModel.groupJoin(currentGroupId ?: return@onClickNew)
            }
        }
        appBarLayout.addOnOffsetChangedListener(OnOffsetChangedListener { appBarLayout, verticalOffset ->
            logger.info("verticalOffset=$verticalOffset")
            if (verticalOffset == 0) {
                if (state != CollapsingToolbarLayoutState.EXPANDED) {
                    state = CollapsingToolbarLayoutState.EXPANDED//修改状态标记为展开
                    btn_action_tb.hide()
                    tv_title.hide()
                }
            } else if (Math.abs(verticalOffset) >= appBarLayout.totalScrollRange) {
                if (state != CollapsingToolbarLayoutState.COLLAPSED) {
                    tv_title.show()
                    btn_action_tb.show()
                }
            } else {
                btn_action_tb.hide()
                tv_title.hide()
            }
        })

    }

    private fun initViewPager() {
        view_pager.adapter = mPagerAdapter
        //配置预加载页数
//        view_pager.offscreenPageLimit = 2
        view_pager.currentItem = 0

    }

    private fun initViewModel() {
        viewModel.tabList.observe(this, Observer {
            if (it.state == NetStateType.SUCCESS) {
                mTabTitles.clear()
                mTabTitles.addAll(it.requireT())
                refreshTabList()
            }

        })
        viewModel.dynamicDetailInfo.observe(this, Observer {
            if (it.isSuccess()) {
                renderHeaderData(it.requireT())
            }

        })
        attentionCircleViewModel.joinedGroupIdData.observe(this, Observer {
            if (it != null) {
                currentGroup?.join = true
//                btn_action.isActivated = false
//                btn_action.text = "退出"
//                btn_action_tb.isActivated = false
//                btn_action_tb.text = "退出"
                setJoinBtn(true)
            }
        })
        attentionCircleViewModel.quitGroupIdData.observe(this, Observer {
            if (it != null) {
                currentGroup?.join = false
//                btn_action.isActivated = true
//                btn_action.text = "加入"
//                btn_action_tb.isActivated = true
//                btn_action_tb.text = "加入"
                setJoinBtn(false)
            }
        })

        viewModel.queryInfo()
        viewModel.queryGroupPostDetail(currentGroupId)
    }

    private var currentGroup: DynamicGroup? = null
    private fun renderHeaderData(group: DynamicGroup) {
        currentGroup = group
        headImage.loadImage(group.groupPic, 80f, 80f)
        tvCircleName.text = group.groupName
        tv_title.text = group.groupName
        tvDynamicNum.text = "${StringHelper.formatNum(group.postNum)}  动态"
        tvDes.text = group.groupDesc
        setJoinBtn(group.join)
//        if (group.join) {
//            btn_action.isActivated = false
//            btn_action.text = "退出"
//        } else {
//            btn_action.isActivated = true
//            btn_action.text = "加入"
//        }
    }

    private fun setJoinBtn(join: Boolean) {
        if (join) {
            btn_action.isActivated = false
            btn_action.text = "退出"
            btn_action_tb.isActivated = false
            btn_action_tb.text = "退出"
        } else {
            btn_action.isActivated = true
            btn_action.text = "加入"
            btn_action_tb.isActivated = true
            btn_action_tb.text = "加入"
        }
    }

    /**
     * 初始化历史记录指示器
     */
    private fun initMagicIndicator() {
        mCommonNavigator = CommonNavigator(this)
        mCommonNavigator.scrollPivotX = 0.65f
//        mCommonNavigator.isAdjustMode = true
        mCommonNavigator.adapter = object : CommonNavigatorAdapter() {
            override fun getCount(): Int {
                return mTabTitles.size
            }

            override fun getTitleView(context: Context, index: Int): IPagerTitleView {
                logger.info("getTitleView：$index")

                val simplePagerTitleView: ScaleTransitionPagerTitleView = ScaleTransitionPagerTitleView(context)
                simplePagerTitleView.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                simplePagerTitleView.minScale = 0.583f
                simplePagerTitleView.text = mTabTitles[index].typeName
                simplePagerTitleView.textSize = 24f
                simplePagerTitleView.normalColor = ContextCompat.getColor(context, R.color.black_666)
                simplePagerTitleView.selectedColor = ContextCompat.getColor(context, R.color.black_333)
                simplePagerTitleView.setOnClickListener { view_pager.currentItem = index }
                if (view_pager.currentItem == index) {
                    simplePagerTitleView.setTextColor(ContextCompat.getColor(context, R.color.black_333))
                    simplePagerTitleView.scaleX = 1.0f
                    simplePagerTitleView.scaleY = 1.0f
                } else {
                    simplePagerTitleView.setTextColor(ContextCompat.getColor(context, R.color.black_666))
                    simplePagerTitleView.scaleX = 0.583f
                    simplePagerTitleView.scaleY = 0.583f
                }
                return simplePagerTitleView
            }

            override fun getIndicator(context: Context): IPagerIndicator? {
//                if (mTabTitles.size <= 1) {
//                    return null
//                }
                val indicator = LinePagerIndicator(context)
                indicator.mode = LinePagerIndicator.MODE_EXACTLY
                indicator.lineHeight = dp2pxf(4)
                indicator.lineWidth = dp2pxf(13)
                indicator.roundRadius = dp2pxf(2)
                indicator.startInterpolator = AccelerateInterpolator()
                indicator.endInterpolator = DecelerateInterpolator(2.0f)
                indicator.yOffset = dp2pxf(4)
                indicator.setColors(context.resources.getColor(R.color.primary_color))
                return indicator
            }
        }
        magic_indicator.navigator = mCommonNavigator
        ViewPagerHelper.bind(magic_indicator, view_pager)
    }

    /**
     * 刷新tab数据 并且切换到指定tab
     */
    private fun refreshTabList(currentTab: String? = null) {
        if (mTabTitles.isNotEmpty()) {
//            if (mTabTitles.size > 4) {
//                mCommonNavigator.isAdjustMode = false
//            }
            mPagerAdapter.notifyDataSetChanged()
            view_pager.offscreenPageLimit = mTabTitles.size
            mCommonNavigator.notifyDataSetChanged()
//            if(mTabTitles.size>1){
//                magic_indicator.show()
//                mCommonNavigator.notifyDataSetChanged()
//            }else{
//                magic_indicator.hide()
//            }


            if (!currentTab.isNullOrEmpty()) {
                switchToTab(currentTab)
            }
        } else {
//            showErrorView(false)
        }
    }

    /**
     *  手动切换到指定tab位置
     */
    private fun switchToTab(currentTabCode: String) {
        var position = 0
        run breaking@{
            mTabTitles.forEachIndexed { index, newProgramTab ->
                if (currentTabCode == newProgramTab.typeCode) {
                    position = index
                    return@breaking
                }
            }

        }
        val count = mPagerAdapter.count
        if (count >= position) {
            view_pager.currentItem = position
        }
    }

    /**
     * 滑动到顶部
     */
    fun scrollToTop() {
        val tempIndex = view_pager.currentItem
        val tempFragment: androidx.fragment.app.Fragment? = mPagerAdapter.getItem(tempIndex)
        tempFragment?.let {
            if (it is DynamicTabFragment) {
                it.scrollToTopAndRefresh()
            }
        }
    }


    private val mPagerAdapter: FragmentPagerAdapter by lazy {
        object : FragmentPagerAdapter(supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            override fun getItem(position: Int): Fragment {
                return mFragmentList[position] ?: getFragment(position)
            }

            override fun getCount(): Int {
                return mTabTitles.size
            }

            private fun getFragment(position: Int): Fragment {
                val fragment: Fragment = DynamicTabFragment.newInstance(mTabTitles[position], currentGroupId)
                mFragmentList.put(position, fragment)
                return fragment
            }

            override fun getPageTitle(position: Int): CharSequence {
                return mTabTitles[position].typeName
            }
        }
    }

    override fun showLoadState(state: NetState) {
    }

}