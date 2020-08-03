package com.julun.huanque.core.ui.live.dialog

import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.constant.IntentParamKey
import com.julun.huanque.common.widgets.ColorFlipPagerTitleView
import com.julun.huanque.core.R
import com.julun.huanque.core.ui.live.PlayerViewModel
import com.julun.huanque.core.ui.live.fragment.ScoreFragment
import kotlinx.android.synthetic.main.dialog_score.*
import net.lucode.hackware.magicindicator.ViewPagerHelper
import net.lucode.hackware.magicindicator.buildins.UIUtil
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator
import org.jetbrains.anko.textSizeDimen
import java.util.*

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/8/1 11:04
 *
 *@Description: ScoreDialogFragment
 *
 */
class ScoreDialogFragment : BaseDialogFragment() {

    companion object {
        fun newInstance(programId: Long): ScoreDialogFragment {
            val args = Bundle()
            args.putLong(IntentParamKey.PROGRAM_ID.name,programId)
            val fragment = ScoreDialogFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var mCommonNavigator: CommonNavigator

    private val playerViewModel: PlayerViewModel by activityViewModels()

    private val tabRankIds = arrayListOf(10001, 10002, 10003)
    private val tabTitles = arrayListOf("本场", "本周", "本月")
    private var fragmentList = ArrayList<androidx.fragment.app.Fragment>()

    override fun getLayoutId(): Int = R.layout.dialog_score
    override fun onStart() {
        super.onStart()
        setDialogSize(width = ViewGroup.LayoutParams.MATCH_PARENT, height = 404)
    }

    override fun initViews() {

        initViewModel()
        val programId = arguments?.getLong(IntentParamKey.PROGRAM_ID.name, 0L) ?: return
        fragmentList.add(ScoreFragment.newInstance().apply {
            setArgument(programId, tabRankIds[0])
        })
        fragmentList.add(ScoreFragment.newInstance().apply {
            setArgument(programId, tabRankIds[1])
        })
        fragmentList.add(ScoreFragment.newInstance().apply {
            setArgument(programId, tabRankIds[2])
        })

//        fragmentList.add(ScoreFragment(programId, tabRankIds[1]))
//        fragmentList.add(ScoreFragment(programId, tabRankIds[2]))

        scoreViewPager.adapter = scorePagerAdapter
        scoreViewPager.offscreenPageLimit = fragmentList.size
        scoreViewPager.currentItem = 0


        initMagicIndicator()
    }


    /**
     * 初始化ViewModel相关
     */
    private fun initViewModel() {
        playerViewModel.privateMessageView.observe(this, Observer {
            //todo
        })
    }

    /**
     * 初始化指示器
     */
    private fun initMagicIndicator() {
        mCommonNavigator = CommonNavigator(requireContext())
        mCommonNavigator.isEnablePivotScroll
        mCommonNavigator.scrollPivotX = 0.65f
        mCommonNavigator.isAdjustMode = true
        mCommonNavigator.isSkimOver = true
        scorePagerAdapter.let {
            mCommonNavigator.adapter = object : CommonNavigatorAdapter() {
                override fun getCount(): Int {
                    return it.count
                }

                override fun getTitleView(context: Context, index: Int): IPagerTitleView {
                    val simplePagerTitleView = ColorFlipPagerTitleView(context)
                    simplePagerTitleView.textSizeDimen = R.dimen.text_size_small
                    simplePagerTitleView.text = tabTitles[index]
                    simplePagerTitleView.normalColor = ContextCompat.getColor(context, R.color.black_999)
                    simplePagerTitleView.selectedColor = ContextCompat.getColor(context, R.color.black_333)
                    simplePagerTitleView.setOnClickListener { scoreViewPager.currentItem = index }
                    return simplePagerTitleView
                }

                override fun getIndicator(context: Context): IPagerIndicator {
                    val indicator = LinePagerIndicator(context)
                    indicator.mode = LinePagerIndicator.MODE_EXACTLY
                    indicator.lineHeight = UIUtil.dip2px(context, 2.0).toFloat()
                    indicator.lineWidth = UIUtil.dip2px(context, 28.0).toFloat()
                    indicator.roundRadius = UIUtil.dip2px(context, 3.0).toFloat()
                    indicator.startInterpolator = AccelerateInterpolator()
                    indicator.endInterpolator = DecelerateInterpolator(2.0f)
//                    indicator.yOffset = dip(11f).toFloat()
                    indicator.setColors(ContextCompat.getColor(context, R.color.app_main))
                    return indicator
                }
            }
        }
        magic_indicator.navigator = mCommonNavigator
        ViewPagerHelper.bind(magic_indicator, scoreViewPager)
    }

    private val scorePagerAdapter: androidx.fragment.app.FragmentStatePagerAdapter by lazy {
        object : androidx.fragment.app.FragmentStatePagerAdapter(childFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            override fun getItem(position: Int): androidx.fragment.app.Fragment {
                return fragmentList[position]
            }

            override fun getCount(): Int {
                return fragmentList.size
            }

            override fun getPageTitle(position: Int): CharSequence {
                return tabTitles[position]
            }
        }
    }

}
