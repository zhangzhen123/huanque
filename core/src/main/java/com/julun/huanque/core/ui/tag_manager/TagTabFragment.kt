package com.julun.huanque.core.ui.tag_manager

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.base.BaseLazyFragment
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.constant.IntentParamKey
import com.julun.huanque.common.helper.MixedHelper
import com.julun.huanque.common.manager.HuanViewModelManager
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.common.viewmodel.TagManagerViewModel
import com.julun.huanque.common.widgets.recycler.decoration.GridLayoutSpaceItemDecoration2
import com.julun.huanque.core.R
import kotlinx.android.synthetic.main.fragment_favorite_tag_tab.*

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/11/20 17:34
 *
 *@Description: DynamicTabFragment
 *
 */
class TagTabFragment : BaseLazyFragment() {

    companion object {
        fun newInstance(tab: ManagerTagTabBean?): TagTabFragment {
            return TagTabFragment().apply {
                val bundle = Bundle()
                bundle.putSerializable(IntentParamKey.TAB_TYPE.name, tab)
                this.arguments = bundle
            }
        }
    }

    private val tagManagerViewModel: TagManagerViewModel = HuanViewModelManager.tagManagerViewModel

    private var currentTab: ManagerTagTabBean? = null

    private val mAdapter: BaseQuickAdapter<UserTagBean, BaseViewHolder> by lazy {
        object : BaseQuickAdapter<UserTagBean, BaseViewHolder>(R.layout.item_favorite_tag_list), LoadMoreModule {

//            init {
//                addChildClickViewIds(R.id.iv_tag_like)
//            }

            override fun convert(holder: BaseViewHolder, item: UserTagBean) {
                val sdv = holder.getView<SimpleDraweeView>(R.id.card_img)
                val sdvTag = holder.getView<SimpleDraweeView>(R.id.sdv_tag)
                sdv.loadImage(item.tagPic, 82f, 110f)
                sdvTag.loadImage(item.tagIcon, 18f, 18f)

                if (item.like) {
                    holder.setImageResource(R.id.iv_tag_like, R.mipmap.icon_mine_tag_like)
                    sdv.alpha = 1f
                } else {
                    holder.setImageResource(R.id.iv_tag_like, R.mipmap.icon_mine_tag_like_no)
                    sdv.alpha = 0.5f
                }
                if (deleteMode) {
                    holder.setVisible(R.id.iv_tag_like, true)
                } else {
                    holder.setGone(R.id.iv_tag_like, true)
                }

                holder.setText(R.id.tv_tag_name, item.tagName)

            }

        }
    }


    override fun getLayoutId(): Int = R.layout.fragment_favorite_tag_tab

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        val act = requireActivity()
        if (act is TagManagerActivity) {
            deleteMode = act.deleteMode
        }
        currentTab = arguments?.getSerializable(IntentParamKey.TAB_TYPE.name) as? ManagerTagTabBean
        initViewModel()
        postList.layoutManager = GridLayoutManager(requireContext(), 3)
        postList.addItemDecoration(GridLayoutSpaceItemDecoration2(dp2px(15)))
        postList.adapter = mAdapter

        (postList.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false


    }

    override fun initEvents(rootView: View) {
        super.initEvents(rootView)

        mAdapter.onAdapterClickNew { _, _, position ->
            val item = mAdapter.getItemOrNull(position) ?: return@onAdapterClickNew
            logger.info("跳转到标签详情")

            if (deleteMode) {
                val parentTag = currentTab ?: return@onAdapterClickNew
                if (!item.like) {
                    tagManagerViewModel.tagLike(item/*, parentTag*/)
                } else {
                    tagManagerViewModel.tagCancelLike(item/*, parentTag*/)
                }
            } else {
                TagPicsActivity.start(requireActivity(), item)
            }


        }
        mAdapter.setOnItemLongClickListener { adapter, view, position ->
            val act = requireActivity()
            if (act is TagManagerActivity) {
                act.switchToDeleteMode()
            }
            true
        }
//        mAdapter.onAdapterChildClickNew { _, view, position ->
//            when (view.id) {
//                R.id.iv_tag_like -> {
//                    val item = mAdapter.getItemOrNull(position) ?: return@onAdapterChildClickNew
//                    val parentTag = currentTab ?: return@onAdapterChildClickNew
//                    if (!item.like) {
//                        tagManagerViewModel.tagLike(item/*, parentTag*/)
//                    } else {
//                        tagManagerViewModel.tagCancelLike(item/*, parentTag*/)
//                    }
//                }
//            }
//        }
    }

    override fun onResume() {
        logger.info("onResume =${currentTab?.tagName}")
        super.onResume()
    }

    override fun onPause() {
        logger.info("onPause =${currentTab?.tagName}")
        super.onPause()
    }


    override fun lazyLoadData() {
        renderData(currentTab ?: return)
    }

    private var deleteMode: Boolean = false
    fun refreshMode(mode: Boolean) {
        deleteMode = mode
        mAdapter.notifyDataSetChanged()
    }

    private fun initViewModel() {
        tagManagerViewModel.tagChangeStatus.observe(this, Observer {
            if (it.isSuccess()) {
                if (it.isNew() && it.requireT().like) {
                    ToastUtils.showToastCustom(R.layout.layout_toast_tag, action = { view, t ->
                        val tv = view.findViewById<TextView>(R.id.toastContent)
                        t.duration = Toast.LENGTH_SHORT
                        t.setGravity(Gravity.CENTER, 0, 0)
                        tv.text = "已喜欢"
                    })
                }
                val tag = it.requireT()
                if (tag.parentTagId != currentTab?.tagId) {
                    return@Observer
                }
                val result = mAdapter.data.firstOrNull { item -> item.tagId == tag.tagId } ?: return@Observer
                result.like = tag.like
                val index = mAdapter.data.indexOf(result)
                logger.info("index=$index")
                MixedHelper.safeNotifyItem(index, postList, mAdapter)

            } else if (it.state == NetStateType.ERROR) {
//                if (it.isNew()) {
//                    ToastUtils.show("网络异常")
//                }
            }
        })
        tagManagerViewModel.tagGroupRemove.observe(this, Observer {
            if (it.isSuccess()) {
                val tagBean = it.requireT()
                if (tagBean.tagId == currentTab?.tagId) {
                    currentTab?.childList?.forEach { item ->
                        item.like = false
                    }
                }
                mAdapter.notifyDataSetChanged()
            }
        })

    }


    private fun renderData(listData: ManagerTagTabBean) {

        mAdapter.setList(listData.childList)

        if (mAdapter.data.isEmpty()) {
            state_pager_view.showSuccess()
            postList.show()
            var message = "暂无内容"
            mAdapter.setEmptyView(
                MixedHelper.getEmptyView(
                    requireContext(),
                    msg = message
                )
            )
        } else {
            postList.show()
            state_pager_view.showSuccess()
        }
    }

}