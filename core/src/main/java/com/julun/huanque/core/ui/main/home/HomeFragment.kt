package com.julun.huanque.core.ui.main.home

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.SparseArray
import android.view.MotionEvent
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
import com.facebook.drawee.generic.RoundingParams
import com.julun.huanque.common.base.BaseFragment
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.helper.StorageHelper
import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.DateHelper
import com.julun.huanque.common.utils.ForceUtils
import com.julun.huanque.common.utils.ScreenUtils
import com.julun.huanque.common.utils.ULog
import com.julun.huanque.common.widgets.indicator.ScaleTransitionPagerTitleView
import com.julun.huanque.core.R
import com.julun.huanque.core.dialog.TodayFateDialogFragment
import com.julun.huanque.core.ui.main.makefriend.MakeFriendsFragment
import com.julun.huanque.core.ui.main.makefriend.PlumFlowerActivity
import com.julun.huanque.core.viewmodel.TodayFateViewModel
import com.julun.rnlib.RnManager
import com.luck.picture.lib.tools.StatusBarUtil
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.fragment_main.home_container
import kotlinx.android.synthetic.main.fragment_main.magic_indicator
import kotlinx.android.synthetic.main.fragment_main.view_pager
import kotlinx.android.synthetic.main.fragment_program_container.*
import net.lucode.hackware.magicindicator.ViewPagerHelper
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.CommonPagerTitleView
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.topPadding
import kotlin.math.abs

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

        //距离屏幕的间距
        private const val mMargin = 40

    }
    private var mTodayFateDialogFragment: TodayFateDialogFragment? = null

    private lateinit var mCommonNavigator: CommonNavigator
    private var mFragmentList = SparseArray<Fragment>()
    private val mTabTitles = arrayListOf<String>()


    //x最小值
    private val xMin = mMargin

    //X最大值
    private var xMax = ScreenUtils.getScreenWidth() - mMargin - dp2px(61)

    //y最小值
    private val yMin = StatusBarUtil.getStatusBarHeight(CommonInit.getInstance().getContext())

    //y最大值
    private var yMax = ScreenUtils.getScreenHeight() - dp2px(60 + 45)


    override fun getLayoutId(): Int {
        return R.layout.fragment_main
    }

    private val viewModel: HomeViewModel by activityViewModels()
    private val mTodayFateViewModel: TodayFateViewModel by activityViewModels()


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
        sdw_flower.onClickNew {
            activity?.let { act ->
                val intent = Intent(act, PlumFlowerActivity::class.java)
                if (ForceUtils.activityMatch(intent)) {
                    act.startActivity(intent)
                }
            }
        }
        rl_fate.setOnTouchListener(object : View.OnTouchListener {
            private var x = 0f
            private var y = 0f

            //            //touch事件的开始时间
//            private var startTime: Long = 0
//
//            //touch事件的结束时间
//            private var endTime: Long = 0
            private var isDrag = false
            override fun onTouch(view: View?, event: MotionEvent): Boolean {
                if (view == null) {
                    return false
                }
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        x = event.rawX
                        y = event.rawY
//                        startTime = System.currentTimeMillis()
                        isDrag = false
                    }
                    MotionEvent.ACTION_MOVE -> {

                        val nowX = event.rawX
                        val nowY = event.rawY
                        val movedX = nowX - x
                        val movedY = nowY - y
                        if (abs(movedX) > 0 || abs(movedY) > 0) {
                            isDrag = true
                        }
                        x = nowX
                        y = nowY
                        var tempX = (view.x + movedX).toInt()
                        if (tempX < xMin) {
                            tempX = xMin
                        } else if (tempX > xMax) {
                            tempX = xMax
                        }
                        view.x = tempX.toFloat()

                        var tempY = view.y + movedY.toInt()
                        if (tempY < yMin) {
                            tempY = yMin.toFloat()
                        } else if (tempY > yMax) {
                            tempY = yMax.toFloat()
                        }
                        view.y = tempY
                    }
                    MotionEvent.ACTION_UP -> {

                        if (isDrag) {
                            yuanFenAnimationToSide()
                        } else {
                            logger.info("点击处理")
                            mTodayFateDialogFragment = mTodayFateDialogFragment ?: TodayFateDialogFragment()
                            mTodayFateDialogFragment?.show(requireActivity(), "TodayFateDialogFragment")
                        }

                    }
                }

                return true
            }
        })

    }

    //缘分View 移动动画
    private var mYuanFenTranslationAnimation: ObjectAnimator? = null

    /**
     * 缘分图标  动画移动到屏幕边侧
     */
    private fun yuanFenAnimationToSide() {
        val screenMiddleX = ScreenUtils.getScreenWidth() / 2
        val viewMiddleX = rl_fate.x + rl_fate.width / 2
        val targetX = if (viewMiddleX > screenMiddleX) {
            //View在屏幕右侧
            xMax
        } else {
            //View在屏幕左侧
            xMin
        }
        mYuanFenTranslationAnimation?.cancel()
        mYuanFenTranslationAnimation = ObjectAnimator.ofFloat(rl_fate, "x", rl_fate.x, targetX.toFloat())
        mYuanFenTranslationAnimation?.duration = 200
        mYuanFenTranslationAnimation?.start()
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
                mTabTitles.addAll(it.requireT())
                refreshTabList("推荐")
            }

        })
        viewModel.queryInfo()
        viewModel.flowerPic.observe(viewLifecycleOwner, Observer {
            //
            if (it != null) {
                iv_flower_fg.show()
                val roundingParams: RoundingParams = RoundingParams.asCircle()
                sdw_flower.hierarchy.roundingParams = roundingParams
                sdw_flower.loadImage(it, 32f, 32f)
                iv_flower_fg.imageResource = R.mipmap.fg_home_flower_top
            } else {
                iv_flower_fg.hide()
                sdw_flower.hierarchy.roundingParams?.roundAsCircle = false
                sdw_flower.loadImageLocal(R.mipmap.icon_home_flower_top)
            }
        })

        mTodayFateViewModel.closeFateDialogTag.observe(viewLifecycleOwner, Observer {
            logger.info("closeFateDialogTag=$it")
            //今日缘分关闭后
            if (it == true) {
                mTodayFateViewModel.closeFateDialogTag.value = null
                if (!mTodayFateViewModel.isComplete) {
                    rl_fate.show()
                } else {
                    rl_fate.hide()
                }
            }
        })


        mTodayFateViewModel.matchesInfo.observe(this, Observer {
            it ?: return@Observer
            if (it.isSuccess()) {
                if (it.requireT().showTodayFate) {

                    val date = StorageHelper.getLastTodayFateDialogTime()

                    val today = DateHelper.formatNow()

                    val diff = if (date.isNotEmpty()) {
                        DateHelper.getDifferDays(date, today)
                    } else {
                        -1
                    }
                    ULog.i("今日缘分弹窗间隔时间：$diff")
                    if (diff == -1 || diff >= 1) {
                        mTodayFateDialogFragment = mTodayFateDialogFragment ?: TodayFateDialogFragment()
                        mTodayFateDialogFragment?.show(requireActivity(), "TodayFateDialogFragment")
                        //弹过就不再重复弹出
                        StorageHelper.setLastTodayFateDialogTime()
                    }else{
                        rl_fate.show()
                    }

                }
            }
        })
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
//                val simplePagerTitleView = CommonPagerTitleView(context)
//                simplePagerTitleView.setContentView(R.layout.view_home_tab_title)
//
////                val container = simplePagerTitleView.findViewById<RelativeLayout>(R.id.tab_container)
//                val tabTitle = simplePagerTitleView.findViewById<TextView>(R.id.tvTabTitle)
////                val dot = simplePagerTitleView.findViewById<ImageView>(R.id.tab_dot)
//                simplePagerTitleView.onPagerTitleChangeListener = object : CommonPagerTitleView.OnPagerTitleChangeListener {
//                    override fun onDeselected(index: Int, totalCount: Int) {
////                            tabTitle.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
//                        logger.info("onDeselected:$index")
//                        tabTitle.setTextColor(ContextCompat.getColor(context, R.color.black_666))
//                        tabTitle.textSize = 14f
//                    }
//
//                    override fun onSelected(index: Int, totalCount: Int) {
//                        logger.info("onSelected:$index")
//                        tabTitle.setTextColor(ContextCompat.getColor(context, R.color.black_333))
//                        tabTitle.textSize = 24f
//
//                    }
//
//                    override fun onLeave(index: Int, totalCount: Int, leavePercent: Float, leftToRight: Boolean) {
//                    }
//
//                    override fun onEnter(index: Int, totalCount: Int, enterPercent: Float, leftToRight: Boolean) {
//                    }
//                }
//                tabTitle.text = mTabTitles[index]
//                simplePagerTitleView.setOnClickListener { view_pager.currentItem = index }
//                if (view_pager.currentItem == index) {
//                    tabTitle.setTextColor(ContextCompat.getColor(context, R.color.black_333))
//                    tabTitle.textSize = 24f
//                } else {
//                    tabTitle.setTextColor(ContextCompat.getColor(context, R.color.black_666))
//                    tabTitle.textSize = 14f
//                }
//                return simplePagerTitleView

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

    /**
     * 滑动到顶部
     */
    fun scrollToTop() {
        val tempIndex = view_pager.currentItem
        val tempFragment: androidx.fragment.app.Fragment? = mPagerAdapter.getItem(tempIndex)
        tempFragment?.let {
            (tempFragment as? MakeFriendsFragment)?.scrollToTopAndRefresh()
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
        object : FragmentPagerAdapter(childFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            override fun getItem(position: Int): Fragment {
                return mFragmentList[position] ?: getFragment(position)
            }

            override fun getCount(): Int {
                return mTabTitles.size
            }

            private fun getFragment(position: Int): Fragment {
                val fragment: Fragment = when (mTabTitles.getOrNull(position)) {
                    "推荐" -> MakeFriendsFragment.newInstance()
//                    "推荐" -> MakeFriendsFragment.newInstance()
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