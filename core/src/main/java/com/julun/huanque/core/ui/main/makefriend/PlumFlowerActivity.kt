package com.julun.huanque.core.ui.main.makefriend

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.alibaba.android.arouter.facade.annotation.Route
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.constant.ParamConstant
import com.julun.huanque.common.helper.DensityHelper
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.utils.GlobalUtils
import com.julun.huanque.common.utils.StatusBarUtil
import com.julun.huanque.common.widgets.indicator.ColorFlipPagerTitleView
import com.julun.huanque.core.R
import com.julun.huanque.core.adapter.PlumFlowerFragmentAdapter
import kotlinx.android.synthetic.main.act_plum_flower.*
import net.lucode.hackware.magicindicator.ViewPagerHelper
import net.lucode.hackware.magicindicator.buildins.UIUtil
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.textSizeDimen

/**
 *@创建者   dong
 *@创建时间 2020/8/21 17:19
 *@描述 花魁页面
 */
@Route(path = ARouterConstant.PLUM_FLOWER_ACTIVITY)
class PlumFlowerActivity : BaseActivity() {

    private lateinit var mCommonNavigator: CommonNavigator
    private var mPagerAdapter: PlumFlowerFragmentAdapter? = null
    private var mFlowerIntroductionFragment: FlowerIntroductionFragment? = null

    override fun getLayoutId() = R.layout.act_plum_flower

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            super.finish()
            return
        }
        val type = intent?.getStringExtra(ParamConstant.TYPE) ?: ""
        val barHeight = StatusBarUtil.getStatusBarHeight(this)
        val params = view_top.layoutParams as? ConstraintLayout.LayoutParams
        params?.topMargin = barHeight
        view_top.layoutParams = params

        StatusBarUtil.setTransparent(this)
        val famousListFragment = FamousListFragment()
        val framList = mutableListOf<Fragment>()
        framList.add(DayListFragment(DayListFragment.TODAY, barHeight))
        framList.add(DayListFragment(DayListFragment.WEEK, barHeight))
        framList.add(famousListFragment)
        mPagerAdapter = PlumFlowerFragmentAdapter(framList, supportFragmentManager, this)
        pager.adapter = mPagerAdapter
        pager.offscreenPageLimit = 3
        initMagicIndicator()
        if (type == "Famous") {
            pager.currentItem = 2
        } else {
            pager.currentItem = 0
        }
    }

    override fun initEvents(rootView: View) {
        iv_close.onClickNew {
            finish()
        }

        iv_help.onClickNew {
            //显示说明弹窗
            mFlowerIntroductionFragment = mFlowerIntroductionFragment ?: FlowerIntroductionFragment()
            mFlowerIntroductionFragment?.show(supportFragmentManager, "FlowerIntroductionFragment")
        }

    }

    /**
     * 初始化指示器
     */
    private fun initMagicIndicator() {
        mCommonNavigator = CommonNavigator(this)
        mCommonNavigator.isEnablePivotScroll
        mCommonNavigator.scrollPivotX = 0.65f
        mCommonNavigator.isAdjustMode = false
        mCommonNavigator.isSkimOver = true
        mPagerAdapter?.let {
            mCommonNavigator.adapter = object : CommonNavigatorAdapter() {
                override fun getCount(): Int {
                    return it.count
                }

                override fun getTitleView(context: Context, index: Int): IPagerTitleView {
                    val simplePagerTitleView =
                        ColorFlipPagerTitleView(context)
                    simplePagerTitleView.textSizeDimen = R.dimen.text_size_big
                    simplePagerTitleView.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                    simplePagerTitleView.backgroundColor = Color.TRANSPARENT
                    val title = when (index) {
                        0 -> {
                            "日榜"
                        }
                        1 -> {
                            "周榜"
                        }
                        else -> {
                            "名人榜"
                        }
                    }
                    simplePagerTitleView.text = title

                    simplePagerTitleView.normalColor = GlobalUtils.formatColor("#A6FFFFFF")
                    simplePagerTitleView.selectedColor = GlobalUtils.getColor(R.color.white)
                    simplePagerTitleView.setOnClickListener { pager.currentItem = index }
                    if (mCommonNavigator.isAdjustMode) {
                        //固定模式，不设置padding
                        simplePagerTitleView.setPadding(0, 0, 0, 0)
                    } else {
                        //自适应模式设置padding
                        val padding = UIUtil.dip2px(this@PlumFlowerActivity, 21.0)
                        simplePagerTitleView.setPadding(padding, 0, padding, 0)
                    }
                    return simplePagerTitleView
                }

                override fun getIndicator(context: Context): IPagerIndicator {
                    val indicator = LinePagerIndicator(context)
                    indicator.mode = LinePagerIndicator.MODE_EXACTLY
                    indicator.lineHeight = UIUtil.dip2px(context, 3.0).toFloat()
                    indicator.lineWidth = UIUtil.dip2px(context, 20.0).toFloat()
                    indicator.roundRadius = UIUtil.dip2px(context, 3.0).toFloat()
                    indicator.startInterpolator = AccelerateInterpolator()
                    indicator.endInterpolator = DecelerateInterpolator(2.0f)
                    indicator.yOffset = DensityHelper.dp2px(4f).toFloat()
                    indicator.setColors(GlobalUtils.getColor(R.color.white))
                    return indicator
                }
            }
        }
        magic_indicator.navigator = mCommonNavigator
        ViewPagerHelper.bind(magic_indicator, pager)
    }
}