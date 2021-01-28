package com.julun.huanque.core.ui.main.heartbeat

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import com.julun.huanque.common.base.BaseLazyFragment
import com.julun.huanque.common.basic.NetState
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.bean.beans.UserTagBean
import com.julun.huanque.common.bean.events.LoginEvent
import com.julun.huanque.common.manager.HuanViewModelManager
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.removeDuplicate
import com.julun.huanque.common.viewmodel.TagManagerViewModel
import com.julun.huanque.core.R
import com.julun.huanque.core.ui.tag_manager.TagManagerActivity
import kotlinx.android.synthetic.main.fragment_favorite_container.*
import net.lucode.hackware.magicindicator.ViewPagerHelper
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.CommonPagerTitleView
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.backgroundResource

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/12/16 9:50
 *
 *@Description: HomeFavoriteFragment
 *
 */
class FavoriteFragment : BaseLazyFragment() {

    companion object {
        fun newInstance() = FavoriteFragment()
    }

    private val viewModel: FavoriteViewModel by activityViewModels()
    private val tagManagerViewModel: TagManagerViewModel = HuanViewModelManager.tagManagerViewModel

    private val observer by lazy {
        Observer<NetState> { state ->
            if (state != null) {
                when (state.state) {
                    NetStateType.SUCCESS -> {//showSuccess()
                        state_pager_view.showSuccess()
                    }
                    NetStateType.LOADING -> {//showLoading()
                        state_pager_view.showLoading()
                    }
                    NetStateType.ERROR, NetStateType.NETWORK_ERROR -> {
                        state_pager_view.showError(showBtn = true, btnClick = View.OnClickListener {
                            viewModel.queryInfo(QueryType.INIT)
                        })
                    }

                }

            }


        }
    }

    private lateinit var mCommonNavigator: CommonNavigator
    private var mFragmentMap = HashMap<Int, Fragment>()
    private val mTabTitles = arrayListOf<UserTagBean>()

    private var currentTag: UserTagBean? = null
    override fun getLayoutId(): Int {
        return R.layout.fragment_favorite_container
    }

    override fun isRegisterEventBus(): Boolean {
        return true
    }

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {

        //
        logger.info("initViews")
        //设置头部边距
        initViewModel()
        initViewPager()
        initMagicIndicator()

        tag_manager.onClickNew {
            TagManagerActivity.start(requireActivity())
        }
    }

