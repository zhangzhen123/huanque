package com.julun.huanque.core.ui.main.dynamic_square

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.util.SparseArray
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.julun.huanque.common.base.BaseFragment
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.bean.beans.PagerTab
import com.julun.huanque.common.constant.StatisticCode
import com.julun.huanque.common.helper.DensityHelper
import com.julun.huanque.common.suger.dp2pxf
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.reportClick
import com.julun.huanque.common.suger.reportScan
import com.julun.huanque.common.widgets.indicator.ScaleTransitionPagerTitleView
import com.julun.huanque.core.R
import com.julun.huanque.core.ui.publish_dynamic.PublishStateActivity
import com.julun.huanque.core.viewmodel.ScrollStateViewModel
import com.luck.picture.lib.tools.StatusBarUtil
import kotlinx.android.synthetic.main.fragment_dynamic_container.*
import kotlinx.android.synthetic.main.fragment_dynamic_container.magic_indicator
import kotlinx.android.synthetic.main.fragment_dynamic_container.publish_dynamic
import kotlinx.android.synthetic.main.fragment_dynamic_container.view_pager
import net.lucode.hackware.magicindicator.ViewPagerHelper
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.topPadding

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/11/20 17:32
 *
 *@Description: DynamicSquareFragment
 *
 */
class DynamicSquareFragment : BaseFragment() {

    companion object {
        fun newInstance() = DynamicSquareFragment()
    }

    private lateinit var mCommonNavigator: CommonNavigator
    private var mFragmentList = SparseArray<Fragment>()
    private val mTabTitles = arrayListOf<PagerTab>()

    private val mScrollStateViewModel: ScrollStateViewModel by activityViewModels()

    override fun getLayoutId(): Int {
        return R.layout.fragment_dynamic_container
    }

    private val viewModel: SquareViewModel by viewModels()


    override fun initViews(rootView: View, savedInstanceState: Bundle?) {

        //
        logger.info("initViews")
        //设置头部边距
        dynamic_container.topPadding = StatusBarUtil.getStatusBarHeight(requireContext())
        initViewModel()
        initViewPager()
        initMagicIndicator()

        publish_dynamic.onClickNew {
            reportClick(
                eventCode = StatisticCode.PubPost + StatisticCode.Home
            )
            requireActivity().startActivity<PublishStateActivity>()
        }
        startShowTime = System.currentTimeMillis()
    }

    private fun initViewPager() {
        view_pager.adapter = mPagerAdapter
        //配置预加载页数
//        view_pager.offscreenPageLimit = 2
        view_pager.currentItem = 0

    }

    private fun initViewModel() {
        viewModel.tabList.observe(viewLifecycleOwner, Observer {
            if (it.state == NetStateType.SUCCESS) {
                mTabTitles.clear()
                mTabTitles.addAll(it.requireT())
                refreshTabList()
            }

        })

        mScrollStateViewModel.scrollState.observe(this, Observer {
            if (it != null) {
                if (it) {
                    showOrHidePublish(false)
                } else {
                    showOrHidePublish(true)
                }

            }
        })

        viewModel.queryInfo()

    }


    /**
     * 初始化历史记录指示器
     */
    private fun initMagicIndicator() {
        mCommonNavigator = CommonNavigator(context)
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
                simplePagerTitleView.minScale = 0.727f
                simplePagerTitleView.text = mTabTitles[index].typeName
                simplePagerTitleView.textSize = 22f
                simplePagerTitleView.setPadding(DensityHelper.dp2px(8),0, DensityHelper.dp2px(8),0)
                simplePagerTitleView.normalColor = ContextCompat.getColor(context, R.color.black_666)
                simplePagerTitleView.selectedColor = ContextCompat.getColor(context, R.color.black_333)
                simplePagerTitleView.setOnClickListener { view_pager.currentItem = index }
                if (view_pager.currentItem == index) {
                    simplePagerTitleView.setTextColor(ContextCompat.getColor(context, R.color.black_333))
                    simplePagerTitleView.scaleX = 1.0f
                    simplePagerTitleView.scaleY = 1.0f
                } else {
                    simplePagerTitleView.setTextColor(ContextCompat.getColor(context, R.color.black_666))
                    simplePagerTitleView.scaleX = 0.727f
                    simplePagerTitleView.scaleY = 0.727f
                }
                return simplePagerTitleView
            }

            override fun getIndicator(context: Context): IPagerIndicator? {
//                if (mTabTitles.size <= 1) {
//                    return null
//                }
                val indicator = LinePagerIndicator(context)
                indicator.mode = LinePagerIndicator.MODE_EXACTLY
                indicator.lineHeight = dp2pxf(3)
                indicator.lineWidth = dp2pxf(6)
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

    var startShowTime: Long = 0L
    override fun onHiddenChanged(hidden: Boolean) {
        logger.info("onHiddenChanged=$hidden")
        super.onHiddenChanged(hidden)
        val tempIndex = view_pager.currentItem
        val tempFragment: androidx.fragment.app.Fragment? = mPagerAdapter.getItem(tempIndex)
        tempFragment?.let {
            if (it is DynamicTabFragment) {
                it.onParentHiddenChanged(hidden)
            }

        }
        if (!hidden) {
            startShowTime = System.currentTimeMillis()
        } else {
            val current = System.currentTimeMillis()
            val duration = current - startShowTime
            if (duration > 10 * 1000) {
                reportScan(
                    eventCode = StatisticCode.Post,
                    enterTime = startShowTime,
                    leaveTime = current
                )
            }
        }
    }

    //隐藏发布按钮动画
    private var mHidePublishAnimation: ObjectAnimator? = null

    //显示发布按钮动画
    private var mShowPublishAnimation: ObjectAnimator? = null


    /**
     * 显示或者隐藏发布按钮
     * @param show  true 显示发布按钮  false 隐藏发布按钮
     */
    private fun showOrHidePublish(show: Boolean) {
        if (show) {
            if (mShowPublishAnimation?.isRunning == true) {
                //动画正在执行，什么都不操作
                return
            }
            mShowPublishAnimation = ObjectAnimator.ofFloat(publish_dynamic, "translationX", publish_dynamic.translationX, 0f)
                .apply { duration = 100 }
            mHidePublishAnimation?.cancel()
            mShowPublishAnimation?.cancel()
            mShowPublishAnimation?.start()
        } else {
            if (mHidePublishAnimation?.isRunning == true) {
                //动画正在执行，什么都不操作
                return
            }
            mHidePublishAnimation =
                ObjectAnimator.ofFloat(publish_dynamic, "translationX", publish_dynamic.translationX, dp2pxf(95))
                    .apply { duration = 100 }

            mHidePublishAnimation?.cancel()
            mShowPublishAnimation?.cancel()
            mHidePublishAnimation?.start()
        }

    }

    private val mPagerAdapter: FragmentPagerAdapter by lazy {
        object : FragmentPagerAdapter(childFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            override fun getItem(position: Int): Fragment {
                return mFragmentList[position] ?: getFragment(position)
            }

            override fun getCount(): Int {
                return mTabTitles.size
            }

            private fun getFragment(position: Int): Fragment {
                val fragment: Fragment = DynamicTabFragment.newInstance(mTabTitles[position])
                mFragmentList.put(position, fragment)
                return fragment
            }

            override fun getPageTitle(position: Int): CharSequence {
                return mTabTitles[position].typeName
            }
        }
    }

    override fun onViewDestroy() {
        super.onViewDestroy()
        mHidePublishAnimation?.cancel()
        mShowPublishAnimation?.cancel()
    }

}