package com.julun.huanque.core.ui.tag_manager

import android.app.Activity
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.util.SparseArray
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.util.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemDragListener
import com.chad.library.adapter.base.module.DraggableModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.google.android.flexbox.FlexboxLayoutManager
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.base.dialog.CustomDialogFragment
import com.julun.huanque.common.base.dialog.MyAlertDialog
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.bean.beans.ManagerTagTabBean
import com.julun.huanque.common.bean.beans.UserTagBean
import com.julun.huanque.common.manager.HuanViewModelManager
import com.julun.huanque.common.suger.dp2pxf
import com.julun.huanque.common.suger.loadImage
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.utils.NotificationUtils
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.common.widgets.indicator.ScaleTransitionPagerTitleView
import com.julun.huanque.core.R
import kotlinx.android.synthetic.main.activity_favorite_tag_manager.*
import kotlinx.android.synthetic.main.dialog_tag_cancel_ensure.view.*
import net.lucode.hackware.magicindicator.ViewPagerHelper
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator
import org.jetbrains.anko.backgroundResource
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
class TagManagerActivity : BaseActivity() {

    companion object {

        fun start(act: Activity) {
//            act.startActivityForResult<TagManagerActivity>(requestCode = ActivityRequestCode.MANAGER_TAG_RESULT_CODE)
            act.startActivity<TagManagerActivity>()
        }
    }

    //    private var state: CollapsingToolbarLayoutState? = null
    private var viewModel = HuanViewModelManager.tagManagerViewModel

    private lateinit var mCommonNavigator: CommonNavigator
    private var mFragmentList = SparseArray<TagTabFragment>()
    private val mTabTitles = arrayListOf<ManagerTagTabBean>()

    private var currentSelect: Int = -1
    var deleteMode: Boolean by Delegates.observable(false) { _, _, newValue ->
        if (newValue) {
            btn_action_tb.backgroundResource = R.drawable.bg_solid_btn1
            btn_action_tb.text = "完成"
            btn_action_tb.isActivated = true
        } else {
            btn_action_tb.backgroundResource = 0
            btn_action_tb.text = "管理"
            btn_action_tb.isActivated = false
        }
        doTipsText(newValue)
        tagAdapter.notifyDataSetChanged()
        mFragmentList.forEach { _, value ->
            value.refreshMode(newValue)
        }
    }
    private val tagAdapter: BaseQuickAdapter<UserTagBean, BaseViewHolder> by lazy {
        object : BaseQuickAdapter<UserTagBean, BaseViewHolder>(R.layout.item_tag_manager, viewModel.currentTagList),
            DraggableModule {
            override fun convert(holder: BaseViewHolder, item: UserTagBean) {
                val tagName = holder.getView<TextView>(R.id.tag_name)
                val ctLayout = holder.getView<View>(R.id.ct_layout)
                val str = if (item.likeCnt == 0) {
                    item.tagName
                } else {
                    "${item.tagName} ${item.likeCnt}"
                }
                tagName.text = str
                tagName.isSelected = currentSelect == holder.adapterPosition
                if (currentSelect == holder.adapterPosition) {
                    ctLayout.scaleX = 1.2f
                    ctLayout.scaleY = 1.2f
                } else {
                    ctLayout.scaleX = 1f
                    ctLayout.scaleY = 1f

                }

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
                viewModel.tagSequenceHasChange = true
                val holder = viewHolder as BaseViewHolder
                currentSelect = -1
                tagAdapter.notifyDataSetChanged()
                logger.info("更换位置后${viewModel.currentTagList}")
            }
        }

    }

    override fun getLayoutId(): Int {
        return R.layout.activity_favorite_tag_manager
    }