    private fun initViewPager() {
        view_pager.adapter = mPagerAdapter
        //配置预加载页数
//        view_pager.offscreenPageLimit = 2
        view_pager.currentItem = 0
        view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                currentTag = mTabTitles.getOrNull(position)

            }

        })

    }

    private fun initViewModel() {

        viewModel.firstListData.observe(viewLifecycleOwner, Observer {
            if (it.state == NetStateType.SUCCESS) {
                mTabTitles.clear()
                mTabTitles.addAll(it.requireT().tagList)
                refreshTabList()
                //首次加载时给currentTagList赋值
                tagManagerViewModel.currentTagList.addAll(it.requireT().tagList.filter { item -> item.tagId != -1 })
            }

        })
        viewModel.loadState.observe(this, observer)

        tagManagerViewModel.tagHasChange.observe(this, Observer {
            val list = tagManagerViewModel.currentTagList
            logger.info("我是选择的结果=${list}")
            if (viewModel.firstListData.value == null) {
                logger.info("基础数据还没就位 这里直接返回")
                return@Observer
            }
            list.removeDuplicate()
            val first = mTabTitles.firstOrNull()
            mTabTitles.clear()

            if (first != null) {
                mTabTitles.add(first)
            }
            mTabTitles.addAll(list)
            refreshTabList(currentTag?.tagId)
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
//            when (requestCode) {
//                ActivityRequestCode.MANAGER_TAG_RESULT_CODE -> {
//                    val list = data?.extras?.get(ManagerTagCode.TAG_LIST) as? ArrayList<ManagerTagBean> ?: return
//                    logger.info("我是选择的结果=${list}")
//                    val first = mTabTitles.first()
//                    mTabTitles.clear()
//                    mTabTitles.add(first)
//                    mTabTitles.addAll(list)
//                    refreshTabList(currentTag?.tagId)
//                }
//            }
        }
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
                val simplePagerTitleView = CommonPagerTitleView(context)
                simplePagerTitleView.setContentView(R.layout.view_home_tab_title)
                val tabTitle = simplePagerTitleView.findViewById<TextView>(R.id.tvTabTitle)
                simplePagerTitleView.onPagerTitleChangeListener = object : CommonPagerTitleView.OnPagerTitleChangeListener {
                    override fun onDeselected(index: Int, totalCount: Int) {
//                            tabTitle.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
                        logger.info("onDeselected:$index")
                        tabTitle.setTextColor(ContextCompat.getColor(context, R.color.black_666))
                        tabTitle.backgroundResource = R.drawable.bg_favorite_tab_normal
                    }

                    override fun onSelected(index: Int, totalCount: Int) {
                        logger.info("onSelected:$index")
                        tabTitle.setTextColor(ContextCompat.getColor(context, R.color.white))
                        tabTitle.backgroundResource = R.drawable.bg_favorite_tab_select
                    }

                    override fun onLeave(index: Int, totalCount: Int, leavePercent: Float, leftToRight: Boolean) {
                    }

                    override fun onEnter(index: Int, totalCount: Int, enterPercent: Float, leftToRight: Boolean) {
                    }
                }
                tabTitle.text = mTabTitles[index].tagName
                simplePagerTitleView.setOnClickListener { view_pager.currentItem = index }
                if (view_pager.currentItem == index) {
                    tabTitle.setTextColor(ContextCompat.getColor(context, R.color.white))
                    tabTitle.backgroundResource = R.drawable.bg_favorite_tab_select
                } else {
                    tabTitle.setTextColor(ContextCompat.getColor(context, R.color.black_666))
                    tabTitle.backgroundResource = R.drawable.bg_favorite_tab_normal
                }
                return simplePagerTitleView
            }

            override fun getIndicator(context: Context): IPagerIndicator? {
//                val indicator = WrapPagerIndicator(context)
//                indicator.verticalPadding = dp2px(2)
//                indicator.fillColor = ContextCompat.getColor(context, R.color.colorAccent_lib)
//                return indicator
                return null
            }
        }
        magic_indicator.navigator = mCommonNavigator
        ViewPagerHelper.bind(magic_indicator, view_pager)
    }

    /**
     * 刷新tab数据 并且切换到指定tab
     */
    private fun refreshTabList(currentTagId: Int? = null) {
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


            if (currentTagId != null && currentTagId != 0) {
                switchToTab(currentTagId)
            }
        } else {
//            showErrorView(false)
        }
    }

    /**
     *  手动切换到指定tab位置
     */
    private fun switchToTab(currentTagId: Int) {
        var position = 0
        run breaking@{
            mTabTitles.forEachIndexed { index, newProgramTab ->
                if (currentTagId == newProgramTab.tagId) {
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


    override fun lazyLoadData() {
        viewModel.queryInfo()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun receiveLoginCode(event: LoginEvent) {
        logger.info("登录事件:${event.result}")
        if (event.result) {
            viewModel.queryInfo()
        }

    }

    override fun onHiddenChanged(hidden: Boolean) {
        logger.info("onHiddenChanged=$hidden")
        super.onHiddenChanged(hidden)
        val tempIndex = view_pager.currentItem
        val tempFragment: androidx.fragment.app.Fragment? = mPagerAdapter.getItem(tempIndex)
        tempFragment?.let {
            if (it is FavoriteTabFragment) {
                it.onParentHiddenChanged(hidden)
            }

        }
    }

    private val mPagerAdapter: FragmentPagerAdapter by lazy {
        object : FragmentPagerAdapter(childFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            override fun getItem(position: Int): Fragment {
                return mFragmentMap[mTabTitles[position].tagId] ?: getFragment(position)
            }

            override fun getCount(): Int {
                return mTabTitles.size
            }

            private fun getFragment(position: Int): Fragment {
                val fragment: Fragment = FavoriteTabFragment.newInstance(mTabTitles[position])
                mFragmentMap.put(mTabTitles[position].tagId, fragment)
                return fragment
            }

            override fun getPageTitle(position: Int): CharSequence {
                return mTabTitles[position].tagName
            }
        }
    }

}