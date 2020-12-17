package com.julun.huanque.core.ui.main.tagmanager

import android.app.Activity
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.util.SparseArray
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemDragListener
import com.chad.library.adapter.base.module.DraggableModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.julun.huanque.common.base.BaseVMActivity
import com.julun.huanque.common.basic.NetState
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.bean.beans.CollapsingToolbarLayoutState
import com.julun.huanque.common.bean.beans.DynamicGroup
import com.julun.huanque.common.bean.beans.PagerTab
import com.julun.huanque.common.bean.beans.TagManagerBean
import com.julun.huanque.common.suger.dp2pxf
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.widgets.indicator.ScaleTransitionPagerTitleView
import com.julun.huanque.core.R
import kotlinx.android.synthetic.main.activity_favorite_tag_manager.*
import net.lucode.hackware.magicindicator.ViewPagerHelper
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator
import org.jetbrains.anko.startActivity
import kotlin.properties.Delegates

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/12/16 19:58
 *
 *@Description: TagManagerActivity
 *
 */
class TagManagerActivity : BaseVMActivity<TagManagerViewModel>() {

    companion object {
        fun start(act: Activity) {
            act.startActivity<TagManagerActivity>()
        }
    }


//    private var state: CollapsingToolbarLayoutState? = null


    private lateinit var mCommonNavigator: CommonNavigator
    private var mFragmentList = SparseArray<Fragment>()
    private val mTabTitles = arrayListOf<PagerTab>()

    private var currentSelect: Int = -1
    private var deleteMode: Boolean by Delegates.observable(false) { _, _, newValue ->
        if (newValue) {
            btn_action_tb.text = "完成"
        } else {
            btn_action_tb.text = "管理"
        }
        tagAdapter.notifyDataSetChanged()
    }
    private val tagAdapter: BaseQuickAdapter<TagManagerBean, BaseViewHolder> by lazy {
        object : BaseQuickAdapter<TagManagerBean, BaseViewHolder>(R.layout.item_tag_manager, mViewModel.currentTagList),
            DraggableModule {
            override fun convert(holder: BaseViewHolder, item: TagManagerBean) {
                val tagName = holder.getView<TextView>(R.id.tag_name)
                val str = if (item.num == 0) {
                    item.tagName
                } else {
                    "${item.tagName}(${item.num})"
                }
                tagName.text = str
                tagName.isSelected = currentSelect == holder.adapterPosition
                holder.setGone(R.id.iv_delete, !deleteMode || currentSelect == holder.adapterPosition)
            }

        }
    }
    private val listener: OnItemDragListener by lazy {

        // 拖拽监听
        object : OnItemDragListener {
            override fun onItemDragStart(viewHolder: RecyclerView.ViewHolder, pos: Int) {
                logger.info("drag start")
                val holder = viewHolder as BaseViewHolder
//                currentSelect = holder.adapterPosition
//                deleteMode = true
            }

            override fun onItemDragMoving(
                source: RecyclerView.ViewHolder,
                from: Int,
                target: RecyclerView.ViewHolder,
                to: Int
            ) {
                logger.info("move from: " + source.adapterPosition + " to: " + target.adapterPosition)
            }

            override fun onItemDragEnd(viewHolder: RecyclerView.ViewHolder, pos: Int) {
                logger.info("drag end")
                val holder = viewHolder as BaseViewHolder
                currentSelect = -1
                tagAdapter.notifyDataSetChanged()
                // 结束时，item背景色变化，demo这里使用了一个动画渐变，使得自然
                logger.info("更换位置后${mViewModel.currentTagList}")
            }
        }

    }

    override fun getLayoutId(): Int {
        return R.layout.activity_favorite_tag_manager
    }

    private val viewModel: TagManagerViewModel by viewModels()


    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        //
        //设置头部边距
        initViewModel()
        initViewPager()
        initMagicIndicator()

        val flexBoxLayoutManager = FlexboxLayoutManager(this)
        myTags.layoutManager = flexBoxLayoutManager
        myTags.adapter = tagAdapter

//        mAdapter.getDraggableModule().setSwipeEnabled(true);
        tagAdapter.draggableModule.isDragEnabled = true
        tagAdapter.draggableModule.setOnItemDragListener(listener)
        tagAdapter.setOnItemLongClickListener { adapter, view, position ->
            logger.info("长按")
            currentSelect = position
            deleteMode = true
            tagAdapter.notifyDataSetChanged()
            false
        }
        tagAdapter.setOnItemClickListener { _, view, position ->
            logger.info("点击item=$position")
            if (deleteMode) {
                tagAdapter.removeAt(position)
                logger.info("删除后${mViewModel.currentTagList}")
            }
        }
        iv_back.onClickNew {
            onBackPressed()
        }
        tv_title.text = "我喜欢的标签"
        btn_action_tb.onClickNew {
            deleteMode = !deleteMode
        }
    }

    private fun initViewPager() {
        view_pager.adapter = mPagerAdapter
        //配置预加载页数
//        view_pager.offscreenPageLimit = 2
        view_pager.currentItem = 0

    }

    private fun initViewModel() {
        viewModel.tabList.observe(this, Observer {
            if (it.state == NetStateType.SUCCESS) {
                mTabTitles.clear()
                mTabTitles.addAll(it.requireT())
                refreshTabList()
            }

        })
        viewModel.tagChange.observe(this, Observer {
            if (it != null) {
                tagAdapter.notifyDataSetChanged()
            }

        })
        viewModel.queryInfo()
    }

    private fun renderHeaderData(group: DynamicGroup) {

    }

    override fun onBackPressed() {
        if (deleteMode) {
            deleteMode = false
            return
        }
        super.onBackPressed()
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
                logger.info("getTitleView：$index")

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


    private val mPagerAdapter: FragmentPagerAdapter by lazy {
        object : FragmentPagerAdapter(supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            override fun getItem(position: Int): Fragment {
                return mFragmentList[position] ?: getFragment(position)
            }

            override fun getCount(): Int {
                return mTabTitles.size
            }

            private fun getFragment(position: Int): Fragment {
                val fragment: Fragment = TagTabFragment.newInstance(mTabTitles[position])
                mFragmentList.put(position, fragment)
                return fragment
            }

            override fun getPageTitle(position: Int): CharSequence {
                return mTabTitles[position].typeName
            }
        }
    }

    override fun showLoadState(state: NetState) {
    }

    override fun onViewDestroy() {
        super.onViewDestroy()
    }
}