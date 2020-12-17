package com.julun.huanque.core.ui.main.program

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.julun.huanque.common.base.BaseFragment
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.bean.beans.FollowProgramInfo
import com.julun.huanque.common.bean.beans.PagerTab
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.common.suger.dp2pxf
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.common.widgets.indicator.ScaleTransitionPagerTitleView
import com.julun.huanque.core.R
import com.julun.huanque.core.ui.main.follow.FollowActivity
import com.julun.huanque.core.ui.main.follow.FollowViewModel
import com.julun.huanque.core.ui.search.SearchActivity
import com.julun.huanque.core.widgets.SurfaceVideoViewOutlineProvider
import com.julun.rnlib.RnManager
import com.luck.picture.lib.tools.StatusBarUtil
import kotlinx.android.synthetic.main.fragment_program_container.*
import net.lucode.hackware.magicindicator.ViewPagerHelper
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.imageBitmap
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.topPadding

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/10/28 15:30
 *
 *@Description: ProgramFragment 首页直播节目页面
 *
 */
class HomeProgramFragment : BaseFragment() {

    companion object {
        fun newInstance() = HomeProgramFragment()
    }

    private lateinit var mCommonNavigator: CommonNavigator
    private var mFragmentList = SparseArray<Fragment>()
    private val mTabTitles = arrayListOf<PagerTab>()

    override fun getLayoutId(): Int {
        return R.layout.fragment_program_container
    }

    private val viewModel: ProgramViewModel by viewModels()

    private val followViewModel: FollowViewModel by activityViewModels()

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

        iv_search.onClickNew {
            requireActivity().startActivity<SearchActivity>()
        }


        view_flipper.onClickNew {
            requireActivity().startActivity<FollowActivity>()
        }


    }

    private fun initViewPager() {
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
                refreshTabList()
            }

        })
        followViewModel.followInfo.observe(viewLifecycleOwner, Observer {
            if (it.state == NetStateType.SUCCESS) {
                val result = it.getT()
                showViewFlipper(result)
            }
        })

        viewModel.queryInfo()

        followViewModel.requestProgramList(QueryType.INIT, isNullOffset = true)
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
//                tabTitle.text = mTabTitles[index].typeName
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
                simplePagerTitleView.text = mTabTitles[index].typeName
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
     * 显示入口数据
     */
    private fun showViewFlipper(followProgramInfo: FollowProgramInfo?) {
        view_flipper.removeAllViews()
        if (followProgramInfo == null) {
            view_flipper.hide()
            return
        }
        //关注列表
        val followList = followProgramInfo.followList
        if (followList.isNotEmpty()) {
            view_flipper.backgroundResource = R.mipmap.bg_program_enter_attention
            followList.forEach {
                val view = LayoutInflater.from(requireContext()).inflate(R.layout.view_program_follow_and_recommend, null)
                val sdv_header = view.findViewById<ImageView>(R.id.sdv_header) ?: return
                val tv_nickname = view.findViewById<TextView>(R.id.tv_nickname) ?: return
                val tv_type = view.findViewById<TextView>(R.id.tv_type) ?: return

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    sdv_header?.outlineProvider = SurfaceVideoViewOutlineProvider(dp2pxf(16));
                    sdv_header?.clipToOutline = true;
                }

                ImageUtils.requestImageForBitmap("${StringHelper.getOssImgUrl(it.headPic)}${BusiConstant.OSS_160}", { bitmap ->
                    activity?.runOnUiThread {
                        if (bitmap != null && bitmap?.isRecycled == false) {
                            val tempBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight());
                            sdv_header.imageBitmap = tempBitmap
                        }
                    }

                })
                tv_nickname.text = it.programName
                tv_type.text = "关注的人"
                view_flipper.addView(view)
            }
            if (followList.size > 1) {
                view_flipper.startFlipping()
            } else {
                view_flipper.stopFlipping()
            }
            return
        }
        //推荐列表
        val recommendList = followProgramInfo.recomList
        if (recommendList?.isNotEmpty() == true) {
            view_flipper.backgroundResource = R.mipmap.bg_program_enter_recom
            recommendList.forEach {
                val view = LayoutInflater.from(requireContext()).inflate(R.layout.view_program_follow_and_recommend, null)
                val sdv_header = view.findViewById<ImageView>(R.id.sdv_header) ?: return
                val tv_nickname = view.findViewById<TextView>(R.id.tv_nickname) ?: return
                val tv_type = view.findViewById<TextView>(R.id.tv_type) ?: return

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    sdv_header?.outlineProvider = SurfaceVideoViewOutlineProvider(dp2pxf(16));
                    sdv_header?.clipToOutline = true;
                }

                ImageUtils.requestImageForBitmap("${StringHelper.getOssImgUrl(it.headPic)}${BusiConstant.OSS_160}", { bitmap ->
                    activity?.runOnUiThread {
                        if (bitmap != null && bitmap?.isRecycled == false) {
                            val tempBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight());
                            sdv_header.imageBitmap = tempBitmap
                        }
                    }

                })
                tv_nickname.text = it.programName
                tv_type.text = "推荐关注"
                view_flipper.addView(view)
            }
            if (recommendList.size > 1) {
                view_flipper.startFlipping()
            } else {
                view_flipper.stopFlipping()
            }
            return
        }
        //执行到这行代码，表示关注和推荐都没有数据
        view_flipper.hide()

    }

    /**
     * 滑动到顶部
     */
    fun scrollToTop() {
        val tempIndex = view_pager.currentItem
        val tempFragment: androidx.fragment.app.Fragment? = mPagerAdapter.getItem(tempIndex)
        tempFragment?.let {
            if (it is ProgramTabFragment) {
                it.scrollToTopAndRefresh()
            }

        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        logger.info("onHiddenChanged=$hidden")
        super.onHiddenChanged(hidden)
        val tempIndex = view_pager.currentItem
        val tempFragment: androidx.fragment.app.Fragment? = mPagerAdapter.getItem(tempIndex)
        tempFragment?.let {
            if (it is ProgramTabFragment) {
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
                val fragment: Fragment =  ProgramTabFragment.newInstance(mTabTitles[position])
                mFragmentList.put(position, fragment)
                return fragment
            }

            override fun getPageTitle(position: Int): CharSequence {
                return mTabTitles[position].typeName
            }
        }
    }

}