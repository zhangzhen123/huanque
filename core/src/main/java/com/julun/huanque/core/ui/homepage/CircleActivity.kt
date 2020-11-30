package com.julun.huanque.core.ui.homepage

import android.app.Activity
import android.content.Context
import android.content.Intent
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
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.constant.CircleGroupTabType
import com.julun.huanque.common.constant.CircleGroupType
import com.julun.huanque.common.constant.ParamConstant
import com.julun.huanque.common.suger.dp2pxf
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.utils.ForceUtils
import com.julun.huanque.common.widgets.indicator.ScaleTransitionPagerTitleView
import com.julun.huanque.core.R
import com.julun.huanque.core.viewmodel.CircleViewModel
import kotlinx.android.synthetic.main.act_circle.*
import kotlinx.android.synthetic.main.act_circle.magic_indicator
import kotlinx.android.synthetic.main.act_circle.view_pager
import net.lucode.hackware.magicindicator.ViewPagerHelper
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator

/**
 *@创建者   dong
 *@创建时间 2020/11/23 17:32
 *@描述 全部圈子页面
 */
class CircleActivity : BaseActivity() {
    companion object {
        fun newInstance(act: Activity, defaultTab: String, type: String = CircleGroupType.Circle_All) {
            val intent = Intent(act, CircleActivity::class.java)
            if (ForceUtils.activityMatch(intent)) {
                intent.putExtra(ParamConstant.DefaultTab, defaultTab)
                intent.putExtra(ParamConstant.TYPE, type)
                act.startActivity(intent)
            }
        }
    }

    private val mCircleViewModel: CircleViewModel by viewModels()

    private var mCommonNavigator: CommonNavigator? = null

    //标题列表
    private val mTabTitles = arrayListOf<String>("关注", "推荐")

    private var mFragmentList = SparseArray<Fragment>()

    override fun getLayoutId() = R.layout.act_circle

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        val type = intent?.getStringExtra(ParamConstant.TYPE)
        mCircleViewModel.mType = type ?: ""
        if (type == CircleGroupType.Circle_Choose) {
            //全部圈子
            header_page.textTitle.text = "发布到"
        } else {
            //选择圈子
            header_page.textTitle.text = "全部圈子"
        }

        initMagicIndicator()
        initViewPager()
        val defaultTab = intent?.getStringExtra(ParamConstant.DefaultTab) ?: ""
        if (defaultTab == CircleGroupTabType.Recom) {
            //选中推荐模块
            view_pager.currentItem = 1
        }
    }

    override fun initEvents(rootView: View) {
        super.initEvents(rootView)
        header_page.imageViewBack.onClickNew {
            finish()
        }
    }

    /**
     * 初始化指示器
     */
    private fun initMagicIndicator() {
        mCommonNavigator = CommonNavigator(this)
        mCommonNavigator?.scrollPivotX = 0.65f
//        mCommonNavigator.isAdjustMode = true
        mCommonNavigator?.adapter = object : CommonNavigatorAdapter() {
            override fun getCount(): Int {
                return mTabTitles.size
            }

            override fun getTitleView(context: Context, index: Int): IPagerTitleView {
                logger.info("getTitleView：$index")
                val simplePagerTitleView: ScaleTransitionPagerTitleView = ScaleTransitionPagerTitleView(context)
                simplePagerTitleView.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                simplePagerTitleView.minScale = 0.583f
                simplePagerTitleView.text = mTabTitles[index]
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

    private fun initViewPager() {
        //注 这里只是用到不可滑动功能 没有使用关联viewpager
        view_pager.adapter = mPagerAdapter
        //配置预加载页数
//        view_pager.offscreenPageLimit = 2
        view_pager.currentItem = 0

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
                val fragment: Fragment = when (mTabTitles.getOrNull(position)) {
                    "关注" -> AttentionCircleFragment.newInstance()
//                    "推荐" -> MakeFriendsFragment.newInstance()
                    else -> RecommendCircleFragment.newInstance()
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