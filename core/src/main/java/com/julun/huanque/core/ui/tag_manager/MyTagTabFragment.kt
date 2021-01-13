package com.julun.huanque.core.ui.tag_manager

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.base.BaseLazyFragment
import com.julun.huanque.common.bean.beans.TagTypeTag
import com.julun.huanque.common.bean.beans.UserTagBean
import com.julun.huanque.common.constant.BooleanType
import com.julun.huanque.common.constant.IntentParamKey
import com.julun.huanque.common.constant.MyTagType
import com.julun.huanque.common.helper.MixedHelper
import com.julun.huanque.common.manager.HuanViewModelManager
import com.julun.huanque.common.suger.dp2px
import com.julun.huanque.common.suger.loadImage
import com.julun.huanque.common.suger.onAdapterClickNew
import com.julun.huanque.common.suger.show
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
class MyTagTabFragment : BaseLazyFragment() {

    companion object {
        fun newInstance(tab: TagTypeTag?): MyTagTabFragment {
            return MyTagTabFragment().apply {
                val bundle = Bundle()
                bundle.putSerializable(IntentParamKey.TAB_TYPE.name, tab)
                this.arguments = bundle
            }
        }
    }

    private val tagManagerViewModel: TagManagerViewModel = HuanViewModelManager.tagManagerViewModel

    private var currentTab: TagTypeTag? = null

    private val mAdapter: BaseQuickAdapter<UserTagBean, BaseViewHolder> by lazy {
        object : BaseQuickAdapter<UserTagBean, BaseViewHolder>(R.layout.item_my_tag_list) {

            init {
//                addChildClickViewIds(R.id.iv_tag_like)
            }

            override fun convert(holder: BaseViewHolder, item: UserTagBean) {
                val sdv = holder.getView<SimpleDraweeView>(R.id.card_img)
                val sdvTag = holder.getView<SimpleDraweeView>(R.id.sdv_tag)
                sdv.loadImage(item.tagPic, 82f, 110f)
                sdvTag.loadImage(item.tagIcon, 18f, 18f)
                if (currentTab?.showType == MyTagType.AUTH) {
                    if (item.mark == BooleanType.TRUE) {
                        holder.setImageResource(R.id.iv_tag_like, R.mipmap.ic_tag_pic_auth_success)
                        sdv.alpha = 1f
                    } else {
                        holder.setImageResource(R.id.iv_tag_like, R.mipmap.ic_tag_pic_auth_no)
                        sdv.alpha = 0.5f
                    }
                } else {
                    if (item.mark == BooleanType.TRUE) {
                        holder.setImageResource(R.id.iv_tag_like, R.mipmap.ic_tag_like_yes)
                        sdv.alpha = 1f
                    } else {
                        holder.setImageResource(R.id.iv_tag_like, R.mipmap.ic_tag_like_no)
                        sdv.alpha = 0.5f
                    }
                }

                holder.setText(R.id.tv_tag_name, item.tagName)

            }

        }
    }


    override fun getLayoutId(): Int = R.layout.fragment_favorite_tag_tab

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        currentTab = arguments?.getSerializable(IntentParamKey.TAB_TYPE.name) as? TagTypeTag
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
            if (currentTab?.showType == MyTagType.AUTH) {
//                TagPicsActivity.start(requireActivity(), item)
                AuthTagPicActivity.start(requireActivity(), item.tagId, isMe = true, isMyLike = false, isSameSex = false)
            } else {
                TagPicsActivity.start(requireActivity(), item)
            }
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
        logger.info("onResume =${currentTab?.tabTagName}")
        super.onResume()
    }

    override fun onPause() {
        logger.info("onPause =${currentTab?.tabTagName}")
        super.onPause()
    }

    /**
     * 该方法供父容器在显隐变化时调用
     */
    override fun onParentHiddenChanged(hidden: Boolean) {
        logger.info("当前的界面开始显隐=${currentTab?.tabTagName} hide=$hidden")
    }

    override fun lazyLoadData() {
        renderData(currentTab ?: return)
    }


    private fun initViewModel() {
//        tagManagerViewModel.tagChangeStatus.observe(this, Observer {
//            if (it.isSuccess()) {
//                if (it.isNew() && it.requireT().like) {
//                    ToastUtils.showToastCustom(R.layout.layout_toast_tag, action = { view, t ->
//                        val tv = view.findViewById<TextView>(R.id.toastContent)
//                        t.duration = Toast.LENGTH_SHORT
//                        t.setGravity(Gravity.CENTER, 0, 0)
//                        tv.text = "已喜欢"
//                    })
//                }
//                val tag = it.requireT()
//                if (tag.parentTagId != currentTab?.tagId) {
//                    return@Observer
//                }
//                val result = mAdapter.data.firstOrNull { item -> item.tagId == tag.tagId } ?: return@Observer
//                result.like = tag.like
//                val index = mAdapter.data.indexOf(result)
//                logger.info("index=$index")
//                MixedHelper.safeNotifyItem(index, postList, mAdapter)
//
//            } else if (it.state == NetStateType.ERROR) {
////                if (it.isNew()) {
////                    ToastUtils.show("网络异常")
////                }
//            }
//        })

    }


    private fun renderData(listData: TagTypeTag) {

        mAdapter.setList(listData.groupTagList)

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