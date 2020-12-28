package com.julun.huanque.core.ui.main.tagmanager

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.base.BaseLazyFragment
import com.julun.huanque.common.base.BaseVMFragment
import com.julun.huanque.common.basic.NetState
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.constant.IntentParamKey
import com.julun.huanque.common.helper.MixedHelper
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.ToastUtils
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

    private val tagManagerViewModel: TagManagerViewModel by activityViewModels()

    private var currentTab: ManagerTagTabBean? = null

    private val mAdapter: BaseQuickAdapter<ManagerTagBean, BaseViewHolder> by lazy {
        object : BaseQuickAdapter<ManagerTagBean, BaseViewHolder>(R.layout.item_favorite_tag_list), LoadMoreModule {

            init {
                addChildClickViewIds(R.id.iv_tag_like)
            }

            override fun convert(holder: BaseViewHolder, item: ManagerTagBean) {
                val sdv = holder.getView<SimpleDraweeView>(R.id.card_img)
                val sdvTag = holder.getView<SimpleDraweeView>(R.id.sdv_tag)
                sdv.loadImage(item.tagPic, 82f, 110f)
                sdvTag.loadImage(item.tagIcon, 18f, 18f)
                if (item.like) {
                    holder.setImageResource(R.id.iv_tag_like, R.mipmap.icon_tag_like)
                } else {
                    holder.setImageResource(R.id.iv_tag_like, R.mipmap.icon_tag_dislike)
                }
                holder.setText(R.id.tv_tag_name, item.tagName)

            }

        }
    }


    override fun getLayoutId(): Int = R.layout.fragment_favorite_tag_tab

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        currentTab = arguments?.getSerializable(IntentParamKey.TAB_TYPE.name) as? ManagerTagTabBean
        initViewModel()
        postList.layoutManager = GridLayoutManager(requireContext(), 4)
        postList.addItemDecoration(GridLayoutSpaceItemDecoration2(dp2px(6)))
        postList.adapter = mAdapter

        (postList.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false


    }

    override fun initEvents(rootView: View) {
        super.initEvents(rootView)

        mAdapter.onAdapterClickNew { _, _, position ->
            //todo
            logger.info("跳转到标签详情")


        }
        mAdapter.onAdapterChildClickNew { adapter, view, position ->
            when (view.id) {
                R.id.iv_tag_like -> {
                    val item = mAdapter.getItemOrNull(position) ?: return@onAdapterChildClickNew
                    val parentTag = currentTab ?: return@onAdapterChildClickNew
                    if (!item.like) {
                        tagManagerViewModel.tagLike(item, parentTag)
                    } else {
                        tagManagerViewModel.tagCancelLike(item, parentTag)
                    }
                }
            }
        }
    }

    override fun onResume() {
        logger.info("onResume =${currentTab?.tagName}")
        super.onResume()
    }

    override fun onPause() {
        logger.info("onPause =${currentTab?.tagName}")
        super.onPause()
    }

    /**
     * 该方法供父容器在显隐变化时调用
     */
    override fun onParentHiddenChanged(hidden: Boolean) {
        logger.info("当前的界面开始显隐=${currentTab?.tagName} hide=$hidden")
    }

    override fun lazyLoadData() {
        renderData(currentTab ?: return)
    }


    private fun initViewModel() {
        tagManagerViewModel.tagChangeStatus.observe(this, Observer {
            if (it.isSuccess()) {
                if(it.isNew()&&it.requireT().like){
                    ToastUtils.showToastCustom(R.layout.layout_toast_tag, action = { view, t ->
                        val tv = view.findViewById<TextView>(R.id.toastContent)
                        t.duration = Toast.LENGTH_SHORT
                        t.setGravity(Gravity.CENTER, 0, 0)
                        tv.text = "已喜欢"
                    })
                }

                val index = mAdapter.data.indexOf(it.requireT())
                if (index != -1) {
                    mAdapter.notifyItemChanged(index)
                }
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