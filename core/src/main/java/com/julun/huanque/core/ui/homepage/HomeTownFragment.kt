package com.julun.huanque.core.ui.homepage

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.julun.huanque.common.base.BaseBottomSheetFragment
import com.julun.huanque.common.suger.dp2px
import com.julun.huanque.common.utils.ViewPager2Helper
import com.julun.huanque.common.widgets.indicator.ScaleTransitionPagerTitleView
import com.julun.huanque.core.R
import com.julun.huanque.core.adapter.HomeTownCircumAdapter
import com.julun.huanque.core.viewmodel.HomePageViewModel
import com.julun.huanque.core.viewmodel.HomeTownViewModel
import kotlinx.android.synthetic.main.frag_home_town.*
import net.lucode.hackware.magicindicator.ViewPagerHelper
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator
import java.lang.StringBuilder

/**
 *@创建者   dong
 *@创建时间 2020/12/28 20:05
 *@描述 家乡弹窗
 */
class HomeTownFragment : BaseBottomSheetFragment() {
    private val mHomeViewModel: HomePageViewModel by activityViewModels()
    private val mHomeTownViewModel: HomeTownViewModel by activityViewModels()
    private val mTabTitles = mutableListOf<String>("美食", "景点", "名人")

    //类型列表
    private val mTypeList = mutableListOf<String>()
    override fun getLayoutId() = R.layout.frag_home_town

    override fun initViews() {

        val userIdListBean = mHomeViewModel.homeInfoBean.value?.userId ?: return
        mHomeTownViewModel.queryHomeTownInfo(userIdListBean)
    }

    override fun getHeight() = dp2px(523)

    override fun onStart() {
        super.onStart()
        val win = dialog?.window ?: return
        win.setWindowAnimations(R.style.dialog_bottom_bottom_style)
        initViewModel()
        val parent = view?.parent
        if (parent is View) {
            parent.setBackgroundColor(Color.TRANSPARENT)
        }

    }

    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {
        mHomeViewModel.homeInfoBean.observe(this, Observer { bean ->
            if (bean != null) {
                val homeTownStr = StringBuilder()
                if (bean.homeTown.homeTownProvince.isNotEmpty()) {
                    homeTownStr.append(bean.homeTown.homeTownProvince)
                }
                if (homeTownStr.isNotEmpty()) {
                    homeTownStr.append("·")
                }
                homeTownStr.append(bean.homeTown.homeTownCity)
                tv_city.text = homeTownStr.toString()
            }
        })
        mHomeTownViewModel.homeTownInfo.observe(this, Observer {
            if (it != null) {
                tv_introduction.text = it.introduce
                mTabTitles.clear()
                it.cultureList.forEach { sc ->
                    mTypeList.add(sc.cultureType)
                    mTabTitles.add("${sc.cultureTypeText} ${sc.num}")
                }
                initMagicIndicator()
                initViewPager2(mTypeList)
            }
        })
    }

    /**
     * 初始化ViewPager2
     */
    private fun initViewPager2(typeList: MutableList<String>) {
        view_pager2.adapter = HomeTownCircumAdapter(requireActivity(), typeList)
        view_pager2.offscreenPageLimit = typeList.size
    }

    private lateinit var mCommonNavigator: CommonNavigator

    /**
     * 初始化历史记录指示器
     */
    private fun initMagicIndicator() {
        mCommonNavigator = CommonNavigator(requireContext())
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