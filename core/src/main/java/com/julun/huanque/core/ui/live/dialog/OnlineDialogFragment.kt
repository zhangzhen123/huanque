package com.julun.huanque.core.ui.live.dialog

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.SparseArray
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.base.BaseFragment
import com.julun.huanque.common.base.BaseVMFragment
import com.julun.huanque.common.basic.TabBean
import com.julun.huanque.common.constant.IntentParamKey
import com.julun.huanque.common.constant.TabTags
import com.julun.huanque.common.suger.dp2px
import com.julun.huanque.common.suger.dp2pxf
import com.julun.huanque.common.utils.GlobalUtils
import com.julun.huanque.common.widgets.ColorFlipPagerTitleView
import com.julun.huanque.core.R
import com.julun.huanque.core.ui.live.PlayerViewModel
import com.julun.huanque.core.ui.live.fragment.OnlineListFragment
import kotlinx.android.synthetic.main.dialog_online.*
import net.lucode.hackware.magicindicator.ViewPagerHelper
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator
import org.jetbrains.anko.textSizeDimen

/**
 * 贵族在线 or 普通在线弹窗
 * @author WanZhiYuan
 * @since 4.24
 * @create 2019/12/26
 */
class OnlineDialogFragment : BaseDialogFragment() {

    private val mPlayerViewModel: PlayerViewModel by activityViewModels()

    private var mFragmentList = SparseArray<BaseFragment>()

    private lateinit var mTabs: ArrayList<TabBean>

    private lateinit var mCommonNavigator: CommonNavigator
    companion object {
        /**
         */
        fun newInstance(tabs: ArrayList<TabBean>): OnlineDialogFragment {
            val fragment = OnlineDialogFragment()
            val bundle = Bundle()
            bundle.putSerializable(IntentParamKey.LIST.name, tabs)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun getLayoutId(): Int = R.layout.dialog_online

    override fun onStart() {
        super.onStart()
        setDialogSize(width = ViewGroup.LayoutParams.MATCH_PARENT, height = 377, gravity = Gravity.BOTTOM)
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    }

    override fun onDestroyView() {
        mPlayerViewModel.updateOnLineCount.value = null
        super.onDestroyView()
    }

    override fun initViews() {
        prepareViewModel()
        mTabs = arguments?.getSerializable(IntentParamKey.LIST.name) as? ArrayList<TabBean> ?: arrayListOf()

        vpOnlinePager.adapter = adapter
        //配置预加载页数
        vpOnlinePager.offscreenPageLimit = mTabs.size

        initMagicIndicator(mTabs.indexOfFirst { it.select })
    }

    private fun prepareViewModel() {

        mPlayerViewModel.updateOnLineCount.observe(this, Observer {
            it ?: return@Observer

            mTabs.forEach { tab ->
//                if (it.containsKey(tab.type)) {
                when (tab.type) {
                    TabTags.TAB_TAG_ROYAL -> {
                        if (it[tab.type].isNullOrEmpty()) {
                            tab.title = "贵族席位"
                        } else {
                            tab.title = "贵族席位(${it[tab.type]})"
                        }

                    }
                    TabTags.TAB_TAG_GUARD -> {
                        if (it[tab.type].isNullOrEmpty()) {
                            tab.title = "守护"
                        } else {
                            tab.title = "守护(${it[tab.type]})"
                        }
                    }
                    TabTags.TAB_TAG_MANAGER -> {
                        if (it[tab.type].isNullOrEmpty()) {
                            tab.title = "房管"
                        } else {
                            tab.title = "房管(${it[tab.type]})"
                        }
                    }
                    else -> {
                        if (it[tab.type].isNullOrEmpty()) {
                            tab.title = "在线用户"
                        } else {
                            tab.title = "在线用户(${it[tab.type]})"
                        }
                    }
                }
            }
            mCommonNavigator.notifyDataSetChanged()
//            }
        })
//        mPlayerViewModel?.goGuardAndShowBubbleDialog?.observe(this, Observer {
//            it ?: return@Observer
//            if (mPlayerViewModel?.isThemeRoom == true) {
//                //官方主题房就没有守护页
//                return@Observer
//            }
//            if (mDialogType != DialogTypes.DIALOG_ROYAL || mDialogType != DialogTypes.DIALOG_GUARD) {
//                return@Observer
//            }
//            //切换到守护tab 如果it -> true 就在退出时展示守护引导bubble
//            if (mPlayerViewModel?.experienceGuard?.value?.status == "UnUsed" && it) {
//                mExperienceSource = it
//            }
//            vpOnlinePager.currentItem = mTabTitles.size - 1
//            mPlayerViewModel?.goGuardAndShowBubbleDialog?.value = null
//        })
    }

    /**
     * 初始化指示器
     */
    private fun initMagicIndicator(position: Int = 0) {
        mCommonNavigator = CommonNavigator(activity)
        mCommonNavigator.scrollPivotX = 0.65f
        mCommonNavigator.isAdjustMode = true
        adapter.let {
            mCommonNavigator.adapter = object : CommonNavigatorAdapter() {
                override fun getCount(): Int {
                    return it.count
                }

                override fun getTitleView(context: Context, index: Int): IPagerTitleView? {
                    if (mTabs.isEmpty() || mTabs.size <= index) {
                        return null
                    }
                    val simplePagerTitleView = ColorFlipPagerTitleView(context)
                    simplePagerTitleView.textSizeDimen = R.dimen.sp_16
                    simplePagerTitleView.text = mTabs[index].title
                    simplePagerTitleView.selectedColor = GlobalUtils.getColor(R.color.black_333)
                    simplePagerTitleView.normalColor = GlobalUtils.getColor(R.color.black_999)
                    simplePagerTitleView.setOnClickListener { vpOnlinePager.currentItem = index }
                    return simplePagerTitleView
                }

                override fun getIndicator(context: Context): IPagerIndicator? {
                    val indicator = LinePagerIndicator(context)
                    indicator.mode = LinePagerIndicator.MODE_EXACTLY
                    indicator.lineHeight = dp2pxf(2)
                    indicator.lineWidth = dp2pxf(24)
                    indicator.roundRadius = dp2pxf(2)
                    indicator.startInterpolator = AccelerateInterpolator()
                    indicator.endInterpolator = DecelerateInterpolator(2.0f)
                    indicator.setColors(GlobalUtils.getColor(R.color.app_main))
                    return indicator
                }
            }
        }

        miOnlineIndicator.navigator = mCommonNavigator
//        mCommonNavigator.onPageSelected(position)
        ViewPagerHelper.bind(miOnlineIndicator, vpOnlinePager)

        vpOnlinePager.currentItem = position
    }

    private val adapter: FragmentPagerAdapter by lazy {
        object : FragmentPagerAdapter(childFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            override fun getItem(position: Int): BaseFragment {
                return mFragmentList[position] ?: getFragment(position)
            }

            override fun getCount(): Int {
                return mTabs.size
            }

            private fun getFragment(position: Int): BaseFragment {
                val fragment = OnlineListFragment.newInstance(
                    position, mTabs.getOrNull(position)?.type ?: ""
                )


                mFragmentList.put(position, fragment)
                return fragment
            }

            override fun getPageTitle(position: Int): CharSequence {
                return mTabs[position].title
            }

            override fun saveState(): Parcelable? {
                return null
            }
        }
    }

    override fun refreshDialog() {
        val count = adapter.count
        for (i in 0 until count) {
            val fragment = adapter?.getItem(i)
            if (fragment is BaseVMFragment<*>) {
                fragment.refreshView()
            }

        }
    }
}