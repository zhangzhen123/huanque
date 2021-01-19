package com.julun.huanque.message.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.core.content.ContextCompat
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.bean.forms.WatchForm
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.utils.ForceUtils
import com.julun.huanque.common.utils.ViewPager2Helper
import com.julun.huanque.common.widgets.indicator.ScaleTransitionPagerTitleView
import com.julun.huanque.message.R
import com.julun.huanque.message.adapter.WatchFragmentAdapter
import kotlinx.android.synthetic.main.act_watch_history.*
import net.lucode.hackware.magicindicator.ViewPagerHelper
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator

/**
 *@创建者   dong
 *@创建时间 2021/1/18 19:16
 *@描述 观看历史页面（谁看过我，我看过谁）
 */
class WatchHistoryActivity : BaseActivity() {
    companion object {
        fun newInstance(act: Activity) {
            val intent = Intent(act, WatchHistoryActivity::class.java)
            if (ForceUtils.activityMatch(intent)) {
                act.startActivity(intent)
            }
        }
    }

    private val mTabTitles = arrayListOf<String>("谁看过我", "我看过谁")
    private val mTypeList = arrayListOf<String>(WatchForm.SeenMe, WatchForm.HaveSeen)

    private lateinit var mCommonNavigator: CommonNavigator

    override fun getLayoutId() = R.layout.act_watch_history

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        initMagicIndicator()
        initViewPager2(mTypeList)
    }

    override fun initEvents(rootView: View) {
        super.initEvents(rootView)
        ivback.onClickNew {
            finish()
        }
    }

    /**
     * 初始化ViewPager2
     */
    private fun initViewPager2(typeList: MutableList<String>) {
        view_pager2.adapter = WatchFragmentAdapter(this, typeList)
        view_pager2.offscreenPageLimit = typeList.size
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

                val simplePagerTitleView: ScaleTransitionPagerTitleView =
                    ScaleTransitionPagerTitleView(context)
                simplePagerTitleView.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                simplePagerTitleView.minScale = 0.583f
                simplePagerTitleView.text = mTabTitles[index]
                simplePagerTitleView.textSize = 24f
                simplePagerTitleView.normalColor =
                    ContextCompat.getColor(context, R.color.black_666)
                simplePagerTitleView.selectedColor =
                    ContextCompat.getColor(context, R.color.black_333)
                simplePagerTitleView.setOnClickListener { view_pager2.currentItem = index }
                if (view_pager2.currentItem == index) {
                    simplePagerTitleView.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.black_333
                        )
                    )
                    simplePagerTitleView.scaleX = 1.0f
                    simplePagerTitleView.scaleY = 1.0f
                } else {
                    simplePagerTitleView.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.black_666
                        )
                    )
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
                indicator.lineHeight = 6f
                indicator.lineWidth = 12f
                indicator.roundRadius = 6f
                indicator.startInterpolator = AccelerateInterpolator()
                indicator.endInterpolator = DecelerateInterpolator(2.0f)
                indicator.yOffset = 12f
                indicator.setColors(context.resources.getColor(R.color.primary_color))
                return indicator
            }
        }
        magic_indicator.navigator = mCommonNavigator
        ViewPager2Helper.bind(magic_indicator, view_pager2)
    }
}