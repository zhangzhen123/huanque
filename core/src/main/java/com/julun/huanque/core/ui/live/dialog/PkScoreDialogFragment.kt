package com.julun.huanque.core.ui.live.dialog

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.base.dialog.WebDialogFragment
import com.julun.huanque.common.constant.IntentParamKey
import com.julun.huanque.common.suger.dp2px
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.common.widgets.indicator.ColorFlipPagerTitleView
import com.julun.huanque.core.R
import com.julun.huanque.core.ui.live.PlayerViewModel
import com.julun.huanque.core.ui.live.fragment.PkScoreFragment
import kotlinx.android.synthetic.main.dialog_pk_score.*
import kotlinx.android.synthetic.main.dialog_score.magic_indicator
import kotlinx.android.synthetic.main.dialog_score.scoreViewPager
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
 *@Date: 2020/11/16 14:51
 *
 *@Description: PK贡献榜
 *
 */
class PkScoreDialogFragment : BaseDialogFragment() {

    companion object {
        fun newInstance(programIds: LongArray, pkId: Long): PkScoreDialogFragment {
            val args = Bundle()
            args.putLongArray(IntentParamKey.PROGRAM_ID.name, programIds)
            args.putLong(PkScoreFragment.PK_ID, pkId)
            val fragment = PkScoreDialogFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var mCommonNavigator: CommonNavigator

    private val playerViewModel: PlayerViewModel by activityViewModels()

    private var pkId: Long = 0L
    private val tabTitles = arrayListOf("我方榜单", "对方榜单")
    private var fragmentList = ArrayList<androidx.fragment.app.Fragment>()

    override fun getLayoutId(): Int = R.layout.dialog_pk_score


    override fun configDialog() {
        setDialogSize(width = ViewGroup.LayoutParams.MATCH_PARENT, height = 480)
    }
    override fun initViews() {
        initViewModel()
        val programIds = arguments?.getLongArray(IntentParamKey.PROGRAM_ID.name) ?: return
        pkId = arguments?.getLong(PkScoreFragment.PK_ID) ?: 0L
        if (programIds.size < 2) {
            ToastUtils.show2("参数无效")
            dismiss()
            return
        }
        fragmentList.add(PkScoreFragment.newInstance().apply {
            setArgument(programIds[0], pkId)
        })
        fragmentList.add(PkScoreFragment.newInstance().apply {
            setArgument(programIds[1], pkId)
        })

        scoreViewPager.adapter = scorePagerAdapter
        scoreViewPager.offscreenPageLimit = fragmentList.size
        scoreViewPager.currentItem = 0


        initMagicIndicator()

        pk_help.onClickNew {
            val webDialog = WebDialogFragment(playerViewModel.pkDesUrl)
                .setViewParams(height = 300,width = 270, gravity = Gravity.CENTER,leftAndRightMargin = 15).isNeedBottomBtn("知道了")
                .setDialogBg(R.drawable.bg_shape_white1)


            webDialog.setListener(callback = WebDialogFragment.WebCallback(onBottom = { webDialog.dismiss() }))
            webDialog.show(requireActivity(), "WebDialogFragment")
        }
    }


    /**
     * 初始化ViewModel相关
     */
    private fun initViewModel() {

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
                    val simplePagerTitleView =
                        ColorFlipPagerTitleView(context)
                    simplePagerTitleView.textSizeDimen = R.dimen.text_size_big
                    simplePagerTitleView.text = tabTitles[index]
                    simplePagerTitleView.normalColor = ContextCompat.getColor(context, R.color.black_999)
                    simplePagerTitleView.selectedColor = ContextCompat.getColor(context, R.color.black_333)
                    simplePagerTitleView.setOnClickListener { scoreViewPager.currentItem = index }
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