    private fun doTipsText(deleteMode:Boolean){
        if (deleteMode) {
            if (viewModel.currentTagList.isEmpty()) {
                tv_mode_des.text = "点击管理或长按，可添加喜欢的标签"
            } else {
                tv_mode_des.text = "点击可取消喜欢，长按可拖动"
            }
        } else {
            if (viewModel.currentTagList.isEmpty()) {
                tv_mode_des.text = "点击管理或长按，可添加喜欢的标签"
            } else {
                tv_mode_des.text = "点击管理或长按，可编辑排序"
            }
        }
    }
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
//            tagAdapter.notifyDataSetChanged()
            false
        }
        tagAdapter.setOnItemClickListener { _, view, position ->
            logger.info("点击item=$position")
            val item = tagAdapter.getItemOrNull(position) ?: return@setOnItemClickListener
            if (deleteMode) {
//                viewModel.tagCancelGroupLike(item)

//                val deleteEnsureDialog = CustomDialogFragment(
//                    builder = CustomDialogFragment.FDBuilder(
//                        layoutId = {
//                            return@FDBuilder R.layout.dialog_tag_cancel_ensure
//                        },
//                        onStart = { dialog ->
//                            dialog.setDialogSize(
//                                gravity = Gravity.CENTER,
//                                height = ViewGroup.LayoutParams.WRAP_CONTENT,
//                                padding = 20
//                            )
//                        },
//                        initViews = { dialog, rootView ->
//                            rootView.sdv_tag_pic.loadImage(item.tagPic, 120f, 120f)
//                            rootView.tv_content.text = "取消喜欢后，将取消【${item.tagName}】下的${item.likeCnt}个标签"
//                            rootView.tv_btn_ok.onClickNew {
//                                viewModel.tagCancelGroupLike(item)
//                                dialog.dismiss()
//                            }
//                            rootView.tv_btn_cancel.onClickNew {
//                                dialog.dismiss()
//                            }
//                        })
//                )
//                deleteEnsureDialog.show(supportFragmentManager, "deleteEnsureDialog")
                MyAlertDialog(this).showAlertWithOKAndCancel(
                    "取消喜欢后，将取消【${item.tagName}】下的${item.likeCnt}个标签", MyAlertDialog.MyDialogCallback(onRight = {
                        viewModel.tagCancelGroupLike(item)
                    }),
                    "确认要取消喜欢吗", "确定取消", "在想想"
                )

            }
        }
        iv_back.onClickNew {
            onBackPressed()
        }
        tv_title.text = "标签管理"
        btn_action_tb.onClickNew {
            if (deleteMode) {
                viewModel.saveTagList()
            }
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
            if (it.state == NetStateType.SUCCESS && it.isNew()) {
                mTabTitles.clear()
                mTabTitles.addAll(it.requireT())
                refreshTabList()
                if (viewModel.currentTagList.isEmpty()) {
                    tv_mode_des.text = "点击管理或长按，可添加喜欢的标签"
                }
            }

        })
        viewModel.tagChange.observe(this, Observer {
            if (it != null) {
                doTipsText(deleteMode)
                tagAdapter.notifyDataSetChanged()
            }

        })
        viewModel.saveTagList.observe(this, Observer {
            if (it != null && it.isSuccess() && it.isNew()) {
                ToastUtils.show("标签保存成功")
            }

        })
        viewModel.queryInfo()
    }

    override fun onBackPressed() {
        if (deleteMode) {
            viewModel.saveTagList()
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
                simplePagerTitleView.minScale = 0.727f
                simplePagerTitleView.text = mTabTitles[index].tagName
                simplePagerTitleView.textSize = 20f
                simplePagerTitleView.normalColor = ContextCompat.getColor(context, R.color.black_333)
                simplePagerTitleView.selectedColor = ContextCompat.getColor(context, R.color.black_333)
                simplePagerTitleView.setOnClickListener { view_pager.currentItem = index }
                if (view_pager.currentItem == index) {
//                    simplePagerTitleView.setTextColor(ContextCompat.getColor(context, R.color.black_333))
                    simplePagerTitleView.scaleX = 1.0f
                    simplePagerTitleView.scaleY = 1.0f
                } else {
//                    simplePagerTitleView.setTextColor(ContextCompat.getColor(context, R.color.black_666))
                    simplePagerTitleView.scaleX = 0.7f
                    simplePagerTitleView.scaleY = 0.7f
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
    private fun refreshTabList(currentTab: Int? = null) {
        if (mTabTitles.isNotEmpty()) {
//            if (mTabTitles.size > 4) {
//                mCommonNavigator.isAdjustMode = false
//            }
            mPagerAdapter.notifyDataSetChanged()
            view_pager.offscreenPageLimit = mTabTitles.size
            mCommonNavigator.notifyDataSetChanged()

            if (currentTab != null) {
                switchToTab(currentTab)
            }
        } else {
//            showErrorView(false)
        }
    }

    /**
     *  手动切换到指定tab位置
     */
    private fun switchToTab(currentTabCode: Int) {
        var position = 0
        run breaking@{
            mTabTitles.forEachIndexed { index, newProgramTab ->
                if (currentTabCode == newProgramTab.tagId) {
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

    fun switchToDeleteMode() {
        deleteMode = true
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
                val fragment: TagTabFragment = TagTabFragment.newInstance(mTabTitles[position])
                mFragmentList.put(position, fragment)
                return fragment
            }

            override fun getPageTitle(position: Int): CharSequence {
                return mTabTitles[position].tagName
            }
        }
    }


    override fun onViewDestroy() {
        super.onViewDestroy()
    }
}