package com.julun.huanque.core.ui.main.home

import android.content.Context
import android.os.Bundle
import android.util.SparseArray
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.util.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.alibaba.android.arouter.launcher.ARouter
import com.julun.huanque.common.base.BaseFragment
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.common.suger.dp2pxf
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.core.R
import com.julun.huanque.core.ui.main.makefriend.MakeFriendsFragment
import com.julun.rnlib.RnManager
import com.luck.picture.lib.tools.ScreenUtils
import com.luck.picture.lib.tools.StatusBarUtil
import kotlinx.android.synthetic.main.fragment_main.*
import net.lucode.hackware.magicindicator.ViewPagerHelper
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.CommonPagerTitleView
import org.jetbrains.anko.topPadding

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/6/30 19:51
 *
 *@Description: HomeFragment 首页 包含交友[MakeFriendsFragment]和推荐
 *
 */
class HomeFragment : BaseFragment() {

    companion object {
        fun newInstance() = HomeFragment()
    }

    private lateinit var mCommonNavigator: CommonNavigator
    private var mFragmentList = SparseArray<Fragment>()
    private val mTabTitles = arrayListOf<String>()

    override fun getLayoutId(): Int {
        return R.layout.fragment_main
    }

    private val viewModel: HomeViewModel by activityViewModels()

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        //
        logger.info("initViews")
        //设置头部边距
        home_container.topPadding = StatusBarUtil.getStatusBarHeight(requireContext())
        initViewModel()
        initViewPager()
        initMagicIndicator()
        home_container.post {
            RnManager.createReactInstanceManager(CommonInit.getInstance().getApp())
        }
    }

    private fun initViewPager() {
        //注 这里只是用到不可滑动功能 没有使用关联viewpager
        view_pager.adapter = mPagerAdapter
        //配置预加载页数
//        view_pager.offscreenPageLimit = 2
        view_pager.currentItem = 0

    }

    private fun initViewModel() {
        viewModel.tabList.observe(viewLifecycleOwner, Observer {
            if (it.state == NetStateType.SUCCESS) {
                mTabTitles.clear()
                mTabTitles.addAll(it.getT())
                refreshTabList("交友")
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
                val simplePagerTitleView = CommonPagerTitleView(context)
                simplePagerTitleView.setContentView(R.layout.view_home_tab_title)

//                val container = simplePagerTitleView.findViewById<RelativeLayout>(R.id.tab_container)
                val tabTitle = simplePagerTitleView.findViewById<TextView>(R.id.tvTabTitle)
//                val dot = simplePagerTitleView.findViewById<ImageView>(R.id.tab_dot)
                simplePagerTitleView.onPagerTitleChangeListener = object : CommonPagerTitleView.OnPagerTitleChangeListener {
                    override fun onDeselected(index: Int, totalCount: Int) {
//                            tabTitle.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
                        logger.info("onDeselected:$index")
                        tabTitle.setTextColor(ContextCompat.getColor(context, R.color.black_666))
                        tabTitle.textSize = 14f
                    }

                    override fun onSelected(index: Int, totalCount: Int) {
                        logger.info("onSelected:$index")
                        tabTitle.setTextColor(ContextCompat.getColor(context, R.color.black_333))
                        tabTitle.textSize = 24f

                    }

                    override fun onLeave(index: Int, totalCount: Int, leavePercent: Float, leftToRight: Boolean) {
                    }

                    override fun onEnter(index: Int, totalCount: Int, enterPercent: Float, leftToRight: Boolean) {
                    }
                }
                tabTitle.text = mTabTitles[index]
                simplePagerTitleView.setOnClickListener { view_pager.currentItem = index }
                if (view_pager.currentItem == index) {
                    tabTitle.setTextColor(ContextCompat.getColor(context, R.color.black_333))
                    tabTitle.textSize = 24f
                } else {
                    tabTitle.setTextColor(ContextCompat.getColor(context, R.color.black_666))
                    tabTitle.textSize = 14f
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

    private fun refreshTabList(currentTab: String) {
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


            if (currentTab.isNotEmpty()) {
                switchToTab(currentTab)
            }
        } else {
//            showErrorView(false)
        }
    }

    /**
     *  手动切换到指定tab位置
     */
    private fun switchToTab(currentTab: String) {
        var position = 0
        run breaking@{
            mTabTitles.forEachIndexed { index, newProgramTab ->
                if (currentTab == newProgramTab) {
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

    override fun onHiddenChanged(hidden: Boolean) {
        logger.info("onHiddenChanged=$hidden")
        super.onHiddenChanged(hidden)
        if (hidden) {
            mFragmentList.forEach { key, value ->
                if (value is MakeFriendsFragment) {
                    value.hideTodo()
                }
            }
        }
    }

    private val mPagerAdapter: FragmentPagerAdapter by lazy {
        object : FragmentPagerAdapter(parentFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            override fun getItem(position: Int): Fragment {
                return mFragmentList[position] ?: getFragment(position)
            }

            override fun getCount(): Int {
                return mTabTitles.size
            }

            private fun getFragment(position: Int): Fragment {
                val fragment: Fragment = when (mTabTitles.getOrNull(position)) {
                    "交友" -> MakeFriendsFragment.newInstance()
                    "推荐" -> MakeFriendsFragment.newInstance()
                    else -> MakeFriendsFragment.newInstance()
                }
                mFragmentList.put(position, fragment)
                return fragment
            }

            override fun getPageTitle(position: Int): CharSequence {
                return mTabTitles[position]
            }
        }
    }

}