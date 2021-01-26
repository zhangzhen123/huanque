package com.julun.huanque.core.ui.main.heartbeat

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.util.SparseArray
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.core.animation.addListener
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import com.julun.huanque.common.base.BaseFragment
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.bean.TplBean
import com.julun.huanque.common.bean.beans.PagerTab
import com.julun.huanque.common.constant.TouchTypeConstants
import com.julun.huanque.common.helper.AppHelper
import com.julun.huanque.common.helper.DensityHelper
import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.common.message_dispatch.MessageProcessor
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.ScreenUtils
import com.julun.huanque.common.utils.SessionUtils
import com.julun.huanque.common.widgets.indicator.ScaleTransitionPagerTitleView
import com.julun.huanque.core.R
import com.julun.huanque.core.viewmodel.MainConnectViewModel
import com.julun.rnlib.RnManager
import com.luck.picture.lib.tools.StatusBarUtil
import kotlinx.android.synthetic.main.fragment_heartbeat_container.*
import net.lucode.hackware.magicindicator.ViewPagerHelper
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.topPadding
import java.util.*

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/12/16 9:32
 *
 *@Description: HeartbeatFragment 心动页面
 *
 */
class HomeHeartbeatFragment : BaseFragment() {

    companion object {
        fun newInstance() = HomeHeartbeatFragment()
    }

    private val mMainConnectViewModel: MainConnectViewModel by activityViewModels()

    private lateinit var mCommonNavigator: CommonNavigator
    private var mFragmentList = SparseArray<Fragment>()
    private val mTabTitles = arrayListOf<PagerTab>()

    override fun getLayoutId(): Int {
        return R.layout.fragment_heartbeat_container
    }

//    val queueList: LinkedList<TplBean> = LinkedList()

    private val viewModel: HeartbeatViewModel by viewModels()

    private var mFilterTagFragment: FilterTagFragment? = null
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
        v_filter_tag_holder.onClickNew {
            mFilterTagFragment = FilterTagFragment()
            mFilterTagFragment?.show(childFragmentManager, "FilterTagFragment")
        }
        MessageProcessor.registerTxtProcessor(this, object : MessageProcessor.HomeHeadLineMessageReceiver {
            override fun processMessage(messageList: List<TplBean>) {
                // 需要做排队
                logger.info("收到头条消息：${messageList.getOrNull(0)?.realTxt}")
//                queueList.addAll(messageList)
                head_runway.loadLastRunwayMessage(messageList.firstOrNull()?:return)
//                playRunway()
            }
        })
//        runway_headLine.onClickNew {
//            currentHeadline?.let { tpl ->
//                if (tpl.textTouch != null && tpl.context?.touchValue != null) {
//                    if (tpl.textTouch == TouchTypeConstants.MineHomePage && tpl.context!!.touchValue.toLongOrNull() == SessionUtils.getUserId()) {
//                        return@onClickNew
//                    }
//                    AppHelper.openTouch(tpl.textTouch!!, tpl.context!!.touchValue)
//                }
//
//            }
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
//        playMessageAnimator?.cancel()
        MessageProcessor.removeProcessors(this)
    }

    private fun initViewPager() {
        view_pager.adapter = mPagerAdapter
        //配置预加载页数
//        view_pager.offscreenPageLimit = 2
        view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                view_pager.noScroll = position == 0
                if (position == 1) {
                    tv_filter_tag.hide()
                } else {
                    tv_filter_tag.show()
                }


            }

        })
        view_pager.currentItem = 0
    }

//    private var isPlaying = false
//    private var playMessageAnimator: Animator? = null

//    private var currentHeadline: TplBean? = null
//    private fun playRunway() {
//
//        if (!isPlaying && queueList.isNotEmpty()) {
//
//            isPlaying = true
//            val tpl = queueList.pop()
//            currentHeadline = tpl
//            runway_headLine.render(tpl)
//            val x = ScreenUtils.getViewRealWidth(runway_headLine)
//            val runwayWidth = fl_runway.width
//            val start = if (runwayWidth > 0) {
//                runwayWidth
//            } else {
//                ScreenUtils.getScreenWidth() - dp2px(100)
//            }.toFloat()
//            val end = -x.toFloat()//这么简单的动画竟然折腾了一大圈 也是醉了
//            playMessageAnimator = ObjectAnimator.ofFloat(runway_headLine, "translationX", start, end)
//            playMessageAnimator?.duration = calculateDuration(x.toFloat())
//            playMessageAnimator?.interpolator = LinearInterpolator()
//            playMessageAnimator?.addListener(
//                onStart = {
//                    runway_headLine.show()
//                },
//                onEnd = {
//                    runway_headLine.hide()
//                    isPlaying = false
//                    playRunway()
//                })
//            playMessageAnimator?.start()
//        }
//
//    }

//    private fun calculateDuration(length: Float): Long {
//        val width = DensityHelper.px2dp(length)
//        //文本长度分解成固定每秒播放的dp宽度计算最终这次的文本需要的播放时间
//        val duration = (width / 50f + 0.5f) * 1000
//        logger.info("当前的view的长度：" + width + "当前的播放时长：" + duration.toLong())
//        return duration.toLong()
//    }

    private fun initViewModel() {
        viewModel.tabList.observe(viewLifecycleOwner, Observer {
            if (it.state == NetStateType.SUCCESS) {
                mTabTitles.clear()
                mTabTitles.addAll(it.requireT())
                refreshTabList()
            }

        })
        mMainConnectViewModel.heartBeatSwitch.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                switchToTab(it)
                mMainConnectViewModel.heartBeatSwitch.value = null
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
                simplePagerTitleView.backgroundResource=R.color.transparent
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
                indicator.setColors(ContextCompat.getColor(context, R.color.primary_color))
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

        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        logger.info("onHiddenChanged=$hidden")
        super.onHiddenChanged(hidden)
        val tempIndex = view_pager.currentItem
        val tempFragment: androidx.fragment.app.Fragment? = mPagerAdapter.getItem(tempIndex)
        tempFragment?.let {
            if (it is BaseFragment) {
                it.onParentHiddenChanged(hidden)
            }

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
                val fragment: Fragment = when (position) {
                    0 -> NearbyFragment.newInstance()
                    1 -> FavoriteFragment.newInstance()
                    else -> {
                        FavoriteFragment.newInstance()
                    }
                }

                mFragmentList.put(position, fragment)
                return fragment
            }

            override fun getPageTitle(position: Int): CharSequence {
                return mTabTitles[position].typeName
            }
        }
    }

}